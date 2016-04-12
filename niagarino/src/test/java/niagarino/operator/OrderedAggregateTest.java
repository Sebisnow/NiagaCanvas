/*
 * @(#)OrderedAggregateTest.java   1.0   Oct 22, 2014
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
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import niagarino.operator.function.AggregationFunction;
import niagarino.operator.function.BuiltInAggregationFunction;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.simulator.RealTupleGenerator;
import niagarino.stream.Attribute;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 */
public class OrderedAggregateTest {

   /** Schema for testing purposes. */
   private static final Schema SCHEMA = new Schema(0, new Attribute("WindowValue", Integer.class),
         new Attribute("GroupValue", Integer.class), new Attribute("AggregationValue", Integer.class));

   /** Schema with Date as progressing attribute. */
   private static final Schema SCHEMA_DATE = new Schema(0, new Attribute("timestamp", Date.class),
         new Attribute("counter", Long.class), new Attribute("random", Double.class), new Attribute("letter",
               String.class));

   /** File to which to write result stream. */
   private static final String OUT_FILENAME = "outstream.csv";

   /**
    * Tests whether MIN() and MAX() return null in an OrderedAggregate or not.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testProcessTuple() throws Exception {
      final RealTupleGenerator tupleGen = new RealTupleGenerator(OrderedAggregateTest.SCHEMA, 100);
      final ValueWindow window = new ValueWindow(tupleGen.getOutputSchema(), 4, 1);
      final OrderedAggregate aggr = new OrderedAggregate(window.getOutputSchema(), new int[] { 1 },
            new int[] { 2, 2, 2, 2 }, new AggregationFunction[] { BuiltInAggregationFunction.MIN,
                  BuiltInAggregationFunction.MAX, BuiltInAggregationFunction.AVG,
                  BuiltInAggregationFunction.COUNT });
      final CheckTuples chck = new CheckTuples(aggr.getOutputSchema(),
            new PrintStream(new File(OUT_FILENAME)));

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      plan.addOperator(tupleGen, OperatorType.SOURCE);
      plan.addOperator(window);
      plan.addOperator(aggr);
      plan.addOperator(chck, OperatorType.SINK);

      plan.addStream(tupleGen, window);
      plan.addStream(window, aggr);
      plan.addStream(aggr, chck);

      plan.execute();
   }

   /**
    * Tests with Date as progressing value.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testDateSchema() throws Exception {
      final SourceOperator source = new SourceOperator("source", SCHEMA_DATE, new DateIterator());
      final ValueWindow window = new ValueWindow("window", source.getOutputSchema(), 600000, 60000,
            ZonedDateTime.parse("2015-06-08T09:00:00Z").toEpochSecond() * 1000);
      final OrderedAggregate aggr = new OrderedAggregate("aggr", window.getOutputSchema(),
            new int[] { SCHEMA_DATE.getAttributeIndex("letter") }, new int[] { 2 },
            new AggregationFunction[] { BuiltInAggregationFunction.AVG });
      final Print sink = new Print("print", aggr.getOutputSchema(), true, new PrintStream(new File(
            OUT_FILENAME)));

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(window);
      plan.addOperator(aggr);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, window);
      plan.addStream(window, aggr);
      plan.addStream(aggr, sink);

      plan.execute();
   }

   /**
    * An operator which checks the incoming tuples for certain conditions.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    */
   private static class CheckTuples extends Print {

      /**
       * Constructs a new CheckTuples operator with the given input schema.
       *
       * @param inputSchema
       *           the tuple schema of the incoming tuples
       * @param out
       *           output print stream
       */
      public CheckTuples(final Schema inputSchema, final PrintStream out) {
         super(inputSchema, false, out);
         out.println(inputSchema);
      }

      @Override
      protected void processTuple(final int input, final DataTuple tuple) {
         super.processTuple(input, tuple);
         // Check Min and Max for not null
         Assert.assertNotNull(tuple.getValues().get(2));
         Assert.assertNotNull(tuple.getValues().get(3));
      }
   }

   /**
    * Creates tuples with Date as progressing attribute.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private static class DateIterator implements TupleIterator {

      /** Maximum tuples to emit. */
      private static final long MAX_TUPLE = 100000;

      /** Random number generator with fixed seed. */
      private final Random random = new Random(314159265358979323L);

      /** Start date. */
      private long timestamp = ZonedDateTime.parse("2015-06-08T09:24:01Z").toEpochSecond();

      /** Counter for tuple count. */
      private long counter = 0;

      @Override
      public boolean hasNext() {
         return this.counter < MAX_TUPLE;
      }

      @Override
      public DataTuple next() throws NoSuchElementException {
         final DataTuple tuple = new DataTuple(SCHEMA_DATE, Arrays.asList(new Date(this.timestamp * 1000),
               this.counter, this.random.nextDouble(), new Character(
                     (char) (this.random.nextDouble() * 3 + 65)).toString()));
         this.timestamp += 10;
         this.counter++;
         return tuple;
      }

      @Override
      public ControlTuple[] nextControls() {
         return new ControlTuple[0];
      }
   }
}
