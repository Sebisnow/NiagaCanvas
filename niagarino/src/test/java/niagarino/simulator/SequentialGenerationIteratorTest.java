/*
 * @(#)SequentialGenerationIteratorTest.java   1.0   May 21, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.simulator;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import niagarino.operator.Print;
import niagarino.operator.SourceOperator;
import niagarino.operator.TupleIterator;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.simulator.SequentialGenerationIterator.Sequence;
import niagarino.stream.Attribute;
import niagarino.stream.Schema;

/**
 * Test cases for {@link SequentialGenerationIterator}.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class SequentialGenerationIteratorTest {

   /** Tuple count. */
   public static final int TUPLE_COUNT = 100;

   /** Schema with one progressing attribute with type {@link Long}. */
   public static final Schema SCHEMA_SMALLEST = new Schema(0, new Attribute("progValue", Long.class));

   /** Schema with multiple attributes of different types. */
   public static final Schema SCHEMA_MULTI = new Schema(0, new Attribute("progValue", Long.class),
         new Attribute("aDate", Date.class), new Attribute("anInteger", Integer.class), new Attribute(
               "aDouble", Double.class));

   /** File to which to write result stream. */
   private static final String OUT_FILENAME = "outstream.csv";

   /**
    * Creates a plan, adds the given operator as source and a print operator as sink.
    *
    * @param schema
    *           the schema of the iterator
    * @param iterator
    *           the iterator to use for the source
    * @return the created plan
    * @throws Exception
    *            if executing the query plan fails
    */
   public static PhysicalQueryPlan getPlan(final Schema schema, final TupleIterator iterator)
         throws Exception {
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      final SourceOperator source = new SourceOperator("generator-source", schema, iterator);
      final Print sink = new Print("print-sink", source.getOutputSchema(), true, new PrintStream(new File(
            OUT_FILENAME)));

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, sink);

      return plan;
   }

   /**
    * Executes a plan with one sequence and the given start and step size.
    *
    * @param schema
    *           the schema to use
    * @param start
    *           the start of the sequence
    * @param stepSize
    *           the step size of the sequence
    * @throws Exception
    *            if executing the query plan fails
    */
   public static void singleSequence(final Schema schema, final long start, final long stepSize)
         throws Exception {
      final Sequence seq = new Sequence(start, TUPLE_COUNT, stepSize);
      final SequentialGenerationIterator iterator = new SequentialGenerationIterator(schema,
            Arrays.asList(seq));

      final PhysicalQueryPlan plan = getPlan(schema, iterator);
      plan.execute();
   }

   /**
    * Executes a plan with multiple sequences.
    *
    * @param schema
    *           the schema to use
    * @param sequences
    *           the sequences to generate
    * @throws Exception
    *            if executing the query plan fails
    */
   public static void multiSequence(final Schema schema, final List<Sequence> sequences) throws Exception {
      final SequentialGenerationIterator iterator = new SequentialGenerationIterator(schema, sequences);

      final PhysicalQueryPlan plan = getPlan(schema, iterator);
      plan.execute();
   }

   /**
    * Tests one sequence with step size 1.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testSimplestSequence() throws Exception {
      singleSequence(SCHEMA_SMALLEST, 0, 1);
   }

   /**
    * Tests one sequence with step size 3.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testSimpleStepSequence() throws Exception {
      singleSequence(SCHEMA_SMALLEST, 0, 3);
   }

   /**
    * Tests one sequence with step size 0.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testStallingSequence() throws Exception {
      singleSequence(SCHEMA_SMALLEST, 0, 0);
   }

   /**
    * Tests one sequence with offset 100 and step size 3.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testOffsetSequence() throws Exception {
      singleSequence(SCHEMA_SMALLEST, 100, 3);
   }

   /**
    * Tests one sequence with multiple attributes in the schema.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testMultiAttributeSequence() throws Exception {
      singleSequence(SCHEMA_MULTI, 0, 1);
   }

   /**
    * Tests one sequence with multiple attributes in the schema, the progressing attribute being not the
    * first.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testMultiAttributeWithOffsetProgressingAttributeSequence() throws Exception {
      singleSequence(new Schema(2, SCHEMA_MULTI.getAttributes()
            .toArray(new Attribute[SCHEMA_MULTI.getSize()])), 0, 1);
   }

   /**
    * Tests one sequence with multiple attributes in the schema, the progressing attribute being the last.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testMultiAttributeWithLastProgressingAttributeSequence() throws Exception {
      singleSequence(
            new Schema(SCHEMA_MULTI.getSize() - 1, SCHEMA_MULTI.getAttributes().toArray(
                  new Attribute[SCHEMA_MULTI.getSize()])), 0, 1);
   }

   /**
    * Tests multiple sequences with and without gaps.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testSimpleSequences() throws Exception {
      multiSequence(SCHEMA_MULTI, Arrays.asList(new Sequence(-200, TUPLE_COUNT, 4), new Sequence(
            TUPLE_COUNT * 4, TUPLE_COUNT, 3), new Sequence(TUPLE_COUNT * 10, TUPLE_COUNT, 150)));
   }
}
