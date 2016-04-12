/*
 * @(#)PunctuatorTest.java   1.0   May 22, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;

import org.junit.Test;

import niagarino.QueryException;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.simulator.SequentialGenerationIterator;
import niagarino.simulator.SequentialGenerationIterator.Sequence;
import niagarino.stream.Attribute;
import niagarino.stream.ControlTuple;
import niagarino.stream.ControlTuple.Type;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;

/**
 * Test cases for {@link Punctuator}.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class PunctuatorTest {

   /** Schema to use in tests. */
   private static final Schema SCHEMA = new Schema(0, new Attribute("progValue",
         Integer.class));

   /**
    * Tests one continuous sequence without any special offset, set neither by the operator nor by incoming
    * values.
    *
    * @throws QueryException
    *            if executing the query plan fails
    */
   @Test
   public void testContinuousSequence() throws QueryException {
      final Sequence seq1 = new Sequence(0, 200, 1);
      final SequentialGenerationIterator it = new SequentialGenerationIterator(SCHEMA,
            Arrays.asList(seq1));

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      final SourceOperator source = new SourceOperator("source", SCHEMA, it);
      final Punctuator punct = new Punctuator("punctuator", SCHEMA, 10);
      final PunctuationPrint sink = new PunctuationPrint();

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(punct);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, punct);
      plan.addStream(punct, sink);

      plan.execute();
   }

   /**
    * Tests values with a gap in between and an offset set by tuples. It also tests multiple tuples with the
    * same value.
    *
    * @throws QueryException
    *            if executing the query plan fails
    */
   @Test
   public void testGapfulSequence() throws QueryException {
      final Sequence seq1 = new Sequence(100, 400, 1);
      final Sequence seq2 = new Sequence(499, 10, 0);
      final Sequence seq3 = new Sequence(500, 5, 0);
      final Sequence seq4 = new Sequence(800, 201, 1);
      final SequentialGenerationIterator it = new SequentialGenerationIterator(SCHEMA,
            Arrays.asList(seq1, seq2, seq3, seq4));

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      final SourceOperator source = new SourceOperator("source", SCHEMA, it);
      final Punctuator punct = new Punctuator("punctuator", SCHEMA, 25);
      final PunctuationPrint sink = new PunctuationPrint();

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(punct);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, punct);
      plan.addStream(punct, sink);

      plan.execute();
   }

   /**
    * Tests an offset punctuation.
    * 
    * @throws QueryException
    *            if executing the query plan fails
    */
   @Test
   public void testOffsetPunctuation() throws QueryException {
      final Sequence seq1 = new Sequence(0, 20, 1);
      final SequentialGenerationIterator it = new SequentialGenerationIterator(SCHEMA,
            Arrays.asList(seq1));

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      final SourceOperator source = new SourceOperator("source", SCHEMA, it);
      final Punctuator punct = new Punctuator("punctuator", SCHEMA, 10, -50);
      final PunctuationPrint sink = new PunctuationPrint();

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(punct);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, punct);
      plan.addStream(punct, sink);

      plan.execute();
   }

   /**
    * Prints the punctuations with the context of the previous and the next tuple.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class PunctuationPrint extends AbstractOperator {

      /** The latest seen tuple. */
      private DataTuple last;

      /**
       * Constructs a new {@link PunctuationPrint}.
       */
      public PunctuationPrint() {
         super("print-sink", Arrays.asList(SCHEMA));
         this.last = null;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void processTuple(final int input, final DataTuple tuple) {
         if (this.last == null) {
            System.out.println(tuple.getValues().get(0));
         }
         this.last = tuple;
         this.pushTuple(tuple);
      }

      /**
       * Processes the given control message from the given input along the forward stream flow. This default
       * implementation simply pushes the control messages forward.
       *
       * @param input
       *           input number
       * @param message
       *           control message
       */
      @Override
      protected void processForwardControl(final int input, final ControlTuple message) {
         if (Type.PUNCTUATION.equals(message.getType())) {
            final PunctuationControl ctrl = (PunctuationControl) message;
            if (this.last != null) {
               System.out.println(this.last.getValues().get(0));
            }
            System.out.println("Type: " + ctrl.getPunctuationType().name() + " Start: "
                  + ctrl.getStartValue() + " SegStart: " + ctrl.getSegmentStart()
                  + " SegEnd: " + ctrl.getSegmentEnd() + " Step: " + ctrl.getStepSize()
                  + " SegId: " + ctrl.getSegmentId());
            this.last = null;
         } else {
            this.pushControl(Flow.FORWARD, message);
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void handleEoS(final Socket socket, final int input, final ControlTuple message) {
         if (this.last != null) {
            System.out.println(this.last.getValues().get(0));
         }
         super.handleEoS(socket, input, message);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public Schema getOutputSchema() {
         return this.getInputSchemas().get(0);
      }
   }
}
