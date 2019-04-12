/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef ART_RUNTIME_MONITOR_H_
#define ART_RUNTIME_MONITOR_H_

#include <pthread.h>
#include <stdint.h>
#include <stdlib.h>

#include <iosfwd>
#include <list>
#include <vector>

#include "base/allocator.h"
#include "base/atomic.h"
#include "base/mutex.h"
#include "gc_root.h"
#include "lock_word.h"
#include "obj_ptr.h"
#include "read_barrier_option.h"
#include "runtime_callbacks.h"
#include "thread_state.h"

namespace art {

class ArtMethod;
class IsMarkedVisitor;
class LockWord;
template<class T> class Handle;
class StackVisitor;
class Thread;
typedef uint32_t MonitorId;

namespace mirror {
class Object;
}  // namespace mirror

enum class LockReason {
  kForWait,
  kForLock,
};

class Monitor {
 public:
  // The default number of spins that are done before thread suspension is used to forcibly inflate
  // a lock word. See Runtime::max_spins_before_thin_lock_inflation_.
  constexpr static size_t kDefaultMaxSpinsBeforeThinLockInflation = 50;

  ~Monitor();

  static void Init(uint32_t lock_profiling_threshold, uint32_t stack_dump_lock_profiling_threshold);

  // Return the thread id of the lock owner or 0 when there is no owner.
  static uint32_t GetLockOwnerThreadId(ObjPtr<mirror::Object> obj)
      NO_THREAD_SAFETY_ANALYSIS;  // TODO: Reading lock owner without holding lock is racy.

  // NO_THREAD_SAFETY_ANALYSIS for mon->Lock.
  static ObjPtr<mirror::Object> MonitorEnter(Thread* thread,
                                             ObjPtr<mirror::Object> obj,
                                             bool trylock)
      EXCLUSIVE_LOCK_FUNCTION(obj.Ptr())
      NO_THREAD_SAFETY_ANALYSIS
      REQUIRES(!Roles::uninterruptible_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  // NO_THREAD_SAFETY_ANALYSIS for mon->Unlock.
  static bool MonitorExit(Thread* thread, ObjPtr<mirror::Object> obj)
      NO_THREAD_SAFETY_ANALYSIS
      REQUIRES(!Roles::uninterruptible_)
      REQUIRES_SHARED(Locks::mutator_lock_)
      UNLOCK_FUNCTION(obj.Ptr());

  static void Notify(Thread* self, ObjPtr<mirror::Object> obj)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    DoNotify(self, obj, false);
  }
  static void NotifyAll(Thread* self, ObjPtr<mirror::Object> obj)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    DoNotify(self, obj, true);
  }

  // Object.wait().  Also called for class init.
  // NO_THREAD_SAFETY_ANALYSIS for mon->Wait.
  static void Wait(Thread* self,
                   ObjPtr<mirror::Object> obj,
                   int64_t ms,
                   int32_t ns,
                   bool interruptShouldThrow, ThreadState why)
      REQUIRES_SHARED(Locks::mutator_lock_) NO_THREAD_SAFETY_ANALYSIS;

