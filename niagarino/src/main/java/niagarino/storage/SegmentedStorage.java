/*
 * @(#)SegmentedStorage.java   1.0   Feb 17, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import niagarino.operator.SegmentedStorageWrite;
import niagarino.storage.MapStorage.MapStorageFactory;
import niagarino.stream.DataTuple;

/**
 * A SegmentedStorage stores the values of the processed tuples in segments. These segments are identified by
 * the
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 * @param <V>
 *           the value type of the MapStorage objects
 */
public class SegmentedStorage<V> {

   /** Logger for {@link SegmentedStorage}. */
   private static final Logger LOG = LogManager.getLogger(SegmentedStorage.class);

   /** The register containing all instances identified by name. */
   private static HashMap<String, SegmentedStorage< ? >> register = new HashMap<>();

   /**
    * Retrieves a SegmentedStorage instance by name.
    *
    * @param name
    *           the name of the instance in question within the register
    * @return the requested instance, or <code>null</code> if no instance exists with this name
    */
   public static SegmentedStorage< ? > getInstanceByName(final String name) {
      return SegmentedStorage.register.get(name);
   }

   /**
    * Releases the instance identified by the given name from the register.
    *
    * @param name
    *           the name of the instance
    */
   public static void removeInstanceByName(final String name) {
      SegmentedStorage.register.remove(name);
   }

   /** The name of this storage. */
   private final String name;

   /** The amount of closed segments to keep around. */
   private final int historySize;

   /** The factory for new MapStorage objects. */
   private final MapStorageFactory<V> factory;

   /** Storage objects for the segments. */
   private final HashMap<Object, MapStorage<V>> storages;

   /** Storage objects for closed segments. */
   private final HashMap<Object, MapStorage<V>> closedSegments;

   /** A queue to keep track of the order of the segments. */
   private final Queue<Object> history;

   /** Counts of registered writers per segment. */
   private final HashMap<Object, Integer> closeState;

   /** Registered writers. */
   private final HashSet<Object> writers;

   /** Finish remaining. */
   private int remainingWriters;

   /**
    * Constructs a new SegmentedStorage operator.
    *
    * @param name
    *           the name of the segmented storage within the register
    * @param factory
    *           the factory to create new MapStorage objects from
    * @param historySize
    *           the amount of closed segments to keep around
    */
   public SegmentedStorage(final String name, final MapStorageFactory<V> factory, final int historySize) {
      if (SegmentedStorage.getInstanceByName(name) != null) {
         throw new IllegalArgumentException("SegmentedStorage with name '" + name + "' already exists.");
      }
      this.name = name;
      this.historySize = historySize;
      this.factory = factory;
      this.storages = new HashMap<>();
      this.closedSegments = new HashMap<>();
      this.history = new LinkedList<>();
      this.closeState = new HashMap<>();
      this.writers = new HashSet<>();
      this.remainingWriters = 0;
      SegmentedStorage.register.put(name, this);
      LOG.info("Created SegmentedStorage '" + name + "'.");
   }

   /**
    * Inserts the given tuple into the correct segment storages after deriving the correct value using the
    * given StorageUpdateFunction.
    *
    * @param tuple
    *           the tuple to insert
    * @param segmentKeys
    *           keys of affected segments
    * @param updateFunction
    *           function to update the storage using the given tuple
    */
   public void processTuple(final DataTuple tuple, final Object[] segmentKeys,
         final StorageUpdateFunction<V> updateFunction) {
      for (final Object segmentKey : segmentKeys) {
         MapStorage<V> storage = this.storages.get(segmentKey);
         if (storage == null) {
            // Synchronize only if absolutely necessary, even if we need to do some work twice.
            synchronized (this) {
               storage = this.storages.get(segmentKey);
               if (storage == null) {
                  storage = this.createSegment(segmentKey);
               }
            }
         }
         final int key = updateFunction.getTupleNumericKey(tuple);
         synchronized (storage) {
            final V value = storage.getReader().get(key);
            final V newValue = updateFunction.getUpdatedValue(value, tuple);
            storage.getWriter().put(key, newValue);
         }
      }
   }

   /**
    * Creates a new segment for the given key.
    *
    * @param segmentKey
    *           the key identifying the new segment
    * @return the newly created MapStorage object
    */
   private MapStorage<V> createSegment(final Object segmentKey) {
      final MapStorage<V> storage = this.factory.getStorageInstance();
      this.storages.put(segmentKey, storage);
      this.history.add(segmentKey);
      this.closeState.put(segmentKey, this.writers.size());
      LOG.debug("[" + this.name + "] Created segment '" + segmentKey + "'.");
      return storage;
   }

