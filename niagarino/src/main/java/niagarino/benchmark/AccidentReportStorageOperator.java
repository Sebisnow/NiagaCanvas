/*
 * @(#)AccidentReportStorageOperator.java   1.0   Jun 27, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import niagarino.benchmark.LinearRoad.SegmentData;
import niagarino.operator.SegmentedStorageRead;
import niagarino.operator.StorageDerivationFunction;
import niagarino.storage.SegmentationKeyFunction;
import niagarino.storage.StorageReader;
import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.Schema;

/**
 * Reads accidents relevant for incoming tuples from the accidents storage.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class AccidentReportStorageOperator extends SegmentedStorageRead<SegmentData> {

   /**
    * Constructs a new accident report storage operator.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming tuples
    */
   public AccidentReportStorageOperator(final String operatorId, final Schema inputSchema) {
      super(operatorId, inputSchema, LinearRoadUtil.STORAGE_SEGMENTDATA_CARS, new Functions(),
            new Functions());
   }

   /**
    * Key derivation functions and storage read functions.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private static class Functions implements SegmentationKeyFunction, StorageDerivationFunction<SegmentData> {

      @Override
      public Attribute getDerivedAttribute() {
         return new Attribute("accident_segment", Integer.class);
      }

      @Override
      public Object derive(final DataTuple tuple, final StorageReader<SegmentData> reader) {
         if (reader == null) {
            return -1;
         }
         final int segid = (Integer) tuple.getAttributeValue("segid");
         final int direction = (segid >>> 7) % 2 == 0 ? 1 : -1;
         // if 1: sinking (west); if 0: rising (east)
         int accseg = -1;
         if (direction == 1) {
            // Pure segment number
            final int seg = segid % 128;
            // How many segments to check in case of end of highway
            final int diff = Math.min(5, 100 - seg);
            for (int i = 0; i < diff; i++) {
               final SegmentData data = reader.get(segid + i);
               if (data != null && data.hasAccident()) {
                  accseg = seg + i;
                  break;
               }
            }
         } else {
            // Pure segment number
            final int seg = segid % 128;
            // How many segments to check in case of end of highway
            final int diff = Math.min(5, seg + 1);
            for (int i = 0; i < diff; i++) {
               final SegmentData data = reader.get(segid - i);
               if (data != null && data.hasAccident()) {
                  accseg = seg - i;
                  break;
               }
            }
         }
         return accseg;
      }

      @Override
      public Object[] getSegmentKeys(final DataTuple tuple) {
         final Integer l = (Integer) tuple.getAttributeValue("minute");
         if (l == 0) {
            // There is no previous segment.
            return new Object[0];
         } else {
            return new Object[] { l * 60 - 1 };
         }
      }

      @Override
      public Object[] getSegmentKeys(final PunctuationControl punctuation) {
         return new Object[0];
      }
   }
}
