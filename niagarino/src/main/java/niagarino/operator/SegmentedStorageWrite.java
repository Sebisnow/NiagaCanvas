/*
 * @(#)SegmentedStorageWrite.java   1.0   Feb 17, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import niagarino.plan.PlanOperatorByParametersFactory;
import niagarino.storage.SegmentationKeyFunction;
import niagarino.storage.SegmentedStorage;
import niagarino.storage.StorageUpdateFunction;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;

/**
 * A segmented storage operator keeps a SegmentedStorage for writing. In order to store the segments
 * independent from the segment ID, a derivation is performed on the tuple to derive a key.<br/>
 * <br/>
 * This operator writes to a segmented storage.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 * @param <V>
 *           the value type of the SegmentedStorage
 */
public class SegmentedStorageWrite<V> extends AbstractOperator {

   /** Logger of this class. */
   @SuppressWarnings("unused")
   private static final Logger LOG = LogManager.getLogger(SegmentedStorageWrite.class);

   /** The storage object to use. */
   private final SegmentedStorage<V> storage;

   /** The function to derive a segment key from a tuple. */
   private final SegmentationKeyFunction keyFunction;

   /** The update function to use for inserting new tuples. */
   private final StorageUpdateFunction<V> updateFunction;

   /**
    * Constructs a new SegmentedStorageWrite operator.
    *
    * @param operatorId
    *           the name of this operator
    * @param inputSchema
    *           the input schema of this operator
    * @param storageName
    *           the register name of the segmented storage object that organises all storages for the segments
    * @param keyFunction
    *           function to derive segment keys with
    * @param updateFunction
    *           the function to derive the value to store in the storage from the tuple
    */
   @SuppressWarnings("unchecked")
   public SegmentedStorageWrite(final String operatorId,
         final Schema inputSchema, final String storageName, final SegmentationKeyFunction keyFunction,
         final StorageUpdateFunction<V> updateFunction) {
      super(operatorId, Arrays.asList(inputSchema));
      final SegmentedStorage<V> storage = (SegmentedStorage<V>) SegmentedStorage
            .getInstanceByName(storageName);
      if (storage == null) {
         throw new IllegalArgumentException("Storage '" + storageName + "' is not known.");
      }
      this.storage = storage;
      this.storage.registerWriter(this);
      this.keyFunction = keyFunction;
      this.updateFunction = updateFunction;
   }

   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      final Object[] segmentKeys = this.keyFunction.getSegmentKeys(tuple);
      synchronized (this.storage) {
         this.storage.processTuple(tuple, segmentKeys, this.updateFunction);
         this.storage.notifyAll();
      }
      this.pushTuple(tuple);
   }

   @Override
   public Schema getOutputSchema() {
      return super.getInputSchemas().get(0);
   }

   @Override
   public void stop() {
      this.storage.setEOS();
      super.stop();
   }

   @Override
   public String toString() {
      return this.storage.toString();
   }

   @Override
   protected void processForwardControl(final int input, final ControlTuple message) {
      switch (message.getType()) {
         case PUNCTUATION:
            final PunctuationControl pc = (PunctuationControl) message;
            for (final Object key : this.keyFunction.getSegmentKeys(pc)) {
               this.storage.closeSegment(key);
            }
            this.pushControl(Flow.FORWARD, message);
            break;
         default:
            this.pushControl(Flow.FORWARD, message);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void handleEoS(final Socket socket, final int input, final ControlTuple message) {
      if (Socket.INPUT.equals(socket)) {
         this.storage.setEOS();
      }
      super.handleEoS(socket, input, message);
   }

   /**
    * Factory for new instances of the SegmentedStorageWrite operator. Needs to be extended since
    * SegmentedStorageWrite is using generics which is not supported by Java Reflections.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public abstract static class Factory<V> implements PlanOperatorByParametersFactory {
   }
}
