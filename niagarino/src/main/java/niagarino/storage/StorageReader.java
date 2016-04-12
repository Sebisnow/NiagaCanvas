/*
 * @(#)StorageReader.java   1.0   Nov 21, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.storage;

/**
 * Reader for the MapStorage.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 * @param <V>
 *           the value type for the MapStorage of this StorageReader
 */
public class StorageReader<V> {

   /** The storage this reader reads from. */
   private final MapStorage<V> storage;

   /**
    * Constructs a new StorageReader for the given storage.
    *
    * @param storage
    *           the storage this reader reads from
    */
   protected StorageReader(final MapStorage<V> storage) {
      this.storage = storage;
   }

   /**
    * Retrieves the value for the given key.
    *
    * @param key
    *           the key for the desired value
    * @return the desired value
    */
   public V get(final Object key) {
      return this.storage.get(key);
   }
}
