/*
 * @(#)BinningFrameTest.java   1.0   Mar 22, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.File;
import java.io.PrintStream;

import org.junit.Test;

import niagarino.operator.function.BuiltInAggregationFunction;
import niagarino.operator.predicate.BinningFramePredicate;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.stream.Attribute;
import niagarino.stream.Schema;

/**
 * Unit tests for the implementation of binning frames.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class BinningFrameTest {

   /** Schema of tuples in test data set. */
   // scan,temperature,conductivity,depth,salinity,flSP,flSP1,latitude,longitude,timeJ
   private static final Schema SCHEMA = new Schema(0, new Attribute("scan", Long.class),
         new Attribute("temperature", Double.class), new Attribute("conductivity", Double.class),
         new Attribute("depth", Double.class), new Attribute("salinity", Double.class),
         new Attribute("flSP", Double.class), new Attribute("flSP1", Double.class),
         new Attribute("latitude", Double.class), new Attribute("longitude", Double.class),
         new Attribute("timeJ", Double.class));
   /** File that contains test data set. */
   private static final String IN_FILENAME = "tow2.zip";
   /** File to which to write result stream. */
   private static final String OUT_FILENAME = "outstream.csv";

   /**
    * Test case for binning frames.
    *
    * @throws Exception
    *            if an exception is thrown
    */
   @Test
   public void testBinningFrame() throws Exception {
      // scan
      final Operator scan = new Scan(BinningFrameTest.SCHEMA, BinningFrameTest.IN_FILENAME);
      // multiplex
      final Operator multiplex = new Multiplex(scan.getOutputSchema(), 2);
      // segment
      final Operator segment = new Frame(multiplex.getOutputSchema(), new BinningFramePredicate(3, 1.0, 0.0),
            false);
      // aggregate
      final Operator aggregate = new OrderedAggregate(segment.getOutputSchema(), new int[0],
            new int[] { 1, 2, 4 }, BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.MAX,
            BuiltInAggregationFunction.AVG);
      // merge
      final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), aggregate.getOutputSchema());
      // print
      final Operator print = new Print(merge.getOutputSchema(), true,
            new PrintStream(new File(OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(multiplex);
      plan.addOperator(segment);
      plan.addOperator(aggregate);
      plan.addOperator(merge);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, multiplex);
      plan.addStream(multiplex, merge);
      plan.addStream(multiplex, segment);
      plan.addStream(segment, aggregate);
      plan.addStream(aggregate, merge);
      plan.addStream(merge, print);

      plan.execute();
   }
}
