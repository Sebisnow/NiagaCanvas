/*
 * @(#)PropertiesReader.java   1.0   Apr 26, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to read the Properties file and get the needed properties.
 *
 * @author Maximilian Ortwein &lt;maximilian.ortwein@uni.kn&gt;
 * @version 1.0
 */
public final class PropertiesReader {

   /** Key to enable paging. */
   public static final String PAGING_ENABLED = "niagarino.paging.enabled";
   /** Key to set the number of tuples in a page. */
   public static final String PAGING_PAGESIZE = "niagarino.paging.pagesize";
   /** Key to set the number of elements in a stream. */
   public static final String STREAM_SIZE = "niagarino.stream.size";
   /** Key to set the output directory of the benchmark. */
   public static final String BENCHMARK_OUTPUT = "niagarino.benchmark.output";

   /** Store the instance of the PropertiesReader. */
   private static PropertiesReader instance;
   /** The properties. */
   private final Properties properties;

   /**
    * Constructs a new Properties Reader.
    */
   private PropertiesReader() {
      this.properties = new Properties();
      try {
         final BufferedInputStream stream = new BufferedInputStream(this.getClass().getResourceAsStream(
               "/niagarino.properties"));
         try {
            this.properties.load(stream);
         } catch (final IOException ex) {
            Logger.getLogger(PropertiesReader.class.getName()).log(Level.SEVERE, null, ex);
         }
         stream.close();
      } catch (final IOException ex) {
         Logger.getLogger(PropertiesReader.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   /**
    * Get the instance of the PropertiesReader.
    *
    * @return PropertiesReader instance of PropertiesReader
    */
   public static PropertiesReader getPropertiesReader() {
      if (PropertiesReader.instance == null) {
         PropertiesReader.instance = new PropertiesReader();
      }
      return PropertiesReader.instance;
   }

   /**
    * Get the Properties.
    *
    * @return Properties properties
    */
   public Properties getProperties() {
      return this.properties;
   }
}
