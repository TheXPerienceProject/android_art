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

 import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Main {

    private static String STATUS = "";

    public static void main(String[] args) throws Throwable {
      $noinline$testNoArgsCalls();
      $noinline$testMethodHandleFromOtherDex();
      Multi.$noinline$testMHFromMain(OPTIONAL_GET);
      $noinline$testWithArgs();
      $noinline$nullchecks();
    }

    private static void $noinline$nullchecks() throws Throwable {
      try {
        VOID_METHOD.invokeExact((A) null);
        unreachable("Receiver is null, should throw NPE");
      } catch (NullPointerException expected) {}

      try {
        VOID_METHOD.invokeExact((Main) null);
        unreachable("Should throw WMTE: input is of wrong type");
      } catch (WrongMethodTypeException expected) {}

      try {
        MethodHandle mh = null;
        mh.invokeExact();
        unreachable("MethodHandle object is null, should throw NPE");
      } catch (NullPointerException expected) {}
    }

    private static void $noinline$testMethodHandleFromOtherDex() throws Throwable {
      MethodHandle mh = Multi.$noinline$getMethodHandle();
      Optional<String> nonEmpty = Optional.<String>of("hello");
      Object returnedObject = mh.invokeExact(nonEmpty);
      assertEquals("hello", returnedObject);

      try {
        mh.invokeExact(nonEmpty);
        unreachable("mh.type() is (Optional)Object, but callsite is (Optional)V");
      } catch (WrongMethodTypeException expected) {}
    }

    private static void $noinline$testNoArgsCalls() throws Throwable {
      VOID_METHOD.invokeExact(new A());
      assertEquals("A.voidMethod", STATUS);

      int returnedInt = (int) RETURN_INT.invokeExact(new A());
      assertEquals(42, returnedInt);

      double returnedDouble = (double) RETURN_DOUBLE.invokeExact(new A());
      assertEquals(42.0d, returnedDouble);

      try {
        INTERFACE_DEFAULT_METHOD.invokeExact(new A());
        unreachable("MethodHandle's type is (Main$I)V, but callsite is (Main$A)V");
      } catch (WrongMethodTypeException expected) {}

      INTERFACE_DEFAULT_METHOD.invokeExact((I) new A());
      assertEquals("I.defaultMethod", STATUS);

      OVERWRITTEN_INTERFACE_DEFAULT_METHOD.invokeExact((I) new A());
      assertEquals("A.overrideMe", STATUS);

      assertEquals("boo", (String) PRIVATE_INTERFACE_METHOD.invokeExact((I) new A()));

      int privateIntA = (int) A_PRIVATE_RETURN_INT.invokeExact(new A());
      assertEquals(1042, privateIntA);

      int privateIntB = (int) B_PRIVATE_RETURN_INT.invokeExact(new B());
      assertEquals(9999, privateIntB);

      privateIntB = (int) B_PRIVATE_RETURN_INT.invokeExact((B) new A());
      assertEquals(9999, privateIntB);

      try {
        EXCEPTION_THROWING_METHOD.invokeExact(new A());
        unreachable("Target method always throws");
      } catch (MyRuntimeException expected) {
        assertEquals("A.throwException", STATUS);
      }

      try {
        RETURN_INT.invokeExact(new A());
        unreachable("MethodHandle's type is (Main$A)I, but callsite type is (Main$A)V");
      } catch (WrongMethodTypeException expected) {}

      String returnedString = (String) STATIC_METHOD.invokeExact(new A());
      assertEquals("staticMethod", returnedString);
    }

    private static void $noinline$testWithArgs() throws Throwable {
      int sum = (int) SUM_I.invokeExact(new Sums(), 1);
      assertEquals(1, sum);

      sum = (int) SUM_2I.invokeExact(new Sums(), 1, 2);
      assertEquals(3, sum);

      sum = (int) SUM_3I.invokeExact(new Sums(), 1, 2, 3);
      assertEquals(6, sum);

      sum = (int) SUM_4I.invokeExact(new Sums(), 1, 2, 3, 4);
      assertEquals(10, sum);

      sum = (int) SUM_5I.invokeExact(new Sums(), 1, 2, 3, 4, 5);
      assertEquals(15, sum);

      sum = (int) SUM_6I.invokeExact(new Sums(), 1, 2, 3, 4, 5, 6);
      assertEquals(21, sum);

      sum = (int) SUM_7I.invokeExact(new Sums(), 1, 2, 3, 4, 5, 6, 7);
      assertEquals(28, sum);

      sum = (int) SUM_8I.invokeExact(new Sums(), 1, 2, 3, 4, 5, 6, 7, 8);
      assertEquals(36, sum);

      sum = (int) SUM_9I.invokeExact(new Sums(), 1, 2, 3, 4, 5, 6, 7, 8, 9);
      assertEquals(45, sum);

      sum = (int) SUM_10I.invokeExact(new Sums(), 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
      assertEquals(55, sum);

      long lsum = (long) SUM_IJ.invokeExact(new Sums(), 1, 2L);
      assertEquals(3L, lsum);

      lsum = (long) SUM_2IJ.invokeExact(new Sums(), 1, 2L, 3, 4L);
      assertEquals(10L, lsum);

      lsum = (long) SUM_3IJ.invokeExact(new Sums(), 1, 2L, 3, 4L, 5, 6L);
      assertEquals(21L, lsum);

      lsum = (long) SUM_4IJ.invokeExact(new Sums(), 1, 2L, 3, 4L, 5, 6L, 7, 8L);
      assertEquals(36L, lsum);

      lsum = (long) SUM_5IJ.invokeExact(new Sums(), 1, 2L, 3, 4L, 5, 6L, 7, 8L, 9, 10L);
      assertEquals(55L, lsum);
    }

    private static void assertEquals(Object expected, Object actual) {
      if (!Objects.equals(expected, actual)) {
        throw new AssertionError("Expected: " + expected + ", got: " + actual);
      }
    }

    private static void assertEquals(int expected, int actual) {
      if (expected != actual) {
        throw new AssertionError("Expected: " + expected + ", got: " + actual);
      }
    }

    private static void assertEquals(long expected, long actual) {
      if (expected != actual) {
        throw new AssertionError("Expected: " + expected + ", got: " + actual);
      }
    }

    private static void assertEquals(double expected, double actual) {
      if (expected != actual) {
        throw new AssertionError("Expected: " + expected + ", got: " + actual);
      }
    }

    private static void unreachable(String msg) {
      throw new AssertionError("Unexpectedly reached this point, but shouldn't: " + msg);
    }

    private static final MethodHandle VOID_METHOD;
    private static final MethodHandle RETURN_DOUBLE;
    private static final MethodHandle RETURN_INT;
    private static final MethodHandle PRIVATE_INTERFACE_METHOD;
    private static final MethodHandle B_PRIVATE_RETURN_INT;
    private static final MethodHandle A_PRIVATE_RETURN_INT;
    private static final MethodHandle STATIC_METHOD;
    private static final MethodHandle EXCEPTION_THROWING_METHOD;
    private static final MethodHandle INTERFACE_DEFAULT_METHOD;
    private static final MethodHandle OVERWRITTEN_INTERFACE_DEFAULT_METHOD;
    private static final MethodHandle OPTIONAL_GET;

    private static final MethodHandle SUM_I;
    private static final MethodHandle SUM_2I;
    private static final MethodHandle SUM_3I;
    private static final MethodHandle SUM_4I;
    private static final MethodHandle SUM_5I;
    private static final MethodHandle SUM_6I;
    private static final MethodHandle SUM_7I;
    private static final MethodHandle SUM_8I;
    private static final MethodHandle SUM_9I;
    private static final MethodHandle SUM_10I;

    private static final MethodHandle SUM_IJ;
    private static final MethodHandle SUM_2IJ;
    private static final MethodHandle SUM_3IJ;
    private static final MethodHandle SUM_4IJ;
    private static final MethodHandle SUM_5IJ;

    static {
      try {
        VOID_METHOD = MethodHandles.lookup()
            .findVirtual(A.class, "voidMethod", methodType(void.class));
        RETURN_DOUBLE = MethodHandles.lookup()
            .findVirtual(A.class, "returnDouble", methodType(double.class));
        RETURN_INT = MethodHandles.lookup()
            .findVirtual(A.class, "returnInt", methodType(int.class));
        PRIVATE_INTERFACE_METHOD = MethodHandles.privateLookupIn(I.class, MethodHandles.lookup())
            .findVirtual(I.class, "innerPrivateMethod", methodType(String.class));
        A_PRIVATE_RETURN_INT = MethodHandles.privateLookupIn(A.class, MethodHandles.lookup())
            .findVirtual(A.class, "privateReturnInt", methodType(int.class));
        B_PRIVATE_RETURN_INT = MethodHandles.privateLookupIn(B.class, MethodHandles.lookup())
            .findVirtual(B.class, "privateReturnInt", methodType(int.class));
        STATIC_METHOD = MethodHandles.lookup()
            .findStatic(A.class, "staticMethod", methodType(String.class, A.class));
        EXCEPTION_THROWING_METHOD = MethodHandles.lookup()
            .findVirtual(A.class, "throwException", methodType(void.class));
        INTERFACE_DEFAULT_METHOD = MethodHandles.lookup()
            .findVirtual(I.class, "defaultMethod", methodType(void.class));
        OVERWRITTEN_INTERFACE_DEFAULT_METHOD = MethodHandles.lookup()
            .findVirtual(I.class, "overrideMe", methodType(void.class));
        OPTIONAL_GET = MethodHandles.lookup()
            .findVirtual(Optional.class, "get", methodType(Object.class));

        SUM_I  = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(1, int.class)));
        SUM_2I = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(2, int.class)));
        SUM_3I = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(3, int.class)));
        SUM_4I  = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(4, int.class)));
        SUM_5I  = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(5, int.class)));
        SUM_6I  = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(6, int.class)));
        SUM_7I  = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(7, int.class)));
        SUM_8I  = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(8, int.class)));
        SUM_9I  = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(9, int.class)));
        SUM_10I = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(int.class, repeat(10, int.class)));

        SUM_IJ  = MethodHandles.lookup()
            .findVirtual(Sums.class, "sum", methodType(long.class, int.class, long.class));
        SUM_2IJ  = MethodHandles.lookup()
            .findVirtual(Sums.class,
                         "sum",
                         methodType(long.class, repeat(2, int.class, long.class)));
        SUM_3IJ  = MethodHandles.lookup()
            .findVirtual(Sums.class,
                         "sum",
                         methodType(long.class, repeat(3, int.class, long.class)));
        SUM_4IJ  = MethodHandles.lookup()
            .findVirtual(Sums.class,
                         "sum",
                         methodType(long.class, repeat(4, int.class, long.class)));
        SUM_5IJ  = MethodHandles.lookup()
            .findVirtual(Sums.class,
                         "sum",
                         methodType(long.class, repeat(5, int.class, long.class)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?>[] repeat(int times, Class<?> clazz) {
      Class<?>[] classes = new Class<?>[times];
      Arrays.fill(classes, clazz);
      return classes;
    }

    private static Class<?>[] repeat(int times, Class<?> first, Class<?> second) {
      Class<?>[] classes = new Class<?>[times * 2];
      for (int i = 0; i < 2 * times;) {
        classes[i++] = first;
        classes[i++] = second;
      }
      return classes;
    }

    static interface I {
      public default void defaultMethod() {
        STATUS = "I.defaultMethod";
      }

      public default void overrideMe() {
        throw new RuntimeException("should be overwritten");
      }

      private String innerPrivateMethod() {
        return "boo";
      }
    }

    static class MyRuntimeException extends RuntimeException {}

    static class A extends B implements I {
        public int field;
        public void voidMethod() {
          STATUS = "A.voidMethod";
        }

        @Override
        public void overrideMe() {
          STATUS = "A.overrideMe";
        }

        public void throwException() {
          STATUS = "A.throwException";
          throw new MyRuntimeException();
        }

        public double returnDouble() {
          return 42.0d;
        }

        public int returnInt() {
          return 42;
        }

        private int privateReturnInt() {
          return 1042;
        }

        public static String staticMethod(A a) {
          return "staticMethod";
        }

        public static double staticMethod() {
          return 41.0d;
        }
    }

    static class B {
      private int privateReturnInt() {
        return 9999;
      }
    }

    static class Sums {
        public int sum(int a) {
          return a;
        }

        public int sum(int a1, int a2) {
          return a1 + a2;
        }

        public int sum(int a1, int a2, int a3) {
          return a1 + a2 + a3;
        }

        public int sum(int a1, int a2, int a3, int a4) {
          return a1 + a2 + a3 + a4;
        }

        public int sum(int a1, int a2, int a3, int a4, int a5) {
          return a1 + a2 + a3 + a4 + a5;
        }

        public int sum(int a1, int a2, int a3, int a4, int a5, int a6) {
          return a1 + a2 + a3 + a4 + a5 + a6;
        }

        public int sum(int a1, int a2, int a3, int a4, int a5, int a6, int a7) {
          return a1 + a2 + a3 + a4 + a5 + a6 + a7;
        }

        public int sum(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
          return a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8;
        }

        public int sum(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8, int a9) {
          return a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8 + a9;
        }

        public int sum(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8, int a9,
                       int a10) {
          return a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8 + a9 + a10;
        }

        public long sum(int a1, long a2) {
          return a1 + a2;
        }

        public long sum(int a1, long a2, int a3, long a4) {
          return a1 + a2 + a3 + a4;
        }

        public long sum(int a1, long a2, int a3, long a4, int a5, long a6) {
          return a1 + a2 + a3 + a4 + a5 + a6;
        }

        public long sum(int a1, long a2, int a3, long a4, int a5, long a6, int a7, long a8) {
          return a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8;
        }

        public long sum(int a1, long a2, int a3, long a4, int a5, long a6, int a7, long a8, int a9,
                        long a10) {
          return a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8 + a9 + a10;
        }
    }
}
