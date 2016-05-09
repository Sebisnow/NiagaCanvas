/*
 * @(#)CardataFileIterator.java   1.0   Sep 21, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import java.io.IOException;

import niagarino.operator.FileIterator;
import niagarino.stream.Attribute;
import niagarino.stream.Schema;

/**
 * Strategy for reading in the source data (position reports and requests, or "cardatapoints.out").
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class CardataFileIterator extends FileIterator {

   /** The schema of the input data. */
   private static final Schema SCHEMA = new Schema(1,
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
         new Attribute("day", Integer.class)
         );

   /**
    * Constructs a new FileIterator for the {@code cardatapoints.out} file.
    *
    * @throws IOException
    *            if an error occurs while accessing the file
    */
   public CardataFileIterator() throws IOException {
      super(SCHEMA, System.getProperty("CARDATA").toString(), ",", false, true);
   }
}
