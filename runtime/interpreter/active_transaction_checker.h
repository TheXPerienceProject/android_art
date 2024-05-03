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
    if (runtime->GetTransaction()->WriteConstraint(obj)) {
      DCHECK(runtime->GetHeap()->ObjectIsInBootImageSpace(obj) || obj->IsClass());
      const char* base_msg = runtime->GetHeap()->ObjectIsInBootImageSpace(obj)
          ? "Can't set fields of boot image "
          : "Can't set fields of ";
      runtime->AbortTransactionAndThrowAbortError(self, base_msg + obj->PrettyTypeOf());
      return true;
    }
    return false;
  }

  static inline bool WriteValueConstraint(Thread* self, ObjPtr<mirror::Object> value)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    Runtime* runtime = Runtime::Current();
    if (runtime->GetTransaction()->WriteValueConstraint(value)) {
      DCHECK(value != nullptr);
      std::string msg = value->IsClass()
          ? "Can't store reference to class " + value->AsClass()->PrettyDescriptor()
          : "Can't store reference to instance of " + value->GetClass()->PrettyDescriptor();
      runtime->AbortTransactionAndThrowAbortError(self, msg);
      return true;
    }
    return false;
  }

  static inline bool AllocationConstraint(Thread* self, ObjPtr<mirror::Class> klass)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    if (klass->IsFinalizable()) {
      Runtime::Current()->AbortTransactionF(self,
                                            "Allocating finalizable object in transaction: %s",
                                            klass->PrettyDescriptor().c_str());
      return true;
    }
    return false;
  }
};

}  // namespace interpreter
}  // namespace art

#endif  // ART_RUNTIME_INTERPRETER_ACTIVE_TRANSACTION_CHECKER_H_
