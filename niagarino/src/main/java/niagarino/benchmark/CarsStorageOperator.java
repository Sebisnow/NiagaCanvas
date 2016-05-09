/*
 * @(#)CarsStorageOperator.java   1.0   Apr 15, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import java.util.HashMap;

import niagarino.benchmark.LinearRoad.SegmentData;
import niagarino.operator.SegmentedStorageWrite;
import niagarino.storage.SegmentationKeyFunction;
import niagarino.storage.StorageUpdateFunction;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.Schema;

/**
 * Stores car statistics into the cars segmentdata storage for later use.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class CarsStorageOperator extends SegmentedStorageWrite<SegmentData> {

   /**
    * Constructs a new operator storing car statistics int the car segmentdata storage.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming tuples
    */
   public CarsStorageOperator(final String operatorId, final Schema inputSchema) {
      super(operatorId, inputSchema, LinearRoadUtil.STORAGE_SEGMENTDATA_CARS, new Functions(),
            new Functions());
   }

   /**
    * Functions for deriving the segment key and the update for the storage.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private static class Functions implements SegmentationKeyFunction, StorageUpdateFunction<SegmentData> {

      /** Access speed up with timestamp mapped to stream segment id. */
      private final HashMap<Long, Object> map = new HashMap<>();

      @Override
      public Object[] getSegmentKeys(final DataTuple tuple) {
         final Long segId = tuple.getElementMetadata().getSegmentIds().get(0);
         Object key = this.map.get(segId);
         if (key == null) {
            key = tuple.getAttributeValue("time");
            this.map.put(segId, key);
         }
         return new Object[] { key };
      }

      @Override
      public Object[] getSegmentKeys(final PunctuationControl punctuation) {
         return new Object[] { (int) punctuation.getSegmentEnd() };
      }

      @Override
      public int getTupleNumericKey(final DataTuple tuple) {
         return (Integer) tuple.getAttributeValue("segid");
      }

      @Override
      public Object getTupleKey(final DataTuple tuple) {
         return (Integer) tuple.getAttributeValue("segid");
      }

      @Override
      public SegmentData getUpdatedValue(final SegmentData oldValue, final DataTuple tuple) {
         final int cars = ((Long) tuple.getAttributeValue("COUNT(vid)")).intValue();
         if (oldValue != null) {
            oldValue.setCars(cars);
            return oldValue;
         } else {
            final SegmentData value = new SegmentData();
            value.setCars(cars);
            return value;
         }
      }
   }
}
