/*
 * @(#)LinearRoadUtil.java   1.0   Apr 15, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import niagarino.util.PropertiesReader;

/**
 * Constants for the Linear Road benchmark.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public final class LinearRoadUtil {

   /** Name of the cars segmentdata storage. */
   protected static final String STORAGE_SEGMENTDATA_CARS = "segmentdata-cars";

   /** Name of the pure segmentdata storage. */
   protected static final String STORAGE_SEGMENTDATA = "segmentdata";

   /** Target path as configured. */
   protected static final String TARGET_PATH = System.getProperty("OUTPUTFOLDER") == null ? PropertiesReader
         .getPropertiesReader().getProperties()
         .getProperty(PropertiesReader.BENCHMARK_OUTPUT, "") : System.getProperty("OUTPUTFOLDER");

   /**
    * Private default constructor.
    */
   private LinearRoadUtil() {
   }
}
