/*
 * Copyright (C) 2024 The Android Open Source Project
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

#ifndef ART_RUNTIME_INTERPRETER_ACTIVE_TRANSACTION_CHECKER_H_
#define ART_RUNTIME_INTERPRETER_ACTIVE_TRANSACTION_CHECKER_H_

#include "oat/aot_class_linker.h"
#include "base/casts.h"
#include "base/macros.h"
#include "gc/heap.h"
#include "mirror/object-inl.h"
#include "runtime.h"
#include "transaction.h"

namespace art HIDDEN {
namespace interpreter {

class ActiveTransactionChecker {
 public:
  static inline bool WriteConstraint(Thread* self, ObjPtr<mirror::Object> obj)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    Runtime* runtime = Runtime::Current();
    AotClassLinker* class_linker = GetClassLinker();
    if (class_linker->GetTransaction()->WriteConstraint(obj)) {
      DCHECK(runtime->GetHeap()->ObjectIsInBootImageSpace(obj) || obj->IsClass());
      const char* extra = runtime->GetHeap()->ObjectIsInBootImageSpace(obj) ? "boot image " : "";
      class_linker->AbortTransactionF(
          self, "Can't set fields of %s%s", extra, obj->PrettyTypeOf().c_str());
      return true;
    }
    return false;
  }

  static inline bool WriteValueConstraint(Thread* self, ObjPtr<mirror::Object> value)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    AotClassLinker* class_linker = GetClassLinker();
    if (class_linker->GetTransaction()->WriteValueConstraint(value)) {
      DCHECK(value != nullptr);
      const char* description = value->IsClass() ? "class" : "instance of";
      ObjPtr<mirror::Class> klass = value->IsClass() ? value->AsClass() : value->GetClass();
      class_linker->AbortTransactionF(
          self, "Can't store reference to %s %s", description, klass->PrettyDescriptor().c_str());
      return true;
    }
    return false;
  }

  static inline bool ReadConstraint(Thread* self, ObjPtr<mirror::Object> obj)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    DCHECK(obj->IsClass());
    AotClassLinker* class_linker = GetClassLinker();
    if (class_linker->GetTransaction()->ReadConstraint(obj)) {
      class_linker->AbortTransactionF(
          self,
          "Can't read static fields of %s since it does not belong to clinit's class.",
          obj->PrettyTypeOf().c_str());
      return true;
    }
    return false;
  }

  static inline bool AllocationConstraint(Thread* self, ObjPtr<mirror::Class> klass)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    if (klass->IsFinalizable()) {
      GetClassLinker()->AbortTransactionF(self,
                                          "Allocating finalizable object in transaction: %s",
                                          klass->PrettyDescriptor().c_str());
      return true;
    }
    return false;
  }

  static inline bool IsTransactionAborted() {
    return GetClassLinker()->IsTransactionAborted();
  }

  static void RecordArrayElementsInTransaction(ObjPtr<mirror::Object> array, int32_t count)
      REQUIRES_SHARED(Locks::mutator_lock_);

 private:
  static AotClassLinker* GetClassLinker() {
    return down_cast<AotClassLinker*>(Runtime::Current()->GetClassLinker());
  }
};

}  // namespace interpreter
}  // namespace art

#endif  // ART_RUNTIME_INTERPRETER_ACTIVE_TRANSACTION_CHECKER_H_
