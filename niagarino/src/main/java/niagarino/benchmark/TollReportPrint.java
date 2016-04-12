/*
 * @(#)TollReportPrint.java   1.0   Jul 15, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.Instant;

import niagarino.operator.Print;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Writes the toll reports to the target file.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class TollReportPrint extends Print {

   /** Target file name. */
   public static final String TARGET_FILE = "report_toll.csv";

   /** Format for writing to the target file. */
   private static final String FORMAT = "0,%1$d,%2$d,%3$d,%4$d,%5$d%n";

   /** Output stream. */
   private final PrintStream out;

   /**
    * Constructs a new operator for writing toll reports.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming tuples
    * @throws FileNotFoundException
    *            if the target file can not be written to
    */
   public TollReportPrint(final String operatorId, final Schema inputSchema) throws FileNotFoundException {
      super(operatorId, inputSchema, false, System.out);
      final String targetPath = LinearRoadUtil.TARGET_PATH;
      if (targetPath.length() == 0) {
         this.out = null;
      } else {
         this.out = new PrintStream(targetPath + File.separator + TARGET_FILE);
      }
   }

   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      if (this.out != null) {
         final int lane = (Integer) tuple.getAttributeValue("lane");
         if (lane != 4) {
            final int time = (Integer) tuple.getAttributeValue("time2");
            final int emit = (int) (time + (Instant.now().toEpochMilli() - (Long) tuple
                  .getAttributeValue("epoch_millis")) / 1000);
            final int vid = (Integer) tuple.getAttributeValue("vid");
            final int spd = (Integer) tuple.getAttributeValue("lav");
            final int toll = (Integer) tuple.getAttributeValue("toll");
            this.out.printf(FORMAT, vid, time, emit, spd, toll);
         }
      }
      this.pushTuple(tuple);
   }
}
