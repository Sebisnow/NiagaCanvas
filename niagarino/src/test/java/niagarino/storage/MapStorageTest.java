/*
 * @(#)MapStorageTest.java   1.0   Nov 21, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.storage;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the MapStorage.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class MapStorageTest {

   /** The minimum index to use for the storage tests. */
   private static final int LINEAR_INSERT_MIN = -100000;
   /** The maximum index to use for the storage tests. */
   private static final int LINEAR_INSERT_MAX = 100000;

   /** How many values the random read test should read. */
   private static final int RANDOM_READ_COUNT = 1000000;

   /**
    * Inserts values with all possible keys within testing range, from smallest to largest key.
    */
   @Test
   public void testLinearInsert() {
      final int min = LINEAR_INSERT_MIN;
      final int max = LINEAR_INSERT_MAX;

      final MapStorage<String> storage = new MapStorage<>(Math.abs(min) + max);
      this.linearInsert(min, max, storage);
   }

   /**
    * Inserts values for all keys within the specified range in linear manner.
    *
    * @param min
    *           the minimum key to use for insert
    * @param max
    *           the maximum key to use for insert
    * @param storage
    *           the storage in which values are to insert
    */
   private void linearInsert(final int min, final int max, final MapStorage<String> storage) {
      final StorageWriter<String> writer = storage.getWriter();

      for (int i = min; i < max; i++) {
         writer.put(i, Integer.toString(i + 10));
      }
   }

   /**
    * Tests values for all keys within the specified range in linear manner.
    *
    * @param min
    *           the minimum key to use for insert
    * @param max
    *           the maximum key to use for insert
    * @param storage
    *           the storage in which values are to insert
    */
   private void linearRead(final int min, final int max, final MapStorage<String> storage) {
      final StorageReader<String> reader = storage.getReader();

      for (int i = min; i < max; i++) {
         Assert.assertEquals(Integer.toString(i + 10), reader.get(i));
      }
   }

   /**
    * Test values for all keys within the specified range in random order.
    *
    * @param min
    *           the minimum key to use for insert
    * @param max
    *           the maximum key to use for insert
    * @param storage
    *           the storage in which values are to insert
    */
   private void randomRead(final int min, final int max, final MapStorage<String> storage) {
      final StorageReader<String> reader = storage.getReader();

      for (int i = 0; i < RANDOM_READ_COUNT; i++) {
         final int index = ((int) Math.random() * (max - min)) + min;
         Assert.assertEquals(Integer.toString(index + 10), reader.get(index));
      }
   }

   /**
    * Inserts values for all keys within the specified range in random order.
    *
    * @param min
    *           the minimum key to use for insert
    * @param max
    *           the maximum key to use for insert
    * @param storage
    *           the storage in which values are to insert
    */
   private void randomInsert(final int min, final int max, final MapStorage<String> storage) {
      final StorageWriter<String> writer = storage.getWriter();

      final ArrayList<Integer> numbers = new ArrayList<Integer>(max - min);
      for (int i = min; i < max; i++) {
         numbers.add(i);
      }
      Collections.shuffle(numbers);

      for (final Integer number : numbers) {
         writer.put(number.intValue(), new Integer(number + 10).toString());
      }
   }

   /**
    * Tests whether the inserted values can be retrieved correctly. Does this by inserting values from
    * smallest to largest key and testing the content for each key in the same order.
    */
   @Test
   public void testLinearInsertRead() {
      final int min = LINEAR_INSERT_MIN;
      final int max = LINEAR_INSERT_MAX;

      final MapStorage<String> storage = new MapStorage<>(Math.abs(min) + max);
      this.linearInsert(min, max, storage);
      this.linearRead(min, max, storage);
   }

   /**
    * Tests whether random access works on all keys. Fills the storage in linear order prior to testing.
    */
   @Test
   public void testRandomRead() {
      final int min = LINEAR_INSERT_MIN;
      final int max = LINEAR_INSERT_MAX;

      final MapStorage<String> storage = new MapStorage<>(Math.abs(min) + max);
      this.linearInsert(min, max, storage);
      this.randomRead(min, max, storage);
   }

   /**
    * Tests whether random insert works with all keys.
    */
   @Test
   public void testRandomInsert() {
      final int min = LINEAR_INSERT_MIN;
      final int max = LINEAR_INSERT_MAX;

      final MapStorage<String> storage = new MapStorage<>(Math.abs(min) + max);
      this.randomInsert(min, max, storage);
   }

   /**
    * Tests whether values can be inserted twice for the same key.
    */
   @Test
   public void testDuplicateInsert() {
      final int min = LINEAR_INSERT_MIN;
      final int max = LINEAR_INSERT_MAX;

      final MapStorage<String> storage = new MapStorage<>(Math.abs(min) + max);
      this.linearInsert(min, max, storage);
      this.linearInsert(min, max, storage);
   }
}
