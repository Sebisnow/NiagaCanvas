/*
 * @(#)ReservoirSamplingTest.java   1.0   Jul 05, 2015
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
 * Test Cases to Experiment ResevoirSampling operator.
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 */
public class ReservoirSamplingTest extends AbstractTrafficDataTest {

   /** Window size. */
   private static final long SIZE = 1000;
   /** Window slide. */
   private static final long SLIDE = 1000;
   /** Tuple limit. */
   private static final int LIMIT = 10000;

   /**
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window; Reservoir Sampling
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testReservoirSampling() throws Exception {
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // sample
      final Operator sampling = new SamplingOperator("Reservoir-Sampling", window.getOutputSchema(),
            new ReservoirSampling(6, SIZE));
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
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window; Reservoir
    * Sampling; Seed
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testReservoirSamplingWithSeed() throws Exception {
      final long seed = 123456789L;
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // sample
      final Operator sampling = new SamplingOperator("Reservoir-Sampling", window.getOutputSchema(),
            new ReservoirSampling(5, SIZE, seed));
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
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Value Window; Reservoir Sampling
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testProcessValueWindow() throws Exception {
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final ValueWindow window = new ValueWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // sample
      final Operator sampling = new SamplingOperator("Reservoir-Sampling", window.getOutputSchema(),
            new ReservoirSampling(6, SIZE));
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
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window; Group By
    * Aggregate; No Sampling Operator
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testAverageMinMaxWithoutSampling() throws Exception {
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // aggregate
      final OrderedAggregate aggr = new OrderedAggregate(window.getOutputSchema(), new int[] { 0 },
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
      plan.addOperator(aggr);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, limit);
      plan.addStream(limit, window);
      plan.addStream(window, aggr);
      plan.addStream(aggr, print);
      plan.execute();
   }

   /**
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window; Reservoir
    * Sampling; Group By Aggregate
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testAverageMinMaxWithSampling() throws Exception {
      // scan
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), ReservoirSamplingTest.SIZE,
            ReservoirSamplingTest.SLIDE);
      // sample
      final Operator sampling = new SamplingOperator("Reservoir-Sampling", window.getOutputSchema(),
            new ReservoirSampling(6, ReservoirSamplingTest.SIZE));
      // aggregate
      final OrderedAggregate aggr = new OrderedAggregate(sampling.getOutputSchema(), new int[] { 0 },
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
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window; No Sampling;
    * Aggregate; No grouping
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testAverageMinMaxWithoutSamplingNoGrp() throws Exception {
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // aggregate
      final OrderedAggregate aggr = new OrderedAggregate(window.getOutputSchema(), new int[0],
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
      plan.addOperator(aggr);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, limit);
      plan.addStream(limit, window);
      plan.addStream(window, aggr);
      plan.addStream(aggr, print);
      plan.execute();
   }

   /**
    * Test case for method {@link SamplingOperator#processTuple(int, Tuple)}. Tuple Window; Reservoir
    * Sampling; Aggregate(No group by); derive
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testAverageMinMaxWithSamplingNoGrpDerive() throws Exception {
      final int reservoirSize = 5;
      // scan
      final Operator scan = new Scan(SCHEMA, IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), LIMIT);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), SIZE, SLIDE);
      // sample
      final Operator sampling = new SamplingOperator("Reservoir-Sampling", window.getOutputSchema(),
            new ReservoirSampling(reservoirSize, SIZE));
      // aggregate
      final OrderedAggregate aggr = new OrderedAggregate(sampling.getOutputSchema(), new int[0],
            new int[] { 3, 3, 3, 3, 3 },
            new AggregationFunction[] { BuiltInAggregationFunction.MIN, BuiltInAggregationFunction.MAX,
                  BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT,
                  BuiltInAggregationFunction.SUM });
      // derive
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
