/*
 * Copyright (C) 2018 The Android Open Source Project
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

// The interpreter function takes considerable time to compile and link.
// We compile the explicit definitions separately to speed up the build.

#include "interpreter_switch_impl-inl.h"

namespace art HIDDEN {
namespace interpreter {

// Define the helper class that does not do any transaction checks.
class InactiveTransactionChecker {
 public:
  ALWAYS_INLINE static bool WriteConstraint([[maybe_unused]] Thread* self,
                                            [[maybe_unused]] ObjPtr<mirror::Object> obj)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    return false;
  }

  ALWAYS_INLINE static bool WriteValueConstraint([[maybe_unused]] Thread* self,
                                                 [[maybe_unused]] ObjPtr<mirror::Object> value)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    return false;
  }

  ALWAYS_INLINE static bool ReadConstraint([[maybe_unused]] Thread* self,
                                           [[maybe_unused]] ObjPtr<mirror::Object> value)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    return false;
  }

  ALWAYS_INLINE static bool AllocationConstraint([[maybe_unused]] Thread* self,
                                                 [[maybe_unused]] ObjPtr<mirror::Class> klass)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    return false;
  }

  ALWAYS_INLINE static bool IsTransactionAborted() {
    return false;
  }

  static void RecordArrayElementsInTransaction([[maybe_unused]] ObjPtr<mirror::Object> array,
                                               [[maybe_unused]] int32_t count)
      REQUIRES_SHARED(Locks::mutator_lock_) {}
};

// Explicit definition of ExecuteSwitchImplCpp.
template HOT_ATTR
void ExecuteSwitchImplCpp<false>(SwitchImplContext* ctx);

}  // namespace interpreter
}  // namespace art
