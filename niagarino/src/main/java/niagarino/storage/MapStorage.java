/*
 * @(#)MapStorage.java   1.0   Nov 21, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.storage;

import java.util.HashMap;

/**
 * The MapStorage is a HashMap wrapper featuring dedicated writer and reader objects to ensure proper
 * parallelised access.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 * @param <V>
 *           the value type
 */
public class MapStorage<V> {

   /** The values of this storage. */
   private final HashMap<Object, V> values;

   /** A StorageReader instance for this MapStorage. */
   private StorageReader<V> reader = null;

   /** A StorageWriter instance for this MapStorage. */
   private StorageWriter<V> writer = null;

   /**
    * Constructs a new MapStorage with the specified initial capacity.
    *
    * @param initialSize
    *           the initial capacity of the underlying HashMap object
    */
   public MapStorage(final int initialSize) {
      if (initialSize == -1) {
         this.values = new HashMap<>();
      } else {
         this.values = new HashMap<Object, V>(initialSize);
      }
   }

   /**
    * Constructs a new MapStorage.
    */
   public MapStorage() {
      this(-1);
   }

   /**
    * Puts the specified value with the specified key into this MapStorage.
    *
    * @param key
    *           the key to place the value at
    * @param value
    *           the value to insert
    */
   protected void put(final Object key, final V value) {
      this.values.put(key, value);
   }

   /**
    * Retrieves the value for the specified key.
    *
    * @param key
    *           the key whose value is to get
    * @return the value
    */
   protected V get(final Object key) {
      return this.values.get(key);
   }

   @Override
   public String toString() {
      return this.values.toString();
   }

   /**
    * Delivers a StorageReader object which can read the values of this MapStorage object.
    *
    * @return a StorageReader for this MapStorage
    */
   public StorageReader<V> getReader() {
      if (this.reader == null) {
         this.reader = new StorageReader<>(this);
      }
      return this.reader;
   }

   /**
    * Delivers a StorageWriter object which can write values to this MapStorage object.
    *
    * @return a StorageWriter for this MapStorage
    */
   public StorageWriter<V> getWriter() {
      if (this.writer == null) {
         this.writer = new StorageWriter<>(this);
      }
      return this.writer;
   }

   /**
    * Factory to create new instances of MapStorage.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @param <V>
    *           the value type of the SimpleArrayStorage
    */
   public static class MapStorageFactory<V> {

      /**
       * The initial capacity MapStorage objects created with this factory shall have. <code>-1</code> stands
       * for the default value of the standard HashMap implementation.
       */
      private final int initialCapacity;

      /**
       * Constructs a new MapStorageFactory with the given initial capacity.
       *
       * @param initialCapacity
       *           the initial capacity the MapStorage object shall have
       */
      public MapStorageFactory(final int initialCapacity) {
         this.initialCapacity = initialCapacity;
      }

      /**
       * Constructs a new MapStorageFactory.
       */
      public MapStorageFactory() {
         this(-1);
      }

      /**
       * Creates a new MapStorage instance with the properties specified within this factory.
       *
       * @return a new MapStorage instance
       */
      public MapStorage<V> getStorageInstance() {
         return new MapStorage<>(this.initialCapacity);
      }
   }
}
