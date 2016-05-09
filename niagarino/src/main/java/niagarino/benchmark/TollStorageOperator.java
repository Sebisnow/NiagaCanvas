/*
 * @(#)TollStorageOperator.java   1.0   Jun 15, 2015
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
 * Stores toll data to segmentdata storage.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class TollStorageOperator extends SegmentedStorageWrite<SegmentData> {

   /**
    * Constructs a new operator for storing toll data to segmentdata storage.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming tuples
    */
   public TollStorageOperator(final String operatorId, final Schema inputSchema) {
      super(operatorId, inputSchema, LinearRoadUtil.STORAGE_SEGMENTDATA, new Functions(), new Functions());
   }

   /**
    * Key derivation functions and storage write functions.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private static class Functions implements SegmentationKeyFunction, StorageUpdateFunction<SegmentData> {

      @Override
      public int getTupleNumericKey(final DataTuple tuple) {
         return (Integer) tuple.getAttributeValue("segid");
      }

      @Override
      public Object getTupleKey(final DataTuple tuple) {
         return this.getTupleKey(tuple);
      }

      @Override
      public SegmentData getUpdatedValue(final SegmentData oldValue, final DataTuple tuple) {
         SegmentData value = oldValue;
         if (value == null) {
            value = new SegmentData();
         }
         final Double lavD = (Double) tuple.getAttributeValue("AVG(AVG(AVG(spd)))");
         final int lav = (int) (lavD + 1e-14);
         value.setLAV(lav);
         value.setToll((Integer) tuple.getAttributeValue("toll"));
         return value;
      }

      @Override
      public Object[] getSegmentKeys(final DataTuple tuple) {
         return new Object[] { (Integer) tuple.getAttributeValue("time") };
      }

      @Override
      public Object[] getSegmentKeys(final PunctuationControl punctuation) {
         return new Object[] { (int) punctuation.getSegmentEnd() };
      }
   }
}
