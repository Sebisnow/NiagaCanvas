/*
 * @(#)LAVReportStorageOperator.java   1.0   Jul 8, 2015
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
 * Reads LAV from the segmentdata storage for toll reports.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class LAVReportStorageOperator extends SegmentedStorageRead<SegmentData> {

   /**
    * Constructs a new operator reading LAV from the segmentdata storage.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming tuples
    */
   public LAVReportStorageOperator(final String operatorId, final Schema inputSchema) {
      super(operatorId, inputSchema, LinearRoadUtil.STORAGE_SEGMENTDATA, new Functions(),
            new Functions());
   }

   /**
    * Key derivation functions and storage read functions.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private static class Functions implements SegmentationKeyFunction, StorageDerivationFunction<SegmentData> {

      // Toll Notifications: Type, VID, Time, Emit, Spd (LAV), Toll

      @Override
      public Attribute getDerivedAttribute() {
         return new Attribute("lav", Integer.class);
      }

      @Override
      public Object derive(final DataTuple tuple, final StorageReader<SegmentData> reader) {
         if (reader == null) {
            return -1;
         }
         final int segid = (Integer) tuple.getAttributeValue("segid");
         final SegmentData data = reader.get(segid);
         final int lav;
         if (data == null) {
            lav = -1;
         } else {
            lav = data.getLAV();
         }
         return lav;
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
         return new Object[] {};
      }
   }
}
