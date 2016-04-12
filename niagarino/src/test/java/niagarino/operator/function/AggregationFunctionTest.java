/*
 * @(#)AggregationFunctionTest.java   1.0   Jul 22, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator.function;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test cases for the calculation of incremental aggregates.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 */
public class AggregationFunctionTest {

   /** Number of values that will be aggregated by the test case. */
   private static final int NUM_VALUES = 1000000;

   /** Number of {@code null} values included in the list of values. */
   private static final int NUM_NULL_VALUES = 1000;

   /** Number of non-{@code null} values included in the list of values. */
   private static final int NUM_NONNULL_VALUES = NUM_VALUES - NUM_NULL_VALUES;

   /** Minimum value that will be generated. */
   private static final double MINIMUM = 0;

   /** Maximum value that will be generated. */
   private static final double MAXIMUM = 1000000000;

   /** Permissible delta between the aggregated value and the exact value. */
   private static final double DELTA = 0.0;

   /** List of values to aggregate. */
   private static final List<Double> VALUES = new ArrayList<>();

   /** Sum of all values. */
   private static BigDecimal exactSum;

   /**
    * Test case to check if the incremental average is calculated correctly.
    */
   @Test
   public void testAverage() {
      final Aggregator agg = BuiltInAggregationFunction.AVG.get();
      for (final Double value : VALUES) {
         agg.update(value);
      }
      final BigDecimal exact = exactSum
            .divide(BigDecimal.valueOf(NUM_NONNULL_VALUES), MathContext.DECIMAL128);
      final double avg = ((Number) agg.getValue()).doubleValue();
      assertEquals(exact.doubleValue(), avg, DELTA);
   }

   /**
    * Test case to check if the incremental sum is calculated correctly.
    */
   @Test
   public void testSum() {
      final Aggregator agg = BuiltInAggregationFunction.SUM.get();
      for (final Double value : VALUES) {
         agg.update(value);
      }
      final double sum = ((Number) agg.getValue()).doubleValue();
      assertEquals(exactSum.doubleValue(), sum, DELTA);
   }

   /**
    * Test case to check if the incremental count is calculated correctly.
    */
   @Test
   public void testCount() {
      final Aggregator agg = BuiltInAggregationFunction.COUNT.get();
      for (final Double value : VALUES) {
         agg.update(value);
      }
      final long count = ((Number) agg.getValue()).longValue();
      assertEquals(NUM_NONNULL_VALUES, count);
   }

   /**
    * Test case to check if the incremental minimum is calculated correctly.
    */
   @Test
   public void testMinimum() {
      final Aggregator agg = BuiltInAggregationFunction.MIN.get();
      for (final Double value : VALUES) {
         agg.update(value);
      }
      final double min = ((Number) agg.getValue()).doubleValue();
      assertEquals(MINIMUM, min, DELTA);
   }

   /**
    * Test case to check if the incremental maximum is calculated correctly.
    */
   @Test
   public void testMaximum() {
      final Aggregator agg = BuiltInAggregationFunction.MAX.get();
      for (final Double value : VALUES) {
         agg.update(value);
      }
      final double max = ((Number) agg.getValue()).doubleValue();
      assertEquals(MAXIMUM, max, DELTA);
   }

   /**
    * Initializes the list of values to aggregate.
    */
   @BeforeClass
   public static void setUp() {
      VALUES.clear();
      final Random rnd = new Random();
      final double minimum = MINIMUM;
      final double range = MAXIMUM - MINIMUM;
      for (int i = 0; i < NUM_VALUES; i++) {
         final Double value = Double.valueOf(minimum + rnd.nextDouble() * range);
         VALUES.add(value);
      }
      // Make sure that the minimum and maximum are in the list.
      final int[] reserved = new int[2 + NUM_NULL_VALUES];
      reserved[0] = rnd.nextInt(NUM_VALUES);
      VALUES.set(reserved[0], MINIMUM);
      do {
         reserved[1] = rnd.nextInt(NUM_VALUES);
      } while (reserved[1] == reserved[0]);
      VALUES.set(reserved[1], MAXIMUM);
      // Include null values.
      for (int i = 0; i < NUM_NULL_VALUES; i++) {
         do {
            reserved[2 + i] = rnd.nextInt(NUM_VALUES);
         } while (arrayContains(reserved, reserved[2 + i], 2 + i));
         VALUES.set(reserved[2 + i], null);
      }
      exactSum = calculateExactSum(VALUES);
   }

   /**
    * Checks if the given position is already contained in the given array.
    *
    * @param array
    *           array to check
    * @param value
    *           value to check
    * @param pos
    *           maximum position to check
    * @return {@code true} if the values is contained before the given position, {@code false} otherwise
    */
   private static boolean arrayContains(final int[] array, final int value, final int pos) {
      for (int i = 0; i < pos; i++) {
         if (array[i] == value) {
            return true;
         }
      }
      return false;
   }

   /**
    * Calculates the exact sum of the given list as a {@link BigDecimal}.
    *
    * @param values
    *           list of double values
    * @return sum of all values
    */
   private static BigDecimal calculateExactSum(final List<Double> values) {
      BigDecimal sum = BigDecimal.ZERO;
      for (final Double value : values) {
         if (value != null) {
            sum = sum.add(BigDecimal.valueOf(value.doubleValue()));
         }
      }
      return sum;
   }
}