   /**
    * Informs this SegmentedStorage that the given operator is writing to it. This is necessary to close
    * segments properly.
    *
    * @param writer
    *           the writing operator to register
    */
   public void registerWriter(final SegmentedStorageWrite<V> writer) {
      this.writers.add(writer);
      this.remainingWriters = this.writers.size();
   }

   /**
    * Retrieves the reader object for the storage of the segment identified by the given key.
    *
    * @param segmentKey
    *           the key for the segment of the required storage
    * @return the StorageReader for the segment in question
    */
   public StorageReader<V> getSegmentReader(final Object segmentKey) {
      MapStorage<V> storage = this.closedSegments.get(segmentKey);
      if (storage == null) {
         synchronized (this) {
            storage = this.closedSegments.get(segmentKey);
            final boolean open = this.storages.containsKey(segmentKey);
            if (storage == null && !open) {
               throw new IllegalArgumentException("Storage '" + segmentKey
                     + "' is not known or closed in this SegmentedStorage.");
            } else {
               while (storage == null) {
                  try {
                     LOG.debug("[" + this.name + "] Waiting for segment " + segmentKey + " to become ready.");
                     this.wait();
                  } catch (final InterruptedException e) {
                     // Nothing to do here.
                  }
                  LOG.debug("[" + this.name + "] Woken while waiting for segment " + segmentKey
                        + " to become ready.");
                  storage = this.closedSegments.get(segmentKey);
               }
            }
         }
      }
      return storage.getReader();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
      return this.storages.toString();
   }

   /**
    * Marks EOS for this {@link SegmentedStorage}.
    */
   public synchronized void setEOS() {
      this.remainingWriters--;
      LOG.debug("[" + this.name + "] Received EOS. Remaining: " + this.remainingWriters);
      if (this.remainingWriters == 0) {
         LOG.debug("[" + this.name + "] Closing remaining storages.");
         while (!this.storages.isEmpty()) {
            final ArrayList<Object> keys = new ArrayList<>(this.storages.keySet());
            for (final Object key : keys) {
               this.closeSegment(key);
            }
         }
      }
      // Notify any waiting readers of the changes.
      this.notifyAll();
   }

   /**
    * Closes the segment identified by the given segmentKey.
    *
    * @param segmentKey
    *           identification of the segment
    */
   public synchronized void closeSegment(final Object segmentKey) {
      Integer count = this.closeState.get(segmentKey);
      if (count == null) {
         this.createSegment(segmentKey);
         count = this.closeState.get(segmentKey);
         LOG.debug("[" + this.name + "] Created segment '" + segmentKey + "' in order to detach from it.");
      }
      count--;
      if (count > 0) {
         this.closeState.put(segmentKey, count);
         LOG.debug("[" + this.name + "] Detached from storage for segment " + segmentKey + ". Remaining: "
               + count);
      } else {
         this.closeState.remove(segmentKey);
         final MapStorage<V> storage = this.storages.remove(segmentKey);
         this.closedSegments.put(segmentKey, storage);
         LOG.debug("[" + this.name + "] Closed storage for segment " + segmentKey + ".");
      }

      // Remove segments that are too old.
      while (this.historySize < this.closedSegments.size()) {
         Object key = this.history.poll();
         if (key != null) {
            final MapStorage<V> removedStorage = this.closedSegments.remove(key);
            if (removedStorage == null) {
               // Segment is way too old, but not even closed. Let's close and remove it.
               // This happens if segments are not closed in order.
               LOG.warn("[" + this.name + "] Segment " + key
                     + " is far too old. Closing and removing immediately.");
               LOG.warn("[" + this.name + "] Apparently, segments are not closed in order.");
               this.closeState.remove(key);
               this.storages.remove(key);
            }
         } else {
            // TODO History is empty - this should not happen, we need to fix this later.
            // Reproduce: Have segments closed with key typ Long, create with key type Integer
            // Kill segments at random.
            LOG.error("["
                  + this.name
                  + "] Got a NULL key to close - SOMETHING IS WRONG. Removing a random candidate from the history.");
            key = this.closedSegments.keySet().iterator().next();
            this.closedSegments.remove(key);
         }
         LOG.debug("[" + this.name + "] Removed segment '" + key + "'.");
      }
      this.notifyAll();
   }

   /**
    * Returns whether this {@link SegmentedStorage} is closed.
    *
    * @return whether this {@link SegmentedStorage} is closed
    */
   public boolean isClosed() {
      return this.remainingWriters == 0;
   }
}
