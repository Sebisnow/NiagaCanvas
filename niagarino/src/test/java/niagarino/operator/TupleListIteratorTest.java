/*
 * @(#)TupleListIteratorTest.java   1.0   Jun 2, 2015
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
import niagarino.stream.Attribute;
import niagarino.stream.PunctuationControl;
import niagarino.stream.PunctuationControl.Type;
import niagarino.stream.Schema;

/**
 * Unit tests for {@link TupleListIterator}.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class TupleListIteratorTest {

   /** Small schema with one attribute. */
   private static final Schema SCHEMA_SMALL = new Schema(0, new Attribute("progValue",
         Integer.class));

   /** Schema with multiple attributes. */
   private static final Schema SCHEMA_NORMAL = new Schema(0, new Attribute("progValue",
         Integer.class), new Attribute("otherValue", String.class));

   /** Value list for multi-attribute schema. */
   private static final Object[] OBJECTS = new Object[] { Arrays.asList(10, "value"),
         Arrays.asList(20, "another value"),
         new PunctuationControl(Type.INTERVAL, 0, 30, 0, 0),
         Arrays.asList(30, "just another value") };

   /**
    * Tests small schemas with one attribute.
    * 
    * @throws QueryException
    *            if executing the query plan fails
    */
   @Test
   public void testSmallIterator() throws QueryException {
      final TupleListIterator it = new TupleListIterator(SCHEMA_SMALL, 10, 20,
            new PunctuationControl(Type.INTERVAL, 0, 30, 0, 0), 30, 40, 50);
      final SourceOperator source = new SourceOperator("source", SCHEMA_SMALL, it);
      final Print sink = new Print("print", SCHEMA_SMALL, true, System.out);

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, sink);

      plan.execute();
   }

   /**
    * Tests a schema with multiple attributes.
    * 
    * @throws QueryException
    *            if executing the query plan fails
    */
   @Test
   public void testMultiAttributeIterator() throws QueryException {
      final TupleListIterator it = new TupleListIterator(SCHEMA_NORMAL, OBJECTS);
      final SourceOperator source = new SourceOperator("source", SCHEMA_NORMAL, it);
      final Print sink = new Print("print", SCHEMA_NORMAL, true, System.out);

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, sink);

      plan.execute();
   }
}
