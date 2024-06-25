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

public class Main {
  public static void main(String[] args) {
    test_Integer_compareUnsigned_no_fold();
    test_Long_compareUnsigned_no_fold();
    test_Integer_compareUnsigned();
    test_Long_compareUnsigned();
    test_Integer_divideUnsigned_no_fold();
    test_Long_divideUnsigned_no_fold();
    test_Integer_divideUnsigned();
    test_Long_divideUnsigned();
    test_Integer_remainderUnsigned_no_fold();
    test_Long_remainderUnsigned_no_fold();
    test_Integer_remainderUnsigned();
    test_Long_remainderUnsigned();
  }

  public static int $noinline$cmpUnsignedInt(int a, int b) {
    return Integer.compareUnsigned(a, b);
  }

  public static void test_Integer_compareUnsigned_no_fold() {
    assertEquals($noinline$cmpUnsignedInt(100, 100), 0);
    assertEquals($noinline$cmpUnsignedInt(100, 1), 1);
    assertEquals($noinline$cmpUnsignedInt(1, 100), -1);
    assertEquals($noinline$cmpUnsignedInt(-2, 2), 1);
    assertEquals($noinline$cmpUnsignedInt(2, -2), -1);
    assertEquals($noinline$cmpUnsignedInt(Integer.MAX_VALUE, Integer.MIN_VALUE), -1);
    assertEquals($noinline$cmpUnsignedInt(Integer.MIN_VALUE, Integer.MAX_VALUE), 1);
    assertEquals($noinline$cmpUnsignedInt(Integer.MAX_VALUE, -1), -1);
    assertEquals($noinline$cmpUnsignedInt(-1, Integer.MAX_VALUE), 1);
    assertEquals($noinline$cmpUnsignedInt(0, 0), 0);
  }

  public static int $noinline$cmpUnsignedLong(long a, long b) {
    return Long.compareUnsigned(a, b);
  }

  public static void test_Long_compareUnsigned_no_fold() {
    assertEquals($noinline$cmpUnsignedLong(100L, 100L), 0);
    assertEquals($noinline$cmpUnsignedLong(100L, 1L), 1);
    assertEquals($noinline$cmpUnsignedLong(1L, 100L), -1);
    assertEquals($noinline$cmpUnsignedLong(-2L, 2L), 1);
    assertEquals($noinline$cmpUnsignedLong(2L, -2L), -1);
    assertEquals($noinline$cmpUnsignedLong(Long.MAX_VALUE, Long.MIN_VALUE), -1);
    assertEquals($noinline$cmpUnsignedLong(Long.MIN_VALUE, Long.MAX_VALUE), 1);
    assertEquals($noinline$cmpUnsignedLong(Long.MAX_VALUE, -1L), -1);
    assertEquals($noinline$cmpUnsignedLong(-1L, Long.MAX_VALUE), 1);
    assertEquals($noinline$cmpUnsignedLong(0L, 0L), 0);
  }

  public static void test_Integer_compareUnsigned() {
    assertEquals(Integer.compareUnsigned(100, 100), 0);
    assertEquals(Integer.compareUnsigned(100, 1), 1);
    assertEquals(Integer.compareUnsigned(1, 100), -1);
    assertEquals(Integer.compareUnsigned(-2, 2), 1);
    assertEquals(Integer.compareUnsigned(2, -2), -1);
    assertEquals(Integer.compareUnsigned(Integer.MAX_VALUE, Integer.MIN_VALUE), -1);
    assertEquals(Integer.compareUnsigned(Integer.MIN_VALUE, Integer.MAX_VALUE), 1);
    assertEquals(Integer.compareUnsigned(Integer.MAX_VALUE, -1), -1);
    assertEquals(Integer.compareUnsigned(-1, Integer.MAX_VALUE), 1);
    assertEquals(Integer.compareUnsigned(0, 0), 0);
  }

  public static void test_Long_compareUnsigned() {
    assertEquals(Long.compareUnsigned(100L, 100L), 0);
    assertEquals(Long.compareUnsigned(100L, 1L), 1);
    assertEquals(Long.compareUnsigned(1L, 100L), -1);
    assertEquals(Long.compareUnsigned(-2L, 2L), 1);
    assertEquals(Long.compareUnsigned(2L, -2L), -1);
    assertEquals(Long.compareUnsigned(Long.MAX_VALUE, Long.MIN_VALUE), -1);
    assertEquals(Long.compareUnsigned(Long.MIN_VALUE, Long.MAX_VALUE), 1);
    assertEquals(Long.compareUnsigned(Long.MAX_VALUE, -1L), -1);
    assertEquals(Long.compareUnsigned(-1L, Long.MAX_VALUE), 1);
    assertEquals(Long.compareUnsigned(0L, 0L), 0);
  }

