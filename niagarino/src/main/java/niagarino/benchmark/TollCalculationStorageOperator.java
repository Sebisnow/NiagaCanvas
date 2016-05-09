/*
 * @(#)TollCalculationStorageOperator.java   1.0   Apr 15, 2015
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
 * Operator for reading all information necessary for toll calculation, and for calculating tolls.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class TollCalculationStorageOperator extends SegmentedStorageRead<SegmentData> {

   /**
    * Constructs a new operator reading data from car segmentdata storage and calculating toll.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming tuples
    */
   public TollCalculationStorageOperator(final String operatorId, final Schema inputSchema) {
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
      public Object[] getSegmentKeys(final DataTuple tuple) {
         return new Object[] { (Integer) tuple.getAttributeValue("time") };
      }

      @Override
      public Object[] getSegmentKeys(final PunctuationControl punctuation) {
         return new Object[0];
      }

      @Override
      public Attribute getDerivedAttribute() {
         return new Attribute("toll", Integer.class);
      }

      @Override
      public Object derive(final DataTuple tuple, final StorageReader<SegmentData> reader) {
         final int segId = (Integer) tuple.getAttributeValue("segid");
         final Double lavD = (Double) tuple.getAttributeValue("AVG(AVG(AVG(spd)))");
         final int lav = (int) (lavD + 1e-14);
         final SegmentData segIdData = reader.get(segId);
         final int cars = segIdData == null ? 0 : segIdData.getCars();
         boolean accident = segIdData == null ? false : segIdData.hasAccident();
         final int east = (segId & (1 << 7)) == 0 ? 1 : -1;
         final int seg = segId % 128;
         for (int i = 0; i < 5; i++) {
            final int currentSeg = seg + i * east;
            if (currentSeg < 1 || currentSeg > 100) {
               break;
            }
            final SegmentData data = reader.get(segId + i * east);
            accident = accident || (data != null && data.hasAccident());
         }
         final int toll;
         if (lav < 40 && cars > 50 && !accident) {
            toll = 2 * (cars - 50) * (cars - 50);
         } else {
            toll = 0;
         }
         return toll;
      }
   }
}
