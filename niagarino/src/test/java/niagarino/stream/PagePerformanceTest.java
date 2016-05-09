/*
 * @(#)PagePerformanceTest.java   1.0   Jul 31, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.stream;

import java.io.File;
import java.io.PrintStream;

import org.junit.Test;

import niagarino.AbstractTrafficDataTest;
import niagarino.operator.Derive;
import niagarino.operator.Frame;
import niagarino.operator.Multiplex;
import niagarino.operator.NoOp;
import niagarino.operator.Operator;
import niagarino.operator.OrderedAggregate;
import niagarino.operator.Print;
import niagarino.operator.ProgressingMerge;
import niagarino.operator.Scan;
import niagarino.operator.Selection;
import niagarino.operator.ValueWindow;
import niagarino.operator.function.AccumulatorFunction;
import niagarino.operator.function.BuiltInAggregationFunction;
import niagarino.operator.function.DeltaFunction;
import niagarino.operator.function.SegmentRateDerivationFunction;
import niagarino.operator.predicate.ComparisonPredicate;
import niagarino.operator.predicate.LogicalPredicate;
import niagarino.operator.predicate.LogicalPredicate.LogicalOperator;
import niagarino.operator.predicate.MinMaxFramePredicate;
import niagarino.operator.predicate.SumFramePredicate;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;

/**
 * Performance tests to evaluate page implementation.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 */
public class PagePerformanceTest extends AbstractTrafficDataTest {

   /**
    * Test case that executes a query plan consisting of a scan and print operator.
    *
    * @throws Exception
    *            if query execution fails
    */
   @Test
   public void testScanPrint() throws Exception {
      try (final PrintStream out = new PrintStream(new File(OUT_FILENAME))) {
         // scan
         final Operator scan = new Scan(SCHEMA, IN_FILENAME);
         // print
         final Operator print = new Print(scan.getOutputSchema(), true, out);
         // physical query plan
         final PhysicalQueryPlan plan = new PhysicalQueryPlan();
         // operators
         plan.addOperator(scan, OperatorType.SOURCE);
         plan.addOperator(print, OperatorType.SINK);
         // streams
         plan.addStream(scan, print);
         // execute
         plan.execute();
      }
   }

   /**
    * Test case that executes a query plan consisting of a scan, no-op, and print operator.
    *
    * @throws Exception
    *            if query execution fails
    */
   @Test
   public void testScanNoOpPrint() throws Exception {
      try (final PrintStream out = new PrintStream(new File(OUT_FILENAME))) {
         // scan
         final Operator scan = new Scan(SCHEMA, IN_FILENAME);
         // selection
         final Operator noOp = new NoOp(scan.getOutputSchema());
         // print
         final Operator print = new Print(noOp.getOutputSchema(), true, out);
         // physical query plan
         final PhysicalQueryPlan plan = new PhysicalQueryPlan();
         // operators
         plan.addOperator(scan, OperatorType.SOURCE);
         plan.addOperator(noOp);
         plan.addOperator(print, OperatorType.SINK);
         // streams
         plan.addStream(scan, noOp);
         plan.addStream(noOp, print);
         // execute
         plan.execute();
      }
   }

   /**
    * Test case that executes a query plan consisting of a scan, selection, and print operator.
    *
    * @throws Exception
    *            if query execution fails
    */
   @Test
   public void testScanSelectionPrint() throws Exception {
      try (final PrintStream out = new PrintStream(new File(OUT_FILENAME))) {
         // scan
         final Operator scan = new Scan(SCHEMA, IN_FILENAME);
         // selection
         final Operator selection = new Selection(scan.getOutputSchema(),
               new ComparisonPredicate(3, "GT", 60));
         // print
         final Operator print = new Print(selection.getOutputSchema(), true, out);
         // physical query plan
         final PhysicalQueryPlan plan = new PhysicalQueryPlan();
         // operators
         plan.addOperator(scan, OperatorType.SOURCE);
         plan.addOperator(selection);
         plan.addOperator(print, OperatorType.SINK);
         // streams
         plan.addStream(scan, selection);
         plan.addStream(selection, print);
         // execute
         plan.execute();
      }
   }

