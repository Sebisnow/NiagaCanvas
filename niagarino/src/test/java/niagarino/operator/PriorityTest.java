/*
 * @(#)PriorityTest.java   1.0   Mar 7, 2011
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
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;

/**
 * Unit tests to experiment with de-prioritization of tuples.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class PriorityTest extends AbstractTrafficDataTest {

   /** File that contains the priority table. */
   private static final String PRIORITY_FILE = ClassLoader.getSystemClassLoader()
         .getResource("detector_priority.csv").getPath();

   /**
    * Test case to experiment with de-prioritization.
    *
    * @throws Exception
    *            if an exception is thrown
    */
   @Test
   public void testPrioritizer() throws Exception {
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      final Operator priority = new Priority(scan.getOutputSchema(), 0, PriorityTest.PRIORITY_FILE);
      final Operator limit = new Limit(priority.getOutputSchema(), 10000);
      final Operator window = new ValueWindow(limit.getOutputSchema(), 300000, 60000);
      final Operator print = new Print(window.getOutputSchema(), true,
            new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME)));

      // physical query plan
      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      // operators
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(priority);
      plan.addOperator(limit);
      plan.addOperator(window);
      plan.addOperator(print, OperatorType.SINK);
      // streams
      plan.addStream(scan, priority);
      plan.addStream(priority, limit);
      plan.addStream(limit, window);
      plan.addStream(window, print);
      // execute
      plan.execute();
   }
}
