/*
 * @(#)StratifiedSamplingTest.java   1.0   Jul 08, 2015
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

import niagarino.AbstractTrafficDataTest;
import niagarino.operator.function.AggregationFunction;
import niagarino.operator.function.BuiltInAggregationFunction;
import niagarino.operator.function.ScalingFunction;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;

/**
 * Test Cases to Experiment Stratified Sampling operator.
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 */
public class StratifiedSamplingTest extends AbstractTrafficDataTest {

   /** Window size. */
   private static final long SIZE = 40;
   /** Window slide. */
   private static final long SLIDE = 40;
   /** Tuple limit. */
   private static final int LIMIT = 10000;

   /**
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void scanLimitWindowPrint() throws Exception {
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // print
      final Operator print = new Print(window.getOutputSchema(), true,
            new PrintStream(new File(OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(limit);
      plan.addOperator(window);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, limit);
      plan.addStream(limit, window);
      plan.addStream(window, print);
      plan.execute();
   }

   /**
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window; Stratified
    * Sampling
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testStratifiedSamplingOperator() throws Exception {
      final long numberOfStrata = 4;
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // sample
      final Operator sampling = new StratifiedSamplingOperator("Stratified-Sampling",
            window.getOutputSchema(), numberOfStrata, new ReservoirSampling(5, SIZE / numberOfStrata));
      // print
      final Operator print = new Print(sampling.getOutputSchema(), true,
            new PrintStream(new File(OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(limit);
      plan.addOperator(window);
      plan.addOperator(sampling);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, limit);
      plan.addStream(limit, window);
      plan.addStream(window, sampling);
      plan.addStream(sampling, print);
      plan.execute();
   }

   /**
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window; Stratified
    * Sampling; Aggregate
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testAggregateWithSampling() throws Exception {
      final long numberOfStrata = 2;
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // sample
      final Operator sampling = new StratifiedSamplingOperator("Stratified-Sampling",
            window.getOutputSchema(), numberOfStrata, new ReservoirSampling(10, SIZE / numberOfStrata));
      // aggregate
      final OrderedAggregate aggr = new OrderedAggregate(sampling.getOutputSchema(), new int[0],
            new int[] { 3, 3, 3, 3, 3 },
            new AggregationFunction[] { BuiltInAggregationFunction.MIN, BuiltInAggregationFunction.MAX,
                  BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT,
                  BuiltInAggregationFunction.SUM });
      // print
      final Operator print = new Print(aggr.getOutputSchema(), true, new PrintStream(new File(OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(limit);
      plan.addOperator(window);
      plan.addOperator(sampling);
      plan.addOperator(aggr);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, limit);
      plan.addStream(limit, window);
      plan.addStream(window, sampling);
      plan.addStream(sampling, aggr);
      plan.addStream(aggr, print);
      plan.execute();
   }

   /**
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window; Stratified
    * Sampling; Aggregate; Derive
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testAggregateWithSamplingDerive() throws Exception {
      final long numberOfStrata = 2;
      final int reservoirSize = 10;
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // sample
      final Operator sampling = new StratifiedSamplingOperator("Stratified-Sampling",
            window.getOutputSchema(), numberOfStrata,
            new ReservoirSampling(reservoirSize, SIZE / numberOfStrata));
      // aggregate
      final OrderedAggregate aggr = new OrderedAggregate(sampling.getOutputSchema(), new int[0],
            new int[] { 3, 3, 3, 3, 3 },
            new AggregationFunction[] { BuiltInAggregationFunction.MIN, BuiltInAggregationFunction.MAX,
                  BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT,
                  BuiltInAggregationFunction.SUM });
      final Operator derive = new Derive(aggr.getOutputSchema(),
            new ScalingFunction(5, (double) SIZE / reservoirSize, "Scaled SUM"));
      // print
      final Operator print = new Print(derive.getOutputSchema(), true,
            new PrintStream(new File(OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(limit);
      plan.addOperator(window);
      plan.addOperator(sampling);
      plan.addOperator(aggr);
      plan.addOperator(derive);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, limit);
      plan.addStream(limit, window);
      plan.addStream(window, sampling);
      plan.addStream(sampling, aggr);
      plan.addStream(aggr, derive);
      plan.addStream(derive, print);
      plan.execute();
   }
}