   /**
    * Test case that executes a query plan consisting of a scan, multiplex, merge, and print operator.
    *
    * @throws Exception
    *            if query execution fails
    */
   @Test
   public void testScanMultiplexMergePrint() throws Exception {
      try (final PrintStream out = new PrintStream(new File(OUT_FILENAME))) {
         // scan
         final Operator scan = new Scan(SCHEMA, IN_FILENAME);
         // multiplex
         final Operator multiplex = new Multiplex(scan.getOutputSchema(), 2);
         // merge
         final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(),
               multiplex.getOutputSchema());
         // print
         final Operator print = new Print(merge.getOutputSchema(), true, out);
         // physical query plan
         final PhysicalQueryPlan plan = new PhysicalQueryPlan();
         // operators
         plan.addOperator(scan, OperatorType.SOURCE);
         plan.addOperator(multiplex);
         plan.addOperator(merge);
         plan.addOperator(print, OperatorType.SINK);
         // streams
         plan.addStream(scan, multiplex);
         plan.addStream(multiplex, merge);
         plan.addStream(multiplex, merge);
         plan.addStream(merge, print);
         // execute
         plan.execute();
      }
   }

   /**
    * Test case that executes a query plan consisting of a scan, multiplex, derive, merge, and print operator.
    *
    * @throws Exception
    *            if query execution fails
    */
   @Test
   public void testScanMultiplexDeriveMergePrint() throws Exception {
      try (final PrintStream out = new PrintStream(new File(OUT_FILENAME))) {
         // scan
         final Operator scan = new Scan(SCHEMA, IN_FILENAME);
         // multiplex
         final Operator multiplex = new Multiplex(scan.getOutputSchema(), 2);
         // derive
         final Operator derive = new Derive(multiplex.getOutputSchema(), new DeltaFunction(4, 2),
               new AccumulatorFunction(5, BuiltInAggregationFunction.SUM));
         // merge
         final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), derive.getOutputSchema());
         // print
         final Operator print = new Print(merge.getOutputSchema(), true, out);
         // physical query plan
         final PhysicalQueryPlan plan = new PhysicalQueryPlan();
         // operators
         plan.addOperator(scan, OperatorType.SOURCE);
         plan.addOperator(multiplex);
         plan.addOperator(derive);
         plan.addOperator(merge);
         plan.addOperator(print, OperatorType.SINK);
         // streams
         plan.addStream(scan, multiplex);
         plan.addStream(multiplex, merge);
         plan.addStream(multiplex, derive);
         plan.addStream(derive, merge);
         plan.addStream(merge, print);
         // execute
         plan.execute();
      }
   }

   /**
    * Test case that executes a query plan consisting of a scan, window, derive, and print operator.
    *
    * @throws Exception
    *            if query execution fails
    */
   @Test
   public void testScanWindowDerivePrint() throws Exception {
      try (final PrintStream out = new PrintStream(new File(OUT_FILENAME))) {
         // scan
         final Operator scan = new Scan(SCHEMA, IN_FILENAME);
         // window
         final Operator window = new ValueWindow(scan.getOutputSchema(), 300000, 60000);
         // derive
         final Operator derive = new Derive(window.getOutputSchema(), new SegmentRateDerivationFunction());
         // print
         final Operator print = new Print(derive.getOutputSchema(), true, out);
         // physical query plan
         final PhysicalQueryPlan plan = new PhysicalQueryPlan();
         // operators
         plan.addOperator(scan, OperatorType.SOURCE);
         plan.addOperator(window);
         plan.addOperator(derive);
         plan.addOperator(print, OperatorType.SINK);
         // streams
         plan.addStream(scan, window);
         plan.addStream(window, derive);
         plan.addStream(derive, print);
         // execute
         plan.execute();
      }
   }

   /**
    * Test case that executes a query plan consisting of a scan, multiplex, frame, aggregate, derive, merge,
    * and print operator.
    *
    * @throws Exception
    *            if query execution fails
    */
   @Test
   public void testScanMultiplexFrameAggregateDeriveMergePrint() throws Exception {
      try (final PrintStream out = new PrintStream(new File(OUT_FILENAME))) {
         // scan
         final Operator scan = new Scan(SCHEMA, IN_FILENAME);
         // multiplex
         final Operator multiplex = new Multiplex(scan.getOutputSchema(), 2);
         // frame
         final Operator frame = new Frame(multiplex.getOutputSchema(), new LogicalPredicate(
               LogicalOperator.OR, new SumFramePredicate(2, 30000), new MinMaxFramePredicate(3, 25)), false);
         // aggregate
         final Operator aggregate = new OrderedAggregate(frame.getOutputSchema(), new int[0],
               new int[] { 3, 3 }, BuiltInAggregationFunction.MAX, BuiltInAggregationFunction.COUNT);
         // derive
         final Operator derive = new Derive(aggregate.getOutputSchema(), new SegmentRateDerivationFunction());
         // merge
         final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), derive.getOutputSchema());
         // print
         final Operator print = new Print(merge.getOutputSchema(), true, out);
         // physical query plan
         final PhysicalQueryPlan plan = new PhysicalQueryPlan();
         // operators
         plan.addOperator(scan, OperatorType.SOURCE);
         plan.addOperator(multiplex);
         plan.addOperator(frame);
         plan.addOperator(aggregate);
         plan.addOperator(derive);
         plan.addOperator(merge);
         plan.addOperator(print, OperatorType.SINK);
         // streams
         plan.addStream(scan, multiplex);
         plan.addStream(multiplex, merge);
         plan.addStream(multiplex, frame);
         plan.addStream(frame, aggregate);
         plan.addStream(aggregate, derive);
         plan.addStream(derive, merge);
         plan.addStream(merge, print);
         // execute
         plan.execute();
      }
   }
}
