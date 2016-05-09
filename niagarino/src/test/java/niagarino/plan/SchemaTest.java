/*
 * @(#)SchemaTest.java   1.0   Jan 6, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2014-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.plan;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import niagarino.QueryException;
import niagarino.runtime.PhysicalQueryPlan;

/**
 * Tests the XML query plan description parser.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class SchemaTest {

   /** Path of the XML query plan description file. */
   private static final String XML_FILE_PATH = "src/test/resources/plan/xml-plan-generic.xml";

   /**
    * Tests the JAXB schema definition and parsing.
    *
    * @throws JAXBException
    *            if something is wrong with the JAXB schema definition or the example file
    */
   @Test
   public void testXMLRead() throws JAXBException {
      final File xml = new File(SchemaTest.XML_FILE_PATH);

      final JAXBContext jc = JAXBContext.newInstance(Plan.class);

      final Unmarshaller unmarshaller = jc.createUnmarshaller();
      @SuppressWarnings("unused")
      final Plan plan = (Plan) unmarshaller.unmarshal(xml);
   }

   /**
    * Tests the creation of a query plan from an XML description.
    *
    * @throws QueryException
    *            if executing the query plan fails
    * @throws JAXBException
    *            if something is wrong with the JAXB schema definition or the example file
    * @throws ClassNotFoundException
    *            if for an operator there is a class specified which doesn't exist
    */
   @Test
   public void testXMLReader() throws QueryException, JAXBException, ClassNotFoundException {
      final XMLReader reader = new XMLReader(SchemaTest.XML_FILE_PATH);
      reader.read();
      final PhysicalQueryPlan plan = reader.createPlan();
      plan.execute();
   }
}
