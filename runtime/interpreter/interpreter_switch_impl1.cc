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

#include "oat/aot_class_linker.h"

namespace art HIDDEN {
namespace interpreter {

class ActiveTransactionChecker {
 public:
  static inline bool WriteConstraint(Thread* self, ObjPtr<mirror::Object> obj)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    return GetClassLinker()->TransactionWriteConstraint(self, obj);
  }

  static inline bool WriteValueConstraint(Thread* self, ObjPtr<mirror::Object> value)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    return GetClassLinker()->TransactionWriteValueConstraint(self, value);
  }

  static inline bool ReadConstraint(Thread* self, ObjPtr<mirror::Object> obj)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    return GetClassLinker()->TransactionReadConstraint(self, obj);
  }

  static inline bool AllocationConstraint(Thread* self, ObjPtr<mirror::Class> klass)
      REQUIRES_SHARED(Locks::mutator_lock_) {
    return GetClassLinker()->TransactionAllocationConstraint(self, klass);
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

// TODO: Use ObjPtr here.
template<typename T>
static void RecordArrayElementsInTransactionImpl(ObjPtr<mirror::PrimitiveArray<T>> array,
                                                 int32_t count)
    REQUIRES_SHARED(Locks::mutator_lock_) {
  Runtime* runtime = Runtime::Current();
  for (int32_t i = 0; i < count; ++i) {
    runtime->GetClassLinker()->RecordWriteArray(array.Ptr(), i, array->GetWithoutChecks(i));
  }
}

void ActiveTransactionChecker::RecordArrayElementsInTransaction(ObjPtr<mirror::Object> array,
                                                                int32_t count) {
  DCHECK(Runtime::Current()->IsActiveTransaction());
  if (array == nullptr) {
    return;  // The interpreter shall throw NPE.
  }
  DCHECK(array->IsArrayInstance());
  DCHECK_LE(count, array->AsArray()->GetLength());
  // No read barrier is needed for reading a chain of constant references
  // for reading a constant primitive value, see `ReadBarrierOption`.
  Primitive::Type primitive_component_type =
      array->GetClass<kDefaultVerifyFlags, kWithoutReadBarrier>()
          ->GetComponentType<kDefaultVerifyFlags, kWithoutReadBarrier>()->GetPrimitiveType();
  switch (primitive_component_type) {
    case Primitive::kPrimBoolean:
      RecordArrayElementsInTransactionImpl(array->AsBooleanArray(), count);
      break;
    case Primitive::kPrimByte:
      RecordArrayElementsInTransactionImpl(array->AsByteArray(), count);
      break;
    case Primitive::kPrimChar:
      RecordArrayElementsInTransactionImpl(array->AsCharArray(), count);
      break;
    case Primitive::kPrimShort:
      RecordArrayElementsInTransactionImpl(array->AsShortArray(), count);
      break;
    case Primitive::kPrimInt:
      RecordArrayElementsInTransactionImpl(array->AsIntArray(), count);
      break;
    case Primitive::kPrimFloat:
      RecordArrayElementsInTransactionImpl(array->AsFloatArray(), count);
      break;
    case Primitive::kPrimLong:
      RecordArrayElementsInTransactionImpl(array->AsLongArray(), count);
      break;
    case Primitive::kPrimDouble:
      RecordArrayElementsInTransactionImpl(array->AsDoubleArray(), count);
      break;
    default:
      LOG(FATAL) << "Unsupported primitive type " << primitive_component_type
                 << " in fill-array-data";
      UNREACHABLE();
  }
}

// Explicit definition of ExecuteSwitchImplCpp.
template
void ExecuteSwitchImplCpp<true>(SwitchImplContext* ctx);

}  // namespace interpreter
}  // namespace art
