/*
 * @(#)SegmentTest.java   1.0   Feb 15, 2011
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
import niagarino.operator.function.AccumulatorFunction;
import niagarino.operator.function.BuiltInAggregationFunction;
import niagarino.operator.function.DeltaFunction;
import niagarino.operator.function.SegmentRateDerivationFunction;
import niagarino.operator.function.VhtFunction;
import niagarino.operator.predicate.LogicalPredicate;
import niagarino.operator.predicate.LogicalPredicate.LogicalOperator;
import niagarino.operator.predicate.MaxRMSPredicate;
import niagarino.operator.predicate.MinMaxFramePredicate;
import niagarino.operator.predicate.Predicate;
import niagarino.operator.predicate.SumFramePredicate;
import niagarino.operator.predicate.TimeEqualityPredicate;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;

/**
 * Test cases for various <code>Segment</code> implementations.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class SegmentTest extends AbstractTrafficDataTest {

   /** Delta between minimum and maximum. */
   private static final int MINMAX = 25;
   /** Maximum allowed error. */
   private static final int MAX_ERROR = 16;
   /** Keep satisfying tuple in stream. */
   private static final boolean SATISFYING_TUPLE_IN_FRAME = true;
   /** Minimum frame threshold. */
   private static final int K_MIN = 110;
   /** Maximum frame threshold. */
   private static final int K_MAX = 110;
   /** Frame threshold increment. */
   private static final int K_INC = 1;
   /** Minimum window size. */
   private static final int SIZE_MIN = 300000;
   /** Maximum window size. */
   private static final int SIZE_MAX = 300000;
   /** Window size increment. */
   private static final int SIZE_INC = 20000;

   /**
    * Test case for using RMS error on average speed.
    *
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   @Test
   public void testRMSFrameForAvgSpeed() throws Exception {
      try (final PrintStream out = new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME))) {
         final PhysicalQueryPlan plan = this.queryRMSFrameForAvgSpeed(MAX_ERROR, true, out);
         plan.execute();
      }
   }

   /**
    * Test case for using sum frames on average speed.
    *
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   @Test
   public void testSumFrameForAvgSpeed() throws Exception {
      try (final PrintStream out = new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME))) {
         for (int k = SegmentTest.K_MIN; k <= SegmentTest.K_MAX; k += SegmentTest.K_INC) {
            final PhysicalQueryPlan plan = this.querySumFrameForAvgSpeed(k, k == SegmentTest.K_MIN, out);
            plan.execute();
         }
      }
   }

   /**
    * Test case for using delta frames on average speed.
    *
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   @Test
   public void testDeltaFrameForAvgSpeed() throws Exception {
      try (final PrintStream out = new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME))) {
         for (int k = SegmentTest.K_MIN; k <= SegmentTest.K_MAX; k += SegmentTest.K_INC) {
            final PhysicalQueryPlan plan = this.queryDeltaFrameForAvgSpeed(k, k == SegmentTest.K_MIN, out);
            plan.execute();
         }
      }
   }

   /**
    * Test case for using volume frames on average speed.
    *
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   @Test
   public void testVolumeFrameForAvgSpeed() throws Exception {
      try (final PrintStream out = new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME))) {
         for (int k = SegmentTest.K_MIN; k <= SegmentTest.K_MAX; k += SegmentTest.K_INC) {
            final PhysicalQueryPlan plan = this.queryVolumeFrameForAvgSpeed(k, k == SegmentTest.K_MIN, out);
            plan.execute();
         }
      }
   }

   /**
    * Test case for using windows on average speed.
    *
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   @Test
   public void testWindowForAvgSpeed() throws Exception {
      try (final PrintStream out = new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME))) {
         for (int k = SegmentTest.SIZE_MIN; k <= SegmentTest.SIZE_MAX; k += SegmentTest.SIZE_INC) {
            final PhysicalQueryPlan plan = this.queryWindowForAvgSpeed(k, k, k == SegmentTest.SIZE_MIN, out);
            plan.setStatisticsEnabled(true);
            plan.execute();
            plan.printStatistics(System.out);
         }
      }
   }

   /**
    * Test case for using sum frames on average vht.
    *
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   @Test
   public void testSumFrameForAvgVht() throws Exception {
      try (final PrintStream out = new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME))) {
         for (int k = SegmentTest.K_MIN; k <= SegmentTest.K_MAX; k += SegmentTest.K_INC) {
            final PhysicalQueryPlan plan = this.querySumFrameForAvgVht(k, k == SegmentTest.K_MIN, out);
            plan.execute();
         }
      }
   }

   /**
    * Test case for using windows on average vht.
    *
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   @Test
   public void testWindowForAvgVht() throws Exception {
      try (final PrintStream out = new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME))) {
         for (int k = SegmentTest.SIZE_MIN; k <= SegmentTest.SIZE_MAX; k += SegmentTest.SIZE_INC) {
            final PhysicalQueryPlan plan = this.queryWindowForAvgVht(k, k, k == SegmentTest.SIZE_MIN, out);
            plan.execute();
         }
      }
   }

   /**
    * Constructs a physical query plan that uses sum frames to compute average speed.
    *
    * @param frameK
    *           frame threshold
    * @param printSchema
    *           indicates whether the schema is printed
    * @param out
    *           output stream
    * @return physical query plan
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   private PhysicalQueryPlan querySumFrameForAvgSpeed(final int frameK, final boolean printSchema,
         final PrintStream out) throws Exception {
      // scan
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // multiplex
      final Operator multiplex = new Multiplex(scan.getOutputSchema(), 2);
      // frame
      final Predicate framePredicate = new LogicalPredicate(LogicalOperator.OR,
            new SumFramePredicate(2, frameK), new MinMaxFramePredicate(3, SegmentTest.MINMAX));
      final Operator frame = new Frame(multiplex.getOutputSchema(), framePredicate, false);
      // final Operator frame = new SumFrame(multiplex.getOutputSchema(), 2, frameK);
      // aggregate
      final Operator aggregate = new OrderedAggregate(frame.getOutputSchema(), new int[0], new int[] { 3, 3 },
            BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT);
      // derive segment rate
      final Operator deriveRate = new Derive(aggregate.getOutputSchema(),
            new SegmentRateDerivationFunction());
      // merge
      final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), deriveRate.getOutputSchema());
      // derive
      final Operator deriveError = new Derive("derive-error", merge.getOutputSchema(),
            new DeltaFunction(3, 6), new AccumulatorFunction(9, BuiltInAggregationFunction.SUM));
      // selection
      final Operator selection = new Selection(deriveError.getOutputSchema(),
            new TimeEqualityPredicate(1, 23, 59, 40));
      // print
      final Operator print = new Print(selection.getOutputSchema(), printSchema, out);

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(multiplex);
      plan.addOperator(frame);
      plan.addOperator(aggregate);
      plan.addOperator(deriveRate);
      plan.addOperator(merge);
      plan.addOperator(deriveError);
      // plan.addOperator(selection);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, multiplex);
      plan.addStream(multiplex, merge);
      plan.addStream(multiplex, frame);
      plan.addStream(frame, aggregate);
      plan.addStream(aggregate, deriveRate);
      plan.addStream(deriveRate, merge);
      plan.addStream(merge, deriveError);
      // plan.addStream(deriveError, selection);
      plan.addStream(deriveError, print);

      return plan;
   }

   /**
    * Constructs a physical query plan that uses windows to compute average speed.
    *
    * @param size
    *           window size
    * @param slide
    *           window slide
    * @param printSchema
    *           indicates whether the schema is printed
    * @param out
    *           output stream
    * @return physical query plan
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   private PhysicalQueryPlan queryWindowForAvgSpeed(final int size, final int slide,
         final boolean printSchema, final PrintStream out) throws Exception {
      // scan
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // multiplex
      final Operator multiplex = new Multiplex(scan.getOutputSchema(), 2);
      // window
      final Operator window = new ValueWindow(multiplex.getOutputSchema(), size, slide);
      // aggregate
      final Operator aggregate = new OrderedAggregate(window.getOutputSchema(), new int[0],
            new int[] { 3, 3 }, BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT);
      // merge
      final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), aggregate.getOutputSchema());
      // derive
      final Operator derive = new Derive(merge.getOutputSchema(), new DeltaFunction(3, 6),
            new AccumulatorFunction(8, BuiltInAggregationFunction.SUM));
      // selection
      final Operator selection = new Selection(derive.getOutputSchema(),
            new TimeEqualityPredicate(1, 23, 59, 40));
      // print
      final Operator print = new Print(selection.getOutputSchema(), printSchema, out);

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(multiplex);
      plan.addOperator(window);
      plan.addOperator(aggregate);
      plan.addOperator(merge);
      plan.addOperator(derive);
      // plan.addOperator(selection);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, multiplex);
      plan.addStream(multiplex, merge);
      plan.addStream(multiplex, window);
      plan.addStream(window, aggregate);
      plan.addStream(aggregate, merge);
      plan.addStream(merge, derive);
      // plan.addStream(derive, selection);
      plan.addStream(derive, print);

      return plan;
   }

   /**
    * Constructs a physical query plan that uses frames to compute average vht.
    *
    * @param frameK
    *           frame threshold
    * @param printSchema
    *           indicates whether the schema is printed
    * @param out
    *           output stream
    * @return physical query plan
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   private PhysicalQueryPlan querySumFrameForAvgVht(final double frameK, final boolean printSchema,
         final PrintStream out) throws Exception {
      // read
      final Operator read = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // derive vht
      final Operator deriveVht = new Derive(read.getOutputSchema(), new VhtFunction(2, 3, 1.0));
      // multiplex
      final Operator multiplex = new Multiplex(deriveVht.getOutputSchema(), 2);
      // frame
      final Operator frame = new Frame(multiplex.getOutputSchema(), new SumFramePredicate(2, frameK), false);
      // aggregate
      final Operator aggregate = new OrderedAggregate(frame.getOutputSchema(), new int[0], new int[] { 6, 6 },
            BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT);
      // merge
      final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), aggregate.getOutputSchema());
      // derive error
      final Operator deriveError = new Derive("derive-error", merge.getOutputSchema(),
            new DeltaFunction(6, 7), new AccumulatorFunction(9, BuiltInAggregationFunction.SUM));
      // selection
      final Operator selection = new Selection(deriveError.getOutputSchema(),
            new TimeEqualityPredicate(1, 23, 59, 40));
      // print
      final Operator print = new Print(selection.getOutputSchema(), printSchema, out);

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(read, OperatorType.SOURCE);
      plan.addOperator(deriveVht);
      plan.addOperator(multiplex);
      plan.addOperator(frame);
      plan.addOperator(aggregate);
      plan.addOperator(merge);
      plan.addOperator(deriveError);
      plan.addOperator(selection);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(read, deriveVht);
      plan.addStream(deriveVht, multiplex);
      plan.addStream(multiplex, merge);
      plan.addStream(multiplex, frame);
      plan.addStream(frame, aggregate);
      plan.addStream(aggregate, merge);
      plan.addStream(merge, deriveError);
      plan.addStream(deriveError, selection);
      plan.addStream(selection, print);

      return plan;
   }

   /**
    * Constructs a physical query plan that uses windows to compute average vht.
    *
    * @param size
    *           window size
    * @param slide
    *           window slide
    * @param printSchema
    *           indicates whether the schema is printed
    * @param out
    *           output stream
    * @return physical query plan
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   private PhysicalQueryPlan queryWindowForAvgVht(final int size, final int slide, final boolean printSchema,
         final PrintStream out) throws Exception {
      // read
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // derive vht
      final Operator deriveVht = new Derive(scan.getOutputSchema(), new VhtFunction(2, 3, 1.0));
      // multiplex
      final Operator multiplex = new Multiplex(deriveVht.getOutputSchema(), 2);
      // window
      final Operator window = new ValueWindow(multiplex.getOutputSchema(), size, slide);
      // aggregate
      final Operator aggregate = new OrderedAggregate(window.getOutputSchema(), new int[0],
            new int[] { 6, 6 }, BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT);
      // merge
      final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), aggregate.getOutputSchema());
      // derive error
      final Operator deriveError = new Derive("derive-error", merge.getOutputSchema(),
            new DeltaFunction(6, 7), new AccumulatorFunction(9, BuiltInAggregationFunction.SUM));
      // selection
      final Operator selection = new Selection(deriveError.getOutputSchema(),
            new TimeEqualityPredicate(1, 23, 59, 40));
      // print
      final Operator print = new Print(selection.getOutputSchema(), printSchema, out);

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(deriveVht);
      plan.addOperator(multiplex);
      plan.addOperator(window);
      plan.addOperator(aggregate);
      plan.addOperator(merge);
      plan.addOperator(deriveError);
      plan.addOperator(selection);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, deriveVht);
      plan.addStream(deriveVht, multiplex);
      plan.addStream(multiplex, merge);
      plan.addStream(multiplex, window);
      plan.addStream(window, aggregate);
      plan.addStream(aggregate, merge);
      plan.addStream(merge, deriveError);
      plan.addStream(deriveError, selection);
      plan.addStream(selection, print);

      return plan;
   }

   /**
    * Constructs a physical query plan that uses delta frames to compute average speed.
    *
    * @param frameK
    *           frame threshold
    * @param printSchema
    *           indicates whether the schema is printed
    * @param out
    *           output stream
    * @return physical query plan
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   private PhysicalQueryPlan queryDeltaFrameForAvgSpeed(final int frameK, final boolean printSchema,
         final PrintStream out) throws Exception {
      // scan
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // multiplex
      final Operator multiplex = new Multiplex(scan.getOutputSchema(), 2);
      // frame
      final Predicate framePredicate = new MinMaxFramePredicate(3, frameK);

      final Operator frame = new Frame(multiplex.getOutputSchema(), framePredicate, false);
      // final Operator frame = new SumFrame(multiplex.getOutputSchema(), 2, frameK);
      // aggregate
      final Operator aggregate = new OrderedAggregate(frame.getOutputSchema(), new int[0], new int[] { 3, 3 },
            BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT);
      // derive segment rate
      final Operator deriveRate = new Derive(aggregate.getOutputSchema(),
            new SegmentRateDerivationFunction());
      // merge
      final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), deriveRate.getOutputSchema());
      // derive
      final Operator deriveError = new Derive("derive-error", merge.getOutputSchema(),
            new DeltaFunction(3, 6), new AccumulatorFunction(9, BuiltInAggregationFunction.SUM));
      // selection
      final Operator selection = new Selection(deriveError.getOutputSchema(),
            new TimeEqualityPredicate(1, 23, 59, 40));
      // print
      final Operator print = new Print(selection.getOutputSchema(), printSchema, out);

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(multiplex);
      plan.addOperator(frame);
      plan.addOperator(aggregate);
      plan.addOperator(deriveRate);
      plan.addOperator(merge);
      plan.addOperator(deriveError);
      // plan.addOperator(selection);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, multiplex);
      plan.addStream(multiplex, merge);
      plan.addStream(multiplex, frame);
      plan.addStream(frame, aggregate);
      plan.addStream(aggregate, deriveRate);
      plan.addStream(deriveRate, merge);
      plan.addStream(merge, deriveError);
      // plan.addStream(deriveError, selection);
      plan.addStream(deriveError, print);

      return plan;
   }

   /**
    * Constructs a physical query plan that uses volume frames to compute average speed.
    *
    * @param frameK
    *           frame threshold
    * @param printSchema
    *           indicates whether the schema is printed
    * @param out
    *           output stream
    * @return physical query plan
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   private PhysicalQueryPlan queryVolumeFrameForAvgSpeed(final int frameK, final boolean printSchema,
         final PrintStream out) throws Exception {
      // scan
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // multiplex
      final Operator multiplex = new Multiplex(scan.getOutputSchema(), 2);
      // frame
      final Predicate framePredicate = new SumFramePredicate(2, frameK);
      final Operator frame = new Frame(multiplex.getOutputSchema(), framePredicate, false);
      // final Operator frame = new SumFrame(multiplex.getOutputSchema(), 2, frameK);
      // aggregate
      final Operator aggregate = new OrderedAggregate(frame.getOutputSchema(), new int[0], new int[] { 3, 3 },
            BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT);
      // derive segment rate
      final Operator deriveRate = new Derive(aggregate.getOutputSchema(),
            new SegmentRateDerivationFunction());
      // merge
      final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), deriveRate.getOutputSchema());
      // derive
      final Operator deriveError = new Derive("derive-error", merge.getOutputSchema(),
            new DeltaFunction(3, 6), new AccumulatorFunction(9, BuiltInAggregationFunction.SUM));
      // selection
      final Operator selection = new Selection(deriveError.getOutputSchema(),
            new TimeEqualityPredicate(1, 23, 59, 40));
      // print
      final Operator print = new Print(selection.getOutputSchema(), printSchema, out);

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(multiplex);
      plan.addOperator(frame);
      plan.addOperator(aggregate);
      plan.addOperator(deriveRate);
      plan.addOperator(merge);
      plan.addOperator(deriveError);
      // plan.addOperator(selection);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, multiplex);
      plan.addStream(multiplex, merge);
      plan.addStream(multiplex, frame);
      plan.addStream(frame, aggregate);
      plan.addStream(aggregate, deriveRate);
      plan.addStream(deriveRate, merge);
      plan.addStream(merge, deriveError);
      // plan.addStream(deriveError, selection);
      plan.addStream(deriveError, print);

      return plan;
   }

   /**
    * Constructs a physical query plan that uses RMS frames to compute average speed.
    *
    * @param maxError
    *           maximum RMS error
    * @param printSchema
    *           indicates whether the schema is printed
    * @param out
    *           output stream
    * @return physical query plan
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   private PhysicalQueryPlan queryRMSFrameForAvgSpeed(final int maxError, final boolean printSchema,
         final PrintStream out) throws Exception {
      // scan
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // multiplex
      final Operator multiplex = new Multiplex(scan.getOutputSchema(), 2);
      // frame
      final Predicate framePredicate = new MaxRMSPredicate(3, maxError, SATISFYING_TUPLE_IN_FRAME);
      final Operator frame = new Frame(multiplex.getOutputSchema(), framePredicate, false,
            SATISFYING_TUPLE_IN_FRAME);
      // final Operator frame = new SumFrame(multiplex.getOutputSchema(), 2, frameK);
      // aggregate
      final Operator aggregate = new OrderedAggregate(frame.getOutputSchema(), new int[0], new int[] { 3, 3 },
            BuiltInAggregationFunction.AVG, BuiltInAggregationFunction.COUNT);
      // derive segment rate
      final Operator deriveRate = new Derive(aggregate.getOutputSchema(),
            new SegmentRateDerivationFunction());
      // merge
      final Operator merge = new ProgressingMerge(multiplex.getOutputSchema(), deriveRate.getOutputSchema());
      // derive
      final Operator deriveError = new Derive("derive-error", merge.getOutputSchema(),
            new DeltaFunction(3, 6), new AccumulatorFunction(9, BuiltInAggregationFunction.SUM));
      // selection
      // final Operator selection = new Selection(deriveError.getOutputSchema(),
      // new TimeEqualityPredicate(1, 23, 59, 40));
      // print
      final Operator print = new Print(deriveError.getOutputSchema(), printSchema, out);

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(multiplex);
      plan.addOperator(frame);
      plan.addOperator(aggregate);
      plan.addOperator(deriveRate);
      plan.addOperator(merge);
      plan.addOperator(deriveError);
      // plan.addOperator(selection);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, multiplex);
      plan.addStream(multiplex, merge);
      plan.addStream(multiplex, frame);
      plan.addStream(frame, aggregate);
      plan.addStream(aggregate, deriveRate);
      plan.addStream(deriveRate, merge);
      plan.addStream(merge, deriveError);
      // plan.addStream(deriveError, selection);
      plan.addStream(deriveError, print);

      return plan;
   }
}
