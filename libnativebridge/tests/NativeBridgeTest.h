/*
 * Copyright (C) 2014 The Android Open Source Project
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

#ifndef ART_LIBNATIVEBRIDGE_TESTS_NATIVEBRIDGETEST_H_
#define ART_LIBNATIVEBRIDGE_TESTS_NATIVEBRIDGETEST_H_

#define LOG_TAG "NativeBridge_test"

#include <android-base/file.h>
#include <gtest/gtest.h>
#include <nativebridge/native_bridge.h>

#include <string>

constexpr const char* kNativeBridgeLibrary = "libnativebridge-test-case.so";
constexpr const char* kNativeBridgeLibrary2 = "libnativebridge2-test-case.so";
constexpr const char* kNativeBridgeLibrary3 = "libnativebridge3-test-case.so";
constexpr const char* kNativeBridgeLibrary6 = "libnativebridge6-test-case.so";
constexpr const char* kNativeBridgeLibrary7 = "libnativebridge7-test-case.so";

namespace android {

class NativeBridgeTest : public testing::Test {
 protected:
  NativeBridgeTest() : tempDir() {
    appDataDir_ = std::string(tempDir.path);
    codeCache_ = appDataDir_ + "/code_cache";
    codeCacheStatFail_ = codeCache_ + "/temp";
  }

  const char* appDataDir() { return appDataDir_.c_str(); }

  const char* codeCache() { return codeCache_.c_str(); }

  const char* codeCacheStatFail() { return codeCacheStatFail_.c_str(); }

  TemporaryDir tempDir;
  std::string appDataDir_;
  std::string codeCache_;
  std::string codeCacheStatFail_;
};

};  // namespace android

#endif  // ART_LIBNATIVEBRIDGE_TESTS_NATIVEBRIDGETEST_H_
