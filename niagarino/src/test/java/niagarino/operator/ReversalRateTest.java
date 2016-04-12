/*
 * @(#)ReversalRateTest.java   1.0   Mar 21, 2011
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
import niagarino.operator.function.ReversalRateAggregationFunction;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;

/**
 * Test cases to experiment with reversal rates.
 *
 * @author Michael Grossniklaus &lt;michagro@cs.pdx.edu&gt;
 * @version 1.0
 */
public class ReversalRateTest extends AbstractTrafficDataTest {

   /**
    * Test case for reversal rate aggregation function.
    *
    * @throws Exception
    *            if an input or output file cannot be accessed
    */
   @Test
   public void testReversalRate() throws Exception {
      // scan
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // segment
      // final Operator segment = new Frame(scan.getOutputSchema(), new SumFramePredicate(2, 90), false);
      final Operator segment = new ValueWindow(scan.getOutputSchema(), 300000, 60000);
      // aggregate
      final Operator aggregate = new OrderedAggregate(segment.getOutputSchema(), new int[] { 0 },
            new int[] { 3 }, new ReversalRateAggregationFunction());
      // print
      final Operator print = new Print(aggregate.getOutputSchema(), true,
            new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(segment);
      plan.addOperator(aggregate);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, segment);
      plan.addStream(segment, aggregate);
      plan.addStream(aggregate, print);

      plan.execute();
   }
}
