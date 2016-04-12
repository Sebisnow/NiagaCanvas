/*
 * @(#)FileIteratorTest.java   1.0   May 21, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Test;

import niagarino.QueryException;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.stream.Attribute;
import niagarino.stream.Schema;

/**
 * Test cases for {@link FileIterator}.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class FileIteratorTest {

   /** Schema for test input file. */
   private static final Schema SCHEMA = new Schema(1, new Attribute("type", Integer.class), new Attribute(
         "time", Integer.class), new Attribute("vid", Integer.class), new Attribute("spd", Double.class),
         new Attribute("xway", Integer.class), new Attribute("lane", Integer.class), new Attribute("dir",
               Integer.class), new Attribute("seg", Integer.class), new Attribute("pos", Integer.class),
               new Attribute("qid", Integer.class), new Attribute("s_init", Integer.class), new Attribute("s_end",
                     Integer.class), new Attribute("dow", Integer.class), new Attribute("tod", Integer.class),
                     new Attribute("day", Integer.class));

   /** File name for test input file. */
   private static final String FILENAME = "plan/cardatapoints.out.sorted";

   /** File to which to write result stream. */
   private static final String OUT_FILENAME = "outstream.csv";

   /**
    * Test case for {@link FileIterator}.
    *
    * @throws QueryException
    *            if executing the query plan fails
    * @throws IOException
    *            if an error occurs while reading from the file
    */
   @Test
   public void testFileIterator() throws QueryException, IOException {
      final FileIterator fileIterator = new FileIterator(SCHEMA, FILENAME, ",", false, false);

      final SourceOperator source = new SourceOperator("source", SCHEMA, fileIterator);
      final Print sink = new Print("sink", SCHEMA, true, new PrintStream(new File(OUT_FILENAME)));

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      plan.addOperator(source, OperatorType.SOURCE);
      plan.addOperator(sink, OperatorType.SINK);

      plan.addStream(source, sink);

      plan.execute();
   }
}