  static ThreadState FetchState(const Thread* thread,
                                /* out */ ObjPtr<mirror::Object>* monitor_object,
                                /* out */ uint32_t* lock_owner_tid)
      REQUIRES(!Locks::thread_suspend_count_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  // Used to implement JDWP's ThreadReference.CurrentContendedMonitor.
  static ObjPtr<mirror::Object> GetContendedMonitor(Thread* thread)
      REQUIRES_SHARED(Locks::mutator_lock_);

  // Calls 'callback' once for each lock held in the single stack frame represented by
  // the current state of 'stack_visitor'.
  // The abort_on_failure flag allows to not die when the state of the runtime is unorderly. This
  // is necessary when we have already aborted but want to dump the stack as much as we can.
  static void VisitLocks(StackVisitor* stack_visitor,
                         void (*callback)(ObjPtr<mirror::Object>, void*),
                         void* callback_context,
                         bool abort_on_failure = true)
      REQUIRES_SHARED(Locks::mutator_lock_);

  static bool IsValidLockWord(LockWord lock_word);

  template<ReadBarrierOption kReadBarrierOption = kWithReadBarrier>
  ObjPtr<mirror::Object> GetObject() REQUIRES_SHARED(Locks::mutator_lock_);

  void SetObject(ObjPtr<mirror::Object> object);

  Thread* GetOwner() const NO_THREAD_SAFETY_ANALYSIS {
    return owner_;
  }

  int32_t GetHashCode();

  bool IsLocked() REQUIRES_SHARED(Locks::mutator_lock_) REQUIRES(!monitor_lock_);

  bool HasHashCode() const {
    return hash_code_.load(std::memory_order_relaxed) != 0;
  }

  MonitorId GetMonitorId() const {
    return monitor_id_;
  }

  // Inflate the lock on obj. May fail to inflate for spurious reasons, always re-check.
  static void InflateThinLocked(Thread* self, Handle<mirror::Object> obj, LockWord lock_word,
                                uint32_t hash_code) REQUIRES_SHARED(Locks::mutator_lock_);

  // Not exclusive because ImageWriter calls this during a Heap::VisitObjects() that
  // does not allow a thread suspension in the middle. TODO: maybe make this exclusive.
  // NO_THREAD_SAFETY_ANALYSIS for monitor->monitor_lock_.
  static bool Deflate(Thread* self, ObjPtr<mirror::Object> obj)
      REQUIRES_SHARED(Locks::mutator_lock_) NO_THREAD_SAFETY_ANALYSIS;

#ifndef __LP64__
  void* operator new(size_t size) {
    // Align Monitor* as per the monitor ID field size in the lock word.
    void* result;
    int error = posix_memalign(&result, LockWord::kMonitorIdAlignment, size);
    CHECK_EQ(error, 0) << strerror(error);
    return result;
  }

  void operator delete(void* ptr) {
    free(ptr);
  }
#endif

 private:
  Monitor(Thread* self, Thread* owner, ObjPtr<mirror::Object> obj, int32_t hash_code)
      REQUIRES_SHARED(Locks::mutator_lock_);
  Monitor(Thread* self, Thread* owner, ObjPtr<mirror::Object> obj, int32_t hash_code, MonitorId id)
      REQUIRES_SHARED(Locks::mutator_lock_);

  // Install the monitor into its object, may fail if another thread installs a different monitor
  // first.
  bool Install(Thread* self)
      REQUIRES(!monitor_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  // Links a thread into a monitor's wait set.  The monitor lock must be held by the caller of this
  // routine.
  void AppendToWaitSet(Thread* thread) REQUIRES(monitor_lock_);

  // Unlinks a thread from a monitor's wait set.  The monitor lock must be held by the caller of
  // this routine.
  void RemoveFromWaitSet(Thread* thread) REQUIRES(monitor_lock_);

  void SignalContendersAndReleaseMonitorLock(Thread* self) RELEASE(monitor_lock_);

  // Changes the shape of a monitor from thin to fat, preserving the internal lock state. The
  // calling thread must own the lock or the owner must be suspended. There's a race with other
  // threads inflating the lock, installing hash codes and spurious failures. The caller should
  // re-read the lock word following the call.
  static void Inflate(Thread* self, Thread* owner, ObjPtr<mirror::Object> obj, int32_t hash_code)
      REQUIRES_SHARED(Locks::mutator_lock_)
      NO_THREAD_SAFETY_ANALYSIS;  // For m->Install(self)

  void LogContentionEvent(Thread* self,
                          uint32_t wait_ms,
                          uint32_t sample_percent,
                          ArtMethod* owner_method,
                          uint32_t owner_dex_pc)
      REQUIRES_SHARED(Locks::mutator_lock_);

  static void FailedUnlock(ObjPtr<mirror::Object> obj,
                           uint32_t expected_owner_thread_id,
                           uint32_t found_owner_thread_id,
                           Monitor* mon)
      REQUIRES(!Locks::thread_list_lock_,
               !monitor_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  // Try to lock without blocking, returns true if we acquired the lock.
  bool TryLock(Thread* self)
      REQUIRES(!monitor_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);
  // Variant for already holding the monitor lock.
  bool TryLockLocked(Thread* self)
      REQUIRES(monitor_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  template<LockReason reason = LockReason::kForLock>
  void Lock(Thread* self)
      REQUIRES(!monitor_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  bool Unlock(Thread* thread)
      REQUIRES(!monitor_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  static void DoNotify(Thread* self, ObjPtr<mirror::Object> obj, bool notify_all)
      REQUIRES_SHARED(Locks::mutator_lock_) NO_THREAD_SAFETY_ANALYSIS;  // For mon->Notify.

  void Notify(Thread* self)
      REQUIRES(!monitor_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  void NotifyAll(Thread* self)
      REQUIRES(!monitor_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  static std::string PrettyContentionInfo(const std::string& owner_name,
                                          pid_t owner_tid,
                                          ArtMethod* owners_method,
                                          uint32_t owners_dex_pc,
                                          size_t num_waiters)
      REQUIRES_SHARED(Locks::mutator_lock_);

  // Wait on a monitor until timeout, interrupt, or notification.  Used for Object.wait() and
  // (somewhat indirectly) Thread.sleep() and Thread.join().
  //
  // If another thread calls Thread.interrupt(), we throw InterruptedException and return
  // immediately if one of the following are true:
  //  - blocked in wait(), wait(long), or wait(long, int) methods of Object
  //  - blocked in join(), join(long), or join(long, int) methods of Thread
  //  - blocked in sleep(long), or sleep(long, int) methods of Thread
  // Otherwise, we set the "interrupted" flag.
  //
  // Checks to make sure that "ns" is in the range 0-999999 (i.e. fractions of a millisecond) and
  // throws the appropriate exception if it isn't.
  //
  // The spec allows "spurious wakeups", and recommends that all code using Object.wait() do so in
  // a loop.  This appears to derive from concerns about pthread_cond_wait() on multiprocessor
  // systems.  Some commentary on the web casts doubt on whether these can/should occur.
  //
  // Since we're allowed to wake up "early", we clamp extremely long durations to return at the end
  // of the 32-bit time epoch.
  void Wait(Thread* self, int64_t msec, int32_t nsec, bool interruptShouldThrow, ThreadState why)
      REQUIRES(!monitor_lock_)
      REQUIRES_SHARED(Locks::mutator_lock_);

  // Translates the provided method and pc into its declaring class' source file and line number.
  static void TranslateLocation(ArtMethod* method, uint32_t pc,
                                const char** source_file,
                                int32_t* line_number)
      REQUIRES_SHARED(Locks::mutator_lock_);

  uint32_t GetOwnerThreadId() REQUIRES(!monitor_lock_);

  // Support for systrace output of monitor operations.
  ALWAYS_INLINE static void AtraceMonitorLock(Thread* self,
                                              ObjPtr<mirror::Object> obj,
                                              bool is_wait)
      REQUIRES_SHARED(Locks::mutator_lock_);
  static void AtraceMonitorLockImpl(Thread* self,
                                    ObjPtr<mirror::Object> obj,
                                    bool is_wait)
      REQUIRES_SHARED(Locks::mutator_lock_);
  ALWAYS_INLINE static void AtraceMonitorUnlock();

  static uint32_t lock_profiling_threshold_;
  static uint32_t stack_dump_lock_profiling_threshold_;

  Mutex monitor_lock_ DEFAULT_MUTEX_ACQUIRED_AFTER;

  ConditionVariable monitor_contenders_ GUARDED_BY(monitor_lock_);

  // Number of people waiting on the condition.
  size_t num_waiters_ GUARDED_BY(monitor_lock_);

  // Which thread currently owns the lock?
  Thread* volatile owner_ GUARDED_BY(monitor_lock_);

  // Owner's recursive lock depth.
  int lock_count_ GUARDED_BY(monitor_lock_);

  // What object are we part of. This is a weak root. Do not access
  // this directly, use GetObject() to read it so it will be guarded
  // by a read barrier.
  GcRoot<mirror::Object> obj_;

  // Threads currently waiting on this monitor.
  Thread* wait_set_ GUARDED_BY(monitor_lock_);

  // Threads that were waiting on this monitor, but are now contending on it.
  Thread* wake_set_ GUARDED_BY(monitor_lock_);

  // Stored object hash code, generated lazily by GetHashCode.
  AtomicInteger hash_code_;

  // Method and dex pc where the lock owner acquired the lock, used when lock
  // sampling is enabled. locking_method_ may be null if the lock is currently
  // unlocked, or if the lock is acquired by the system when the stack is empty.
  ArtMethod* locking_method_ GUARDED_BY(monitor_lock_);
  uint32_t locking_dex_pc_ GUARDED_BY(monitor_lock_);

  // The denser encoded version of this monitor as stored in the lock word.
  MonitorId monitor_id_;

#ifdef __LP64__
  // Free list for monitor pool.
  Monitor* next_free_ GUARDED_BY(Locks::allocated_monitor_ids_lock_);
#endif

  friend class MonitorInfo;
  friend class MonitorList;
  friend class MonitorPool;
  friend class mirror::Object;
  DISALLOW_COPY_AND_ASSIGN(Monitor);
};

class MonitorList {
 public:
  MonitorList();
  ~MonitorList();

  void Add(Monitor* m) REQUIRES_SHARED(Locks::mutator_lock_) REQUIRES(!monitor_list_lock_);

  void SweepMonitorList(IsMarkedVisitor* visitor)
      REQUIRES(!monitor_list_lock_) REQUIRES_SHARED(Locks::mutator_lock_);
  void DisallowNewMonitors() REQUIRES(!monitor_list_lock_);
  void AllowNewMonitors() REQUIRES(!monitor_list_lock_);
  void BroadcastForNewMonitors() REQUIRES(!monitor_list_lock_);
  // Returns how many monitors were deflated.
  size_t DeflateMonitors() REQUIRES(!monitor_list_lock_) REQUIRES(Locks::mutator_lock_);
  size_t Size() REQUIRES(!monitor_list_lock_);

  typedef std::list<Monitor*, TrackingAllocator<Monitor*, kAllocatorTagMonitorList>> Monitors;

 private:
  // During sweeping we may free an object and on a separate thread have an object created using
  // the newly freed memory. That object may then have its lock-word inflated and a monitor created.
  // If we allow new monitor registration during sweeping this monitor may be incorrectly freed as
  // the object wasn't marked when sweeping began.
  bool allow_new_monitors_ GUARDED_BY(monitor_list_lock_);
  Mutex monitor_list_lock_ DEFAULT_MUTEX_ACQUIRED_AFTER;
  ConditionVariable monitor_add_condition_ GUARDED_BY(monitor_list_lock_);
  Monitors list_ GUARDED_BY(monitor_list_lock_);

  friend class Monitor;
  DISALLOW_COPY_AND_ASSIGN(MonitorList);
};

// Collects information about the current state of an object's monitor.
// This is very unsafe, and must only be called when all threads are suspended.
// For use only by the JDWP implementation.
class MonitorInfo {
 public:
  MonitorInfo() : owner_(nullptr), entry_count_(0) {}
  MonitorInfo(const MonitorInfo&) = default;
  MonitorInfo& operator=(const MonitorInfo&) = default;
  explicit MonitorInfo(ObjPtr<mirror::Object> o) REQUIRES(Locks::mutator_lock_);

  Thread* owner_;
  size_t entry_count_;
  std::vector<Thread*> waiters_;
};

}  // namespace art

#endif  // ART_RUNTIME_MONITOR_H_