  public static int $noinline$divideUnsignedInt(int a, int b) {
    return Integer.divideUnsigned(a, b);
  }

  public static void test_Integer_divideUnsigned_no_fold() {
    assertEquals($noinline$divideUnsignedInt(100, 10), 10);
    assertEquals($noinline$divideUnsignedInt(100, 1), 100);
    assertEquals($noinline$divideUnsignedInt(1024, 128), 8);
    assertEquals($noinline$divideUnsignedInt(12345678, 264), 46763);
    assertEquals($noinline$divideUnsignedInt(13, 5), 2);
    assertEquals($noinline$divideUnsignedInt(-2, 2), Integer.MAX_VALUE);
    assertEquals($noinline$divideUnsignedInt(-1, 2), Integer.MAX_VALUE);
    assertEquals($noinline$divideUnsignedInt(100000, -1), 0);
    assertEquals($noinline$divideUnsignedInt(Integer.MAX_VALUE, -1), 0);
    assertEquals($noinline$divideUnsignedInt(-2, -1), 0);
    assertEquals($noinline$divideUnsignedInt(-1, -2), 1);
    assertEquals($noinline$divideUnsignedInt(-173448, 13), 330368757);
    assertEquals($noinline$divideUnsignedInt(Integer.MIN_VALUE, 2), (1 << 30));
    assertEquals($noinline$divideUnsignedInt(-1, Integer.MIN_VALUE), 1);
    assertEquals($noinline$divideUnsignedInt(Integer.MAX_VALUE, Integer.MIN_VALUE), 0);
    assertEquals($noinline$divideUnsignedInt(Integer.MIN_VALUE, Integer.MAX_VALUE), 1);

    try {
      $noinline$divideUnsignedInt(1, 0);
      throw new Error("Unreachable");
    } catch (ArithmeticException expected) {
    }
  }

  public static long $noinline$divideUnsignedLong(long a, long b) {
    return Long.divideUnsigned(a, b);
  }

  private static final long BIG_LONG_VALUE = 739287620162442240L;

  public static void test_Long_divideUnsigned_no_fold() {
    assertEquals($noinline$divideUnsignedLong(100L, 10L), 10L);
    assertEquals($noinline$divideUnsignedLong(100L, 1L), 100L);
    assertEquals($noinline$divideUnsignedLong(1024L, 128L), 8L);
    assertEquals($noinline$divideUnsignedLong(12345678L, 264L), 46763L);
    assertEquals($noinline$divideUnsignedLong(13L, 5L), 2L);
    assertEquals($noinline$divideUnsignedLong(-2L, 2L), Long.MAX_VALUE);
    assertEquals($noinline$divideUnsignedLong(-1L, 2L), Long.MAX_VALUE);
    assertEquals($noinline$divideUnsignedLong(100000L, -1L), 0L);
    assertEquals($noinline$divideUnsignedLong(Long.MAX_VALUE, -1L), 0L);
    assertEquals($noinline$divideUnsignedLong(-2L, -1L), 0L);
    assertEquals($noinline$divideUnsignedLong(-1L, -2L), 1L);
    assertEquals($noinline$divideUnsignedLong(-173448L, 13L), 1418980313362259859L);
    assertEquals($noinline$divideUnsignedLong(Long.MIN_VALUE, 2L), (1L << 62));
    assertEquals($noinline$divideUnsignedLong(-1L, Long.MIN_VALUE), 1L);
    assertEquals($noinline$divideUnsignedLong(Long.MAX_VALUE, Long.MIN_VALUE), 0L);
    assertEquals($noinline$divideUnsignedLong(Long.MIN_VALUE, Long.MAX_VALUE), 1L);
    assertEquals($noinline$divideUnsignedLong(Long.MAX_VALUE, 1L), Long.MAX_VALUE);
    assertEquals($noinline$divideUnsignedLong(Long.MIN_VALUE, 1L), Long.MIN_VALUE);
    assertEquals($noinline$divideUnsignedLong(BIG_LONG_VALUE, BIG_LONG_VALUE), 1L);
    assertEquals($noinline$divideUnsignedLong(BIG_LONG_VALUE, 1L), BIG_LONG_VALUE);
    assertEquals($noinline$divideUnsignedLong(BIG_LONG_VALUE, 1024L), 721960566564885L);
    assertEquals($noinline$divideUnsignedLong(BIG_LONG_VALUE, 0x1FFFFFFFFL), 86064406L);

    try {
      $noinline$divideUnsignedLong(1L, 0L);
      throw new Error("Unreachable");
    } catch (ArithmeticException expected) {
    }
  }

