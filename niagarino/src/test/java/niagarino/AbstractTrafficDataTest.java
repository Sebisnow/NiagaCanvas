/*
 * @(#)AbstractTrafficDataTest.java   1.0   Jul 31, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino;

import java.util.Date;

import niagarino.stream.Attribute;
import niagarino.stream.Schema;

/**
 * Abstract superclass for JUnit test cases. This class provides an input file with a corresponding schema and
 * an output stream.
 *
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni-konstanz.de&gt;
 * @version 1.0
 */
public abstract class AbstractTrafficDataTest {

   /** Input file name. */
   protected static final String IN_FILENAME = "portaldata_2011_01_25_fixed.zip";

   /** Output file name. */
   protected static final String OUT_FILENAME = "outstream.csv";

   /** Test data schema: < detectorid, starttime, volume, speed, occupancy, status >. */
   protected static final Schema SCHEMA = new Schema(1, new Attribute("detectorid", Long.class),
         new Attribute("starttime", Date.class), new Attribute("volume", Integer.class),
         new Attribute("speed", Integer.class), new Attribute("occupancy", Integer.class),
         new Attribute("status", Integer.class));
}
