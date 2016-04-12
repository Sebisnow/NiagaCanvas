/*
 * @(#)AccidentsStorageOperator.java   1.0   Apr 15, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import niagarino.benchmark.LinearRoad.SegmentData;
import niagarino.operator.SegmentedStorageWrite;
import niagarino.storage.SegmentationKeyFunction;
import niagarino.storage.StorageUpdateFunction;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.Schema;

/**
 * Stores accidents into the accident storage.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class AccidentsStorageOperator extends SegmentedStorageWrite<SegmentData> {

   /**
    * Constructs a new operator for storing accidents.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming tuples
    */
   public AccidentsStorageOperator(final String operatorId, final Schema inputSchema) {
      super("storage-accidents", inputSchema, LinearRoadUtil.STORAGE_SEGMENTDATA_CARS, new Functions(),
            new Functions());
   }

   /**
    * Key derivation functions and storage write functions.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private static class Functions implements SegmentationKeyFunction, StorageUpdateFunction<SegmentData> {

      @Override
      public Object[] getSegmentKeys(final DataTuple tuple) {
         int key = (Integer) tuple.getAttributeValue("time");
         // Account for 30s slide windows.
         if ((key + 1) % 60 > 0) {
            key += 30;
         }
         return new Object[] { key };
      }

      @Override
      public Object[] getSegmentKeys(final PunctuationControl punctuation) {
         final int key = (int) punctuation.getSegmentEnd();
         if ((key + 1) % 60 == 0) {
            return new Object[] { key };
         }
         return new Object[0];
         // Do not close (or accidentally create) a window every odd 30 seconds.
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
         if (oldValue != null) {
            oldValue.setAccident();
            return oldValue;
         } else {
            final SegmentData value = new SegmentData();
            value.setAccident();
            return value;
         }
      }
   }
}
