/*
 * @(#)AccidentReportPrintP.java   1.0   Jun 28, 2015
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
 * Writer of accident reports. This is the version compliant to the paper's description.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class AccidentReportPrintP extends Print {

   /** Name of the target file. */
   public static final String TARGET_FILE = "report_accident.csv";

   /** Format to use for writing to results file. */
   private static final String FORMAT = "1,%1$d,%2$d,%3$d%n";

   /** Output stream for writing. */
   private final PrintStream out;

   /**
    * Constructs a new AccidentReportPrint writing accident reports to the target file.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of incoming data
    * @throws FileNotFoundException
    *            if the output file can not be written to
    */
   public AccidentReportPrintP(final String operatorId, final Schema inputSchema) throws FileNotFoundException {
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
         final int time = (Integer) tuple.getAttributeValue("time2");
         final int emit = (int) (time + (Instant.now().toEpochMilli() - (Long) tuple
               .getAttributeValue("epoch_millis")) / 1000);
         final int seg = (Integer) tuple.getAttributeValue("accident_segment");
         this.out.printf(FORMAT, time, emit, seg);
      }
      this.pushTuple(tuple);
   }
}
