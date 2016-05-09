/*
 * @(#)SourceOperatorTest.java   1.0   May 21, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.Test;

import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.stream.Attribute;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.PunctuationControl.Type;
import niagarino.stream.Schema;

/**
 * Test cases for {@link SourceOperator}.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class SourceOperatorTest {

   /** Small schema for testing purposes. */
   private static final Schema SCHEMA = new Schema(0, new Attribute("progValue", Long.class));

   /** Amount of tuples to emit. */
   private static final long TUPLE_COUNT = 10000;

   /** File to which to write result stream. */
   private static final String OUT_FILENAME = "outstream.csv";

   /**
    * Tests minimal operation of the {@link SourceOperator}.
    *
    * @throws Exception
    *            if executing the query plan fails
    */
   @Test
   public void testOperation() throws Exception {
      final SourceOperator source = new SourceOperator("source", SourceOperatorTest.SCHEMA,
            new TestIterator());
      final Print sink = new Print("sink", SourceOperatorTest.SCHEMA, true, new PrintStream(new File(
            OUT_FILENAME)));

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, sink);

      plan.execute();
   }

   /**
    * A {@link TupleIterator} for test purposes.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class TestIterator implements TupleIterator {

      /** Current count for the progressing value. */
      private long currentCount = 0;

      @Override
      public boolean hasNext() {
         return this.currentCount < SourceOperatorTest.TUPLE_COUNT;
      }

      @Override
      public DataTuple next() throws NoSuchElementException {
         if (this.hasNext()) {
            return new DataTuple(SourceOperatorTest.SCHEMA, Arrays.asList(this.currentCount++));
         }
         throw new NoSuchElementException();
      }

      @Override
      public ControlTuple[] nextControls() {
         if (this.currentCount % 1000 == 0) {
            return new ControlTuple[] { new PunctuationControl(Type.INTERVAL, 0, 1000,
                  this.currentCount / 1000, this.currentCount - 1000) };
         }
         return new ControlTuple[0];
      }
   }
}
