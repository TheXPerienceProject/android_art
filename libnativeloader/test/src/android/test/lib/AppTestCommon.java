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

package android.test.lib;

import android.test.productsharedlib.ProductSharedLib;
import android.test.systemextsharedlib.SystemExtSharedLib;
import android.test.systemsharedlib.SystemSharedLib;
import android.test.vendorsharedlib.VendorSharedLib;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public abstract class AppTestCommon {
    public enum AppLocation { DATA, SYSTEM, PRODUCT, VENDOR }

    public abstract AppLocation getAppLocation();

    // Loading private libs using absolute paths through shared libs should only
    // depend on the location of the shared lib, so these tests are shared for
    // all apps, regardless of location.

    // There's an exception for system apps. For them LibraryNamespaces::Create
    // gets called with is_shared=true. That means they don't set up separate
    // "unbundled" namespaces for the shared libs in product and vendor, so
    // ProductSharedLib and VendorSharedLib can still load private system libs
    // through their classloader namespaces, but not the private libs in the
    // same partition as themselves.
    private boolean isSharedSystemApp() {
        return getAppLocation() == AppLocation.SYSTEM;
    }

    @Test
    public void testLoadPrivateLibrariesViaSystemSharedLibWithAbsolutePaths() {
        if (getAppLocation() == AppLocation.SYSTEM) {
            SystemSharedLib.load(TestUtils.libPath("/system", "system_private7"));
            SystemSharedLib.load(TestUtils.libPath("/system_ext", "systemext_private7"));
        } else {
            // Cannot load private system libs because there is no provision in
            // LibraryNamespaces::Create to create an "unbundled system apk" for
            // shared system libs based on their location. Hence SystemSharedLib
            // gets a classloader namespace as an "other apk", with the same
            // library_path as the app.
            TestUtils.assertLibraryInaccessible(() -> {
                SystemSharedLib.load(TestUtils.libPath("/system", "system_private7"));
            });
            TestUtils.assertLibraryInaccessible(() -> {
                SystemSharedLib.load(TestUtils.libPath("/system_ext", "systemext_private7"));
            });
        }

        TestUtils.assertLibraryInaccessible(
                () -> { SystemSharedLib.load(TestUtils.libPath("/product", "product_private7")); });

        TestUtils.assertLibraryInaccessible(
                () -> { SystemSharedLib.load(TestUtils.libPath("/vendor", "vendor_private7")); });
    }

    @Test
    public void testLoadPrivateLibrariesViaSystemExtSharedLibWithAbsolutePaths() {
        if (getAppLocation() == AppLocation.SYSTEM) {
            SystemExtSharedLib.load(TestUtils.libPath("/system", "system_private8"));
            SystemExtSharedLib.load(TestUtils.libPath("/system_ext", "systemext_private8"));
        } else {
            // See comment in the corresponding test for SystemSharedLib above.
            TestUtils.assertLibraryInaccessible(() -> {
                SystemExtSharedLib.load(TestUtils.libPath("/system", "system_private8"));
            });
            TestUtils.assertLibraryInaccessible(() -> {
                SystemExtSharedLib.load(TestUtils.libPath("/system_ext", "systemext_private8"));
            });
        }

        TestUtils.assertLibraryInaccessible(() -> {
            SystemExtSharedLib.load(TestUtils.libPath("/product", "product_private8"));
        });

        TestUtils.assertLibraryInaccessible(() -> {
            SystemExtSharedLib.load(TestUtils.libPath("/vendor", "vendor_private8"));
        });
    }

    @Test
    public void testLoadPrivateLibrariesViaProductSharedLibWithAbsolutePaths() {
        if (isSharedSystemApp()) {
            ProductSharedLib.load(TestUtils.libPath("/system", "system_private9"));
            ProductSharedLib.load(TestUtils.libPath("/system_ext", "systemext_private9"));
        } else {
            TestUtils.assertLibraryInaccessible(() -> {
                ProductSharedLib.load(TestUtils.libPath("/system", "system_private9"));
            });
            TestUtils.assertLibraryInaccessible(() -> {
                ProductSharedLib.load(TestUtils.libPath("/system_ext", "systemext_private9"));
            });
        }

        if (!isSharedSystemApp()) {
            ProductSharedLib.load(TestUtils.libPath("/product", "product_private9"));
        } else {
            TestUtils.assertLibraryInaccessible(() -> {
                ProductSharedLib.load(TestUtils.libPath("/product", "product_private9"));
            });
        }

        TestUtils.assertLibraryInaccessible(
                () -> { ProductSharedLib.load(TestUtils.libPath("/vendor", "vendor_private9")); });
    }

    @Test
    public void testLoadPrivateLibrariesViaVendorSharedLibWithAbsolutePaths() {
        if (isSharedSystemApp()) {
            VendorSharedLib.load(TestUtils.libPath("/system", "system_private10"));
            VendorSharedLib.load(TestUtils.libPath("/system_ext", "systemext_private10"));
        } else {
            TestUtils.assertLibraryInaccessible(() -> {
                VendorSharedLib.load(TestUtils.libPath("/system", "system_private10"));
            });
            TestUtils.assertLibraryInaccessible(() -> {
                VendorSharedLib.load(TestUtils.libPath("/system_ext", "systemext_private10"));
            });
        }

        TestUtils.assertLibraryInaccessible(() -> {
            VendorSharedLib.load(TestUtils.libPath("/product", "product_private10"));
        });

        if (!isSharedSystemApp()) {
            VendorSharedLib.load(TestUtils.libPath("/vendor", "vendor_private10"));
        } else {
            TestUtils.assertLibraryInaccessible(() -> {
                VendorSharedLib.load(TestUtils.libPath("/vendor", "vendor_private10"));
            });
        }
    }
}
