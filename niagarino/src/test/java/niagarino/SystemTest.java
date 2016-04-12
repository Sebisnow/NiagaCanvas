/*
 * @(#)SystemTest.java   1.0   Feb 15, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;

import org.junit.Ignore;
import org.junit.Test;

import niagarino.operator.Frame;
import niagarino.operator.Multiplex;
import niagarino.operator.Operator;
import niagarino.operator.OrderedAggregate;
import niagarino.operator.Print;
import niagarino.operator.Scan;
import niagarino.operator.ValueWindow;
import niagarino.operator.function.BuiltInAggregationFunction;
import niagarino.operator.predicate.MinMaxFramePredicate;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.simulator.DoubleTupleGenerator;
import niagarino.simulator.TupleGenerator;
import niagarino.stream.Attribute;
import niagarino.stream.Schema;

/**
 * Test cases for the Niagarino system as a whole.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class SystemTest {

   /** Position of the attribute which is framed. */
   private static final int FRAME_ATTR = 1;
   /** Maximum timestamp for tuple generator. */
   private static final long MAX_TS = 1000;
   /** Frame threshold. */
   private static final double FRAME_K = 15.0;
   /** Window size. */
   private static final long SIZE = 5;
   /** Window slide. */
   private static final long SLIDE = 5;

   /** Test data schema. */
   private static final Schema SCHEMA = new Schema(1, new Attribute("detectorid", Long.class), new Attribute(
         "starttime", Long.class), new Attribute("volume", Integer.class), new Attribute("speed",
         Integer.class), new Attribute("occupancy", Integer.class), new Attribute("status", Integer.class));
   /** Input file name. */
   private static final String STREAM_FILE = "portaldata_2011_01_25_fixed.zip";
   /** File to which to write result stream. */
   private static final String OUT_FILENAME = "outstream.csv";

   /**
    * Test case for min-max delta frames.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testMinMaxFrames() throws Exception {
      // set up operators
      final TupleGenerator generator = new DoubleTupleGenerator(1297194042969L, SystemTest.MAX_TS);
      final Operator framer = new Frame(generator.getOutputSchema(), new MinMaxFramePredicate(
            SystemTest.FRAME_ATTR, SystemTest.FRAME_K), true);
      final Operator grouper = new OrderedAggregate(framer.getOutputSchema(), new int[0], new int[] { 1 },
            BuiltInAggregationFunction.AVG);
      final Operator printer = new Print(grouper.getOutputSchema(), true, new PrintStream(new File(
            OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(generator, OperatorType.SOURCE);
      plan.addOperator(framer);
      plan.addOperator(grouper);
      plan.addOperator(printer, OperatorType.SINK);
      // streams
      plan.addStream(generator, framer);
      plan.addStream(framer, grouper);
      plan.addStream(grouper, printer);
      // execute
      plan.execute();
   }

   /**
    * Test case for windows.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testWindow() throws Exception {

      // set up operators
      final TupleGenerator generator = new DoubleTupleGenerator(1297194042969L, SystemTest.MAX_TS);
      final Operator window = new ValueWindow(generator.getOutputSchema(), SystemTest.SIZE, SystemTest.SLIDE);
      final Operator grouper = new OrderedAggregate(window.getOutputSchema(), new int[0], new int[] { 1 },
            BuiltInAggregationFunction.AVG);
      final Operator printer = new Print(grouper.getOutputSchema(), true, new PrintStream(new File(
            OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(generator, OperatorType.SOURCE);
      plan.addOperator(window);
      plan.addOperator(grouper);
      plan.addOperator(printer, OperatorType.SINK);
      // streams
      plan.addStream(generator, window);
      plan.addStream(window, grouper);
      plan.addStream(grouper, printer);
      // execute
      plan.execute();
   }

   /**
    * Test case for bi-directional stream communication.
    *
    * @throws Exception
    *            if an exception occurs
    */
   @Test
   public void testBidirectional() throws Exception {
      final Operator read = new Scan(SystemTest.SCHEMA, SystemTest.STREAM_FILE);
      final Operator multiplex = new Multiplex(read.getOutputSchema(), 1);
      final Operator print = new Print(multiplex.getOutputSchema(), false, new PrintStream(new File(
            OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(read, OperatorType.SOURCE);
      plan.addOperator(multiplex);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(read, multiplex);
      plan.addStream(multiplex, print);
      // execute
      plan.execute();
   }

   /**
    * Test case for the CSV reader.
    *
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   @Ignore
   @Test
   public void testConvertToCSV() throws Exception {
      final File source = new File("C:/Users/michagro/Downloads/tow2.cnv");
      final BufferedReader in = new BufferedReader(new FileReader(source));
      final File target = new File("C:/Users/michagro/Downloads/tow2.cnv.csv");
      final BufferedWriter out = new BufferedWriter(new FileWriter(target));
      String line = null;
      while ((line = in.readLine()) != null) {
         if (line.startsWith("#") || line.startsWith("*")) {
            out.write(line);
         } else {
            final String[] tokens = line.split(" ");
            for (int i = 0; i < tokens.length; i++) {
               final String token = tokens[i];
               if (token.length() > 0) {
                  out.write(token);
                  if (i + 1 < tokens.length) {
                     out.write(",");
                  }
               }
            }
         }
         out.newLine();
      }
      in.close();
      out.flush();
      out.close();
   }
}
