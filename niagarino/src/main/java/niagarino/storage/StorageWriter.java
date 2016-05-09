/*
 * @(#)StorageWriter.java   1.0   Nov 19, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.storage;

/**
 * Wrapper for MapStorage objects in order to organise synchronised writes.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 * @param <V>
 *           the type of the values
 */
public class StorageWriter<V> {

   /** The storage this writer writes to. */
   private final MapStorage<V> storage;

   /**
    * Constructs a new StorageWriter for the given storage.
    *
    * @param storage
    *           the storage this writer writes to
    */
   protected StorageWriter(final MapStorage<V> storage) {
      this.storage = storage;
   }

   /**
    * Puts a key value pair into the storage of this writer.
    *
    * @param key
    *           the key for the value
    * @param value
    *           the value
    */
   public synchronized void put(final Object key, final V value) {
      this.storage.put(key, value);
   }
}