  public static void test_Integer_divideUnsigned() {
    assertEquals(Integer.divideUnsigned(100, 10), 10);
    assertEquals(Integer.divideUnsigned(100, 1), 100);
    assertEquals(Integer.divideUnsigned(1024, 128), 8);
    assertEquals(Integer.divideUnsigned(12345678, 264), 46763);
    assertEquals(Integer.divideUnsigned(13, 5), 2);
    assertEquals(Integer.divideUnsigned(-2, 2), Integer.MAX_VALUE);
    assertEquals(Integer.divideUnsigned(-1, 2), Integer.MAX_VALUE);
    assertEquals(Integer.divideUnsigned(100000, -1), 0);
    assertEquals(Integer.divideUnsigned(Integer.MAX_VALUE, -1), 0);
    assertEquals(Integer.divideUnsigned(-2, -1), 0);
    assertEquals(Integer.divideUnsigned(-1, -2), 1);
    assertEquals(Integer.divideUnsigned(-173448, 13), 330368757);
    assertEquals(Integer.divideUnsigned(Integer.MIN_VALUE, 2), (1 << 30));
    assertEquals(Integer.divideUnsigned(-1, Integer.MIN_VALUE), 1);
    assertEquals(Integer.divideUnsigned(Integer.MAX_VALUE, Integer.MIN_VALUE), 0);
    assertEquals(Integer.divideUnsigned(Integer.MIN_VALUE, Integer.MAX_VALUE), 1);

    try {
      Integer.divideUnsigned(1, 0);
      throw new Error("Unreachable");
    } catch (ArithmeticException expected) {
    }
  }

  public static void test_Long_divideUnsigned() {
    assertEquals(Long.divideUnsigned(100L, 10L), 10L);
    assertEquals(Long.divideUnsigned(100L, 1L), 100L);
    assertEquals(Long.divideUnsigned(1024L, 128L), 8L);
    assertEquals(Long.divideUnsigned(12345678L, 264L), 46763L);
    assertEquals(Long.divideUnsigned(13L, 5L), 2L);
    assertEquals(Long.divideUnsigned(-2L, 2L), Long.MAX_VALUE);
    assertEquals(Long.divideUnsigned(-1L, 2L), Long.MAX_VALUE);
    assertEquals(Long.divideUnsigned(100000L, -1L), 0L);
    assertEquals(Long.divideUnsigned(Long.MAX_VALUE, -1L), 0L);
    assertEquals(Long.divideUnsigned(-2L, -1L), 0L);
    assertEquals(Long.divideUnsigned(-1L, -2L), 1L);
    assertEquals(Long.divideUnsigned(-173448L, 13L), 1418980313362259859L);
    assertEquals(Long.divideUnsigned(Long.MIN_VALUE, 2L), (1L << 62));
    assertEquals(Long.divideUnsigned(-1L, Long.MIN_VALUE), 1L);
    assertEquals(Long.divideUnsigned(Long.MAX_VALUE, Long.MIN_VALUE), 0L);
    assertEquals(Long.divideUnsigned(Long.MIN_VALUE, Long.MAX_VALUE), 1L);
    assertEquals(Long.divideUnsigned(Long.MAX_VALUE, 1L), Long.MAX_VALUE);
    assertEquals(Long.divideUnsigned(Long.MIN_VALUE, 1L), Long.MIN_VALUE);
    assertEquals(Long.divideUnsigned(BIG_LONG_VALUE, BIG_LONG_VALUE), 1L);
    assertEquals(Long.divideUnsigned(BIG_LONG_VALUE, 1L), BIG_LONG_VALUE);
    assertEquals(Long.divideUnsigned(BIG_LONG_VALUE, 1024L), 721960566564885L);
    assertEquals(Long.divideUnsigned(BIG_LONG_VALUE, 0x1FFFFFFFFL), 86064406L);

    try {
      Long.divideUnsigned(1L, 0L);
      throw new Error("Unreachable");
    } catch (ArithmeticException expected) {
    }
  }

