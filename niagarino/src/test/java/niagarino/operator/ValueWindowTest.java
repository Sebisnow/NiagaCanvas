/*
 * @(#)ValueWindowTest.java   1.0   May 26, 2015
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
import niagarino.operator.PunctuatorTest.PunctuationPrint;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.simulator.SequentialGenerationIterator;
import niagarino.simulator.SequentialGenerationIterator.Sequence;
import niagarino.stream.Attribute;
import niagarino.stream.Schema;

/**
 * Unit tests for {@link ValueWindow}.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class ValueWindowTest {

   /** Schema to use in tests. */
   private static final Schema SCHEMA = new Schema(0, new Attribute("progValue", Integer.class));

   // TODO Add tests for segment ids

   /**
    * Test case for emitted punctuations.
    *
    * @throws QueryException
    *            if executing the query plan fails
    */
   @Test
   public void testEmittedPunctuations() throws QueryException {
      final Sequence seq1 = new Sequence(0, 200, 1);
      final SequentialGenerationIterator it = new SequentialGenerationIterator(SCHEMA, Arrays.asList(seq1));

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      final SourceOperator source = new SourceOperator("source", SCHEMA, it);
      final ValueWindow window = new ValueWindow("window", SCHEMA, 15, 10);
      final PunctuationPrint sink = new PunctuationPrint();

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(window);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, window);
      plan.addStream(window, sink);

      plan.execute();
   }
}
