/*
 * @(#)SegmentedStorageRead.java   1.0   Feb 17, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import niagarino.plan.PlanOperatorByParametersFactory;
import niagarino.storage.SegmentationKeyFunction;
import niagarino.storage.SegmentedStorage;
import niagarino.storage.StorageReader;
import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * A segmented storage operator keeps a SegmentedStorage for writing. In order to store the segments
 * independent from the segment ID, a derivation is performed on the tuple to derive a key.<br/>
 * <br/>
 * This operator reads from a segmented storage.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @param <V>
 *           the value type of the SegmentedStorage
 */
public class SegmentedStorageRead<V> extends AbstractOperator {

   /** Logger of this class. */
   private static final Logger LOG = LogManager.getLogger(SegmentedStorageRead.class);

   /** Human-readable identifier of the operator. */
   private final String operatorId;

   /** Name/identifier of the storage. */
   private final String storageName;

   /** The storage object to use. */
   private final SegmentedStorage<V> storage;

   /** The function to derive segment keys. */
   private final SegmentationKeyFunction keyFunction;

   /** The derivation function to apply to tuples. */
   private final StorageDerivationFunction<V> derivationFunction;

   /** Output schema of this operator. */
   private final Schema outputSchema;

   /**
    * Constructs a new SegmentedStorageRead operator.
    *
    * @param operatorId
    *           the name of this operator
    * @param inputSchema
    *           the input schema of this operator
    * @param storageName
    *           the register name of the segmented storage object that organises all storages for the segments
    * @param keyFunction
    *           function to derive the segment's key for the storage corresponding to a tuple
    * @param derivationFunction
    *           function to derive a value from the storage
    */
   @SuppressWarnings("unchecked")
   public SegmentedStorageRead(final String operatorId, final Schema inputSchema, final String storageName,
         final SegmentationKeyFunction keyFunction, final StorageDerivationFunction<V> derivationFunction) {
      super(operatorId, Arrays.asList(inputSchema));
      this.operatorId = operatorId;
      this.storageName = storageName;
      final SegmentedStorage<V> storage = (SegmentedStorage<V>) SegmentedStorage
            .getInstanceByName(storageName);
      if (storage == null) {
         throw new IllegalArgumentException("Storage '" + storageName + "' is not known.");
      }
      this.storage = storage;
      this.keyFunction = keyFunction;
      this.derivationFunction = derivationFunction;
      final List<Attribute> attributes = new ArrayList<>(inputSchema.getAttributes());
      attributes.add(this.derivationFunction.getDerivedAttribute());
      this.outputSchema = new Schema(inputSchema.getProgressingAttribute(),
            attributes.toArray(new Attribute[0]));
   }

   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      final Object[] keys = this.keyFunction.getSegmentKeys(tuple);
      StorageReader<V> reader = null;
      synchronized (this.storage) {
         // TODO Either remove possibility to return mutliple keys, or actually use them.
         while (reader == null && keys.length > 0) {
            try {
               reader = this.storage.getSegmentReader(keys[0]);
            } catch (final IllegalArgumentException e) {
               // If the storage is closed, there will be no more changes. This means this tuple will not get any
               // matching entries. For now, we just discard in that case.
               if (this.storage.isClosed()) {
                  return;
               }
               try {
                  LOG.debug("[" + this.storageName + "] Could not find keys " + Arrays.toString(keys)
                        + ". Now waiting.");
                  this.storage.wait();
                  LOG.debug("[" + this.storageName + "] Woken for keys " + Arrays.toString(keys) + ".");
               } catch (final InterruptedException e1) {
                  // Nothing to do here.
               }
            }
         }
      }
      final Object newValue = this.derivationFunction.derive(tuple, reader);
      final List<Object> values = new ArrayList<Object>(tuple.getValues());
      values.add(newValue);
      this.pushTuple(new DataTuple(this.outputSchema, values, tuple.getElementMetadata()));
   }

   /**
    * Returns a StorageReader instance for the Storage of the segment identified by the given
    * <code>segmentKey</code> in this operator.
    *
    * @param segmentKey
    *           the object identifying the segment of which the StorageReader is to retrieve
    * @return a StorageReader instance for the given segment
    */
   public StorageReader<V> getSegmentReader(final Object segmentKey) {
      final StorageReader<V> reader = this.storage.getSegmentReader(segmentKey);
      if (reader == null) {
         throw new IllegalArgumentException("Storage '" + segmentKey
               + "' is not known in SegmentedStorageRead '" + this.operatorId + "'.");
      }
      return reader;
   }

   @Override
   public Schema getOutputSchema() {
      return this.outputSchema;
   }

   @Override
   public String toString() {
      return this.storage.toString();
   }

   /**
    * Factory for new instances of the SegmentedStorageRead operator. Needs to be extended since
    * SegmentedStorageRead is using generics which is not supported by Java Reflections.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public abstract static class Factory<V> implements PlanOperatorByParametersFactory {
   }
}
