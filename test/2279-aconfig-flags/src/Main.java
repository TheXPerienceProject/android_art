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

import com.android.libcore.Flags;

public class Main {
    public static void main(String[] args) {
        if (!isVTrunkStableFlagEnabled()) {
            throw new AssertionError(
                    "The value of com.android.libcore.v_apis flag is expected to be true.");
        }
    }

    private static boolean isVTrunkStableFlagEnabled() {
        // The Flags class definition is expected to be in core-libart.jar.
        return Flags.vApis();
    }

}
