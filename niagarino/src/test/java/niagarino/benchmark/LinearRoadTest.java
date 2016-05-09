/*
 * @(#)LinearRoadTest.java   1.0   Sep 11, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import javax.xml.bind.JAXBException;

import org.junit.Ignore;
import org.junit.Test;

import niagarino.QueryException;
import niagarino.operator.Operator;
import niagarino.operator.OrderedAggregate;
import niagarino.operator.Print;
import niagarino.operator.Scan;
import niagarino.operator.Selection;
import niagarino.operator.ValueWindow;
import niagarino.operator.function.AggregationFunction;
import niagarino.operator.function.AggregationFunction.Type;
import niagarino.operator.function.BuiltInAggregationFunction;
import niagarino.operator.predicate.Predicate;
import niagarino.plan.XMLReader;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Unit tests for the Linear Road benchmark implementation.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class LinearRoadTest {

   /** Folder of large data set. */
   private static final String LARGE_DATASET = "/home/sammy/Studium/2014ws/master-projekt/data/";

   /** Schema of cardata file. */
   private static final Schema CARDATA_SCHEMA = new Schema(1,
         new Attribute("type", Integer.class),
         new Attribute("time", Integer.class),
         new Attribute("vid", Integer.class),
         new Attribute("spd", Double.class),
         new Attribute("xway", Integer.class),
         new Attribute("lane", Integer.class),
         new Attribute("dir", Integer.class),
         new Attribute("seg", Integer.class),
         new Attribute("pos", Integer.class),
         new Attribute("qid", Integer.class),
         new Attribute("s_init", Integer.class),
         new Attribute("s_end", Integer.class),
         new Attribute("dow", Integer.class),
         new Attribute("tod", Integer.class),
         new Attribute("day", Integer.class));

   /**
    * Test case for the benchmark implementation v1.
    *
    * @throws Exception
    *            if an exception is thrown
    */
   @Test
   public void benchmarkv1() throws Exception {
      if (System.getProperty("CARDATA") != null) {
         new LinearRoad("src/test/resources/benchmark/linearroad-v1.xml");
      }
   }

   /**
    * Test case for the benchmark implementation v1 with the given sub version (system property {@code subversion}).
    *
    * @throws Exception
    *            if an exception is thrown
    */
   @Test
   public void benchmarkv1sub() throws Exception {
      if (System.getProperty("CARDATA") != null) {
         new LinearRoad("src/test/resources/benchmark/linearroad-v1" + System.getProperty("subversion") + ".xml");
      }
   }

   /**
    * Identify all accidents in the source data.
    */
   @Ignore
   @Test
   public void testAccidents() {
      final XMLReader reader = new XMLReader(
            "src/test/resources/niagarino/benchmark/linearroad-accidents.xml");
      PhysicalQueryPlan plan = null;
      try {
         reader.read();
         plan = reader.createPlan();
         plan.execute();
      } catch (final ClassNotFoundException | JAXBException e) {
         e.printStackTrace();
      } catch (final QueryException e) {
         e.printStackTrace();
      }
   }

   /**
    * Test case for the validation of the frequency of the position reports.
    *
    * @throws Exception
    *            if an exception occurs
    */
   @Ignore
   @Test
   public void testPositionReportFrequency() throws Exception {
      final Predicate predicateType0 = new Predicate() {

         @Override
         public boolean isApplicable(final Schema schema) {
            return schema.equals(LinearRoadTest.CARDATA_SCHEMA);
         }

         @Override
         public boolean evaluate(final DataTuple tuple) {
            return ((Integer) tuple.getAttributeValue(0)).equals(0);
         }
      };
      final Predicate predicateGreater4 = new Predicate() {

         @Override
         public boolean isApplicable(final Schema schema) {
            return true;
         }

         @Override
         public boolean evaluate(final DataTuple tuple) {
            return ((Long) tuple.getAttributeValue(2)) > 4.0;
         }
      };

      final Operator scan = new Scan(LinearRoadTest.CARDATA_SCHEMA, LinearRoadTest.LARGE_DATASET
            + "cardatapoints.out.sorted");
      final Operator select0 = new Selection(scan.getOutputSchema(), predicateType0);
      final Operator tupleWindow = new ValueWindow(select0.getOutputSchema(), 120, 60);
      final Operator aggregate = new OrderedAggregate(tupleWindow.getOutputSchema(), new int[] { 2 },
            new int[] { 8 }, new AggregationFunction[] { BuiltInAggregationFunction.forType(Type.COUNT) });
      final Operator selectG5 = new Selection(aggregate.getOutputSchema(), predicateGreater4);
      final Operator print = new Print(selectG5.getOutputSchema(), true, System.out);

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();
      plan.addOperator(scan, OperatorType.SOURCE);
      plan.addOperator(select0);
      plan.addOperator(tupleWindow);
      plan.addOperator(aggregate);
      plan.addOperator(selectG5);
      plan.addOperator(print, OperatorType.SINK);

      plan.addStream(scan, select0);
      plan.addStream(select0, tupleWindow);
      plan.addStream(tupleWindow, aggregate);
      plan.addStream(aggregate, selectG5);
      plan.addStream(selectG5, print);

      plan.execute();
   }
}
