/*
 * @(#)BernoulliSamplingTest.java   1.0   May 30, 2015
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
 * Test Cases to Experiment BernoulliSampling & BernoulliSamplingEfficient operator.
 *
 * @author Madhurima Varughese &lt;madhurima.varughese@uni.kn&gt;
 * @author Helna Kuttickattu &lt;helna.kuttickattu@uni.kn&gt;
 * @version 1.0
 */
public class BernoulliSamplingTest extends AbstractTrafficDataTest {

   /** Window size. */
   private static final long SIZE = 10;

   /** Window slide. */
   private static final long SLIDE = 10;

   /**
    * Test case for Bernoulli Sampling Algorithm.
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testBernoulliSampling() throws Exception {
      // scan
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), 100);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), BernoulliSamplingTest.SIZE,
            BernoulliSamplingTest.SLIDE);
      // sample
      final Operator sampling = new SamplingOperator("Bernoulli-Sampling", window.getOutputSchema(),
            new BernoulliSampling(40));
      // print
      final Operator print = new Print(sampling.getOutputSchema(), true,
            new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME)));

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
    * Test Case for Bernoulli Sampling Algorithm(Efficient Version).
    *
    * @throws Exception
    *            if the input or output files cannot be accessed
    */
   @Test
   public void testBernoulliSamplingEfficient() throws Exception {
      // scan
      final Operator scan = new Scan(AbstractTrafficDataTest.SCHEMA, AbstractTrafficDataTest.IN_FILENAME);
      // limit
      final Operator limit = new Limit(scan.getOutputSchema(), 100);
      // window
      final TupleWindow window = new TupleWindow(limit.getOutputSchema(), BernoulliSamplingTest.SIZE,
            BernoulliSamplingTest.SLIDE);
      // sample
      final Operator sampling = new SamplingOperator("Bernoulli-Sampling-Efficient)",
            window.getOutputSchema(), new BernoulliSamplingEfficient(40, BernoulliSamplingTest.SIZE));
      // print
      final Operator print = new Print(sampling.getOutputSchema(), true,
            new PrintStream(new File(AbstractTrafficDataTest.OUT_FILENAME)));

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
}