  public static int $noinline$remainderUnsignedInt(int a, int b) {
    return Integer.remainderUnsigned(a, b);
  }

  public static void test_Integer_remainderUnsigned_no_fold() {
    assertEquals($noinline$remainderUnsignedInt(100, 10), 0);
    assertEquals($noinline$remainderUnsignedInt(100, 1), 0);
    assertEquals($noinline$remainderUnsignedInt(1024, 127), 8);
    assertEquals($noinline$remainderUnsignedInt(12345678, 264), 246);
    assertEquals($noinline$remainderUnsignedInt(13, 5), 3);
    assertEquals($noinline$remainderUnsignedInt(-2, 2), 0);
    assertEquals($noinline$remainderUnsignedInt(-1, 2), 1);
    assertEquals($noinline$remainderUnsignedInt(100000, -1), 100000);
    assertEquals($noinline$remainderUnsignedInt(Integer.MAX_VALUE, -1), Integer.MAX_VALUE);
    assertEquals($noinline$remainderUnsignedInt(-2, -1), -2);
    assertEquals($noinline$remainderUnsignedInt(-1, -2), 1);
    assertEquals($noinline$remainderUnsignedInt(-173448, 13), 7);
    assertEquals($noinline$remainderUnsignedInt(Integer.MIN_VALUE, 2), 0);
    assertEquals($noinline$remainderUnsignedInt(-1, Integer.MIN_VALUE), Integer.MAX_VALUE);
    assertEquals(
        $noinline$remainderUnsignedInt(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE);
    assertEquals($noinline$remainderUnsignedInt(Integer.MIN_VALUE, Integer.MAX_VALUE), 1);

    try {
      $noinline$remainderUnsignedInt(1, 0);
      throw new Error("Unreachable");
    } catch (ArithmeticException expected) {
    }
  }

  public static long $noinline$remainderUnsignedLong(long a, long b) {
    return Long.remainderUnsigned(a, b);
  }

  public static void test_Long_remainderUnsigned_no_fold() {
    assertEquals($noinline$remainderUnsignedLong(100L, 10L), 0L);
    assertEquals($noinline$remainderUnsignedLong(100L, 1L), 0L);
    assertEquals($noinline$remainderUnsignedLong(1024L, 127L), 8L);
    assertEquals($noinline$remainderUnsignedLong(12345678L, 264L), 246L);
    assertEquals($noinline$remainderUnsignedLong(13L, 5L), 3L);
    assertEquals($noinline$remainderUnsignedLong(-2L, 2L), 0L);
    assertEquals($noinline$remainderUnsignedLong(-1L, 2L), 1L);
    assertEquals($noinline$remainderUnsignedLong(100000L, -1L), 100000L);
    assertEquals($noinline$remainderUnsignedLong(Long.MAX_VALUE, -1L), Long.MAX_VALUE);
    assertEquals($noinline$remainderUnsignedLong(-2L, -1L), -2L);
    assertEquals($noinline$remainderUnsignedLong(-1L, -2L), 1L);
    assertEquals($noinline$remainderUnsignedLong(-173448L, 13L), 1L);
    assertEquals($noinline$remainderUnsignedLong(Long.MIN_VALUE, 2L), 0L);
    assertEquals($noinline$remainderUnsignedLong(-1L, Long.MIN_VALUE), Long.MAX_VALUE);
    assertEquals($noinline$remainderUnsignedLong(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE);
    assertEquals($noinline$remainderUnsignedLong(Long.MIN_VALUE, Long.MAX_VALUE), 1L);
    assertEquals($noinline$remainderUnsignedLong(Long.MAX_VALUE, 1L), 0L);
    assertEquals($noinline$remainderUnsignedLong(Long.MIN_VALUE, 1L), 0L);
    assertEquals($noinline$remainderUnsignedLong(BIG_LONG_VALUE, BIG_LONG_VALUE), 0L);
    assertEquals($noinline$remainderUnsignedLong(BIG_LONG_VALUE, 1L), 0L);
    assertEquals($noinline$remainderUnsignedLong(BIG_LONG_VALUE, 1024L), 0L);
    assertEquals($noinline$remainderUnsignedLong(BIG_LONG_VALUE, 0x1FFFFFFFFL), 2009174294L);
    assertEquals(
        $noinline$remainderUnsignedLong(BIG_LONG_VALUE, -38766615688777L), 739287620162442240L);

    try {
      $noinline$remainderUnsignedLong(1L, 0L);
      throw new Error("Unreachable");
    } catch (ArithmeticException expected) {
    }
  }

  public static void test_Integer_remainderUnsigned() {
    assertEquals(Integer.remainderUnsigned(100, 10), 0);
    assertEquals(Integer.remainderUnsigned(100, 1), 0);
    assertEquals(Integer.remainderUnsigned(1024, 127), 8);
    assertEquals(Integer.remainderUnsigned(12345678, 264), 246);
    assertEquals(Integer.remainderUnsigned(13, 5), 3);
    assertEquals(Integer.remainderUnsigned(-2, 2), 0);
    assertEquals(Integer.remainderUnsigned(-1, 2), 1);
    assertEquals(Integer.remainderUnsigned(100000, -1), 100000);
    assertEquals(Integer.remainderUnsigned(Integer.MAX_VALUE, -1), Integer.MAX_VALUE);
    assertEquals(Integer.remainderUnsigned(-2, -1), -2);
    assertEquals(Integer.remainderUnsigned(-1, -2), 1);
    assertEquals(Integer.remainderUnsigned(-173448, 13), 7);
    assertEquals(Integer.remainderUnsigned(Integer.MIN_VALUE, 2), 0);
    assertEquals(Integer.remainderUnsigned(-1, Integer.MIN_VALUE), Integer.MAX_VALUE);
    assertEquals(
        Integer.remainderUnsigned(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE);
    assertEquals(Integer.remainderUnsigned(Integer.MIN_VALUE, Integer.MAX_VALUE), 1);

    try {
      Integer.remainderUnsigned(1, 0);
      throw new Error("Unreachable");
    } catch (ArithmeticException expected) {
    }
  }

  public static void test_Long_remainderUnsigned() {
    assertEquals(Long.remainderUnsigned(100L, 10L), 0L);
    assertEquals(Long.remainderUnsigned(100L, 1L), 0L);
    assertEquals(Long.remainderUnsigned(1024L, 127L), 8L);
    assertEquals(Long.remainderUnsigned(12345678L, 264L), 246L);
    assertEquals(Long.remainderUnsigned(13L, 5L), 3L);
    assertEquals(Long.remainderUnsigned(-2L, 2L), 0L);
    assertEquals(Long.remainderUnsigned(-1L, 2L), 1L);
    assertEquals(Long.remainderUnsigned(100000L, -1L), 100000L);
    assertEquals(Long.remainderUnsigned(Long.MAX_VALUE, -1L), Long.MAX_VALUE);
    assertEquals(Long.remainderUnsigned(-2L, -1L), -2L);
    assertEquals(Long.remainderUnsigned(-1L, -2L), 1L);
    assertEquals(Long.remainderUnsigned(-173448L, 13L), 1L);
    assertEquals(Long.remainderUnsigned(Long.MIN_VALUE, 2L), 0L);
    assertEquals(Long.remainderUnsigned(-1L, Long.MIN_VALUE), Long.MAX_VALUE);
    assertEquals(Long.remainderUnsigned(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE);
    assertEquals(Long.remainderUnsigned(Long.MIN_VALUE, Long.MAX_VALUE), 1L);
    assertEquals(Long.remainderUnsigned(Long.MAX_VALUE, 1L), 0L);
    assertEquals(Long.remainderUnsigned(Long.MIN_VALUE, 1L), 0L);
    assertEquals(Long.remainderUnsigned(BIG_LONG_VALUE, BIG_LONG_VALUE), 0L);
    assertEquals(Long.remainderUnsigned(BIG_LONG_VALUE, 1L), 0L);
    assertEquals(Long.remainderUnsigned(BIG_LONG_VALUE, 1024L), 0L);
    assertEquals(Long.remainderUnsigned(BIG_LONG_VALUE, 0x1FFFFFFFFL), 2009174294L);
    assertEquals(Long.remainderUnsigned(BIG_LONG_VALUE, -38766615688777L), 739287620162442240L);

    try {
      Long.remainderUnsigned(1L, 0L);
      throw new Error("Unreachable");
    } catch (ArithmeticException expected) {
    }
  }

  public static void assertEquals(int expected, int actual) {
      if (expected != actual) {
          throw new Error("Expected: " + expected + ", found: " + actual);
      }
  }

  public static void assertEquals(long expected, long actual) {
      if (expected != actual) {
          throw new Error("Expected: " + expected + ", found: " + actual);
      }
  }
}
