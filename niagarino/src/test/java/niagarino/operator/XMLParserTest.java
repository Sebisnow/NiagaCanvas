/*
 * @(#)XMLParserTest.java   1.0   Jun 1, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import niagarino.QueryException;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.Schema;

/**
 * Unit tests for {@link XMLParser}.
 *
 * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class XMLParserTest {

   /** Schema to use during tests. */
   private static final Schema SCHEMA = new Schema(0, new Attribute("xml", String.class));

   /** Schema of emitted tuples. */
   private static final Schema SCHEMA_OUTPUT = new Schema(0, new Attribute("progValue", Integer.class),
         new Attribute("value", Double.class));

   // TODO add second schema with integer as attribute.

   /** XML data. */
   private static final Object[] XML = new Object[] {
         "<test>",
         "   <values>",
         "      <value>1</value>",
         "      <value>2</value>",
         "   </values>",
         "   <values>",
         "      <value>5</value>",
         "      <value>3.141</value>",
         "   </values>",
         "</test>",
         new PunctuationControl(PunctuationControl.Type.END_OF_ENTITY, 0, 0, 0, 0)
   };

   /**
    * Test case.
    *
    * @throws SAXException
    *            some exception
    * @throws ParserConfigurationException
    *            if SAX is not configured properly
    * @throws QueryException
    *            if an exception occurs with the query
    */
   @Test
   public void testXMLParser() throws ParserConfigurationException, SAXException, QueryException {
      final TupleListIterator it = new TupleListIterator(SCHEMA, XML);
      final SourceOperator source = new SourceOperator("source", SCHEMA, it);
      final XMLParser parser = new XMLParser("parser", SCHEMA, new Emitter());
      final Print print = new Print(SCHEMA_OUTPUT, true, System.out);

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(parser);
      plan.addOperator(print, OperatorType.SINK);

      // plan.addStream(source, print);
      plan.addStream(source, parser);
      plan.addStream(parser, print);

      plan.execute();
   }

   /**
    * Tuple emitter for testing XMLParser.
    *
    * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class Emitter extends XMLTupleEmitter {

      // TODO add outputschema to XMLTupleEmitter
      // TODO change XMLParser to deal with outputschema

      /** The cache for parsed tuples. */
      private final LinkedList<DataTuple> tuples;

      /** Whether text needs to be parsed. */
      private boolean inValue;

      /** The current sum of the next tuple. */
      private double nextValue;

      /**
       * Constructs a new {@link Emitter}.
       */
      public Emitter() {
         this.tuples = new LinkedList<>();
         this.reset();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void startElement(final String uri, final String localName, final String qName,
            final org.xml.sax.Attributes attributes) throws SAXException {
         if (localName.equalsIgnoreCase("value")) {
            this.inValue = true;
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
         switch (localName) {
            case "value":
               this.inValue = false;
               break;
            case "values":
               this.tuples.add(new DataTuple(SCHEMA_OUTPUT, Arrays.asList(this.tuples.size(), this.nextValue)));
               this.nextValue = 0;
               break;
            default:
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void characters(final char[] ch, final int start, final int length) throws SAXException {
         if (this.inValue) {
            final String value = new String(ch, start, length);
            this.nextValue += Double.parseDouble(value);
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void reset() {
         this.tuples.clear();
         this.inValue = false;
         this.nextValue = 0;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean tupleAvailable() {
         return !this.tuples.isEmpty();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public DataTuple nextTuple() {
         return this.tuples.poll();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public Schema getOutputSchema() {
         return SCHEMA_OUTPUT;
      }
   }
}
