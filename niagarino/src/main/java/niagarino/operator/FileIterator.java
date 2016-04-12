/*
 * @(#)FileIterator.java   1.0   May 21, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import niagarino.stream.Attribute;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;
import niagarino.util.TypeSystem;

/**
 * Iterates over the content of a given file line by line.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class FileIterator implements TupleIterator {

   /** Empty {@link ControlTuple} array. */
   private static final ControlTuple[] EMPTY_CONTROL = new ControlTuple[0];

   /** The logger for this class. */
   private static final Logger LOG = LogManager.getLogger(FileIterator.class);

   /** The schema of tuples to emit. */
   private final Schema schema;

   /** The file name of the input file. */
   private final String filename;

   /** The reader for the file to read from. */
   private final BufferedReader fileStream;

   /** Field separator. */
   private final String separator;

   /** Next line to emit. */
   private String nextLine;

   /**
    * Constructs a new {@link FileIterator} for the given file. It is assumed to use "," as separator and to
    * have a header.
    *
    * @param schema
    *           the schema of the tuples within the file
    * @param file
    *           the path to the file
    * @throws IOException
    *            if an I/O error occurs while reading the file
    */
   public FileIterator(final Schema schema, final String file)
         throws IOException {
      this(schema, file, ",", true, false);
   }

   /**
    * Constructs a new {@link FileIterator} for the given file. It is assumed to have a header.
    *
    * @param schema
    *           the schema of the tuples within the file
    * @param file
    *           the path to the file
    * @param separator
    *           the field separator used within the file
    * @throws IOException
    *            if an I/O error occurs while reading the file
    */
   public FileIterator(final Schema schema, final String file, final String separator)
         throws IOException {
      this(schema, file, separator, true, false);
   }

   /**
    * Constructs a new {@link FileIterator} for the given file.
    *
    * @param schema
    *           the schema of the tuples within the file
    * @param file
    *           the path to the file
    * @param separator
    *           the field separator used within the file
    * @param hasHeader
    *           whether the file has a header line
    * @param compressed
    *           whether the file is gz-compressed
    * @throws IOException
    *            if an I/O error occurs while reading the file
    */
   public FileIterator(final Schema schema, final String file, final String separator,
         final boolean hasHeader, final boolean compressed)
         throws IOException {
      this.schema = schema;
      this.filename = file;
      // Try both, system resource and normal file path
      InputStream in = ClassLoader.getSystemResourceAsStream(file);
      if (in == null) {
         in = new FileInputStream(file);
      }
      if (compressed) {
         // TODO: Mask exceptions, set reasonable buffer size if necessary, externalize to abstract layer
         in = new GZIPInputStream(in);
      }
      this.fileStream = new BufferedReader(new InputStreamReader(in));
      this.separator = separator;
      this.nextLine = null;
      if (hasHeader) {
         this.retrieveNextLine();
      }
      this.retrieveNextLine();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean hasNext() {
      return this.nextLine != null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DataTuple next() throws NoSuchElementException {
      // Extract values from line
      final String[] strings = this.nextLine.split(this.separator);
      final List<Object> values = new ArrayList<Object>();
      for (int i = 0; i < this.schema.getSize(); i++) {
         if (i < strings.length) {
            final Attribute attribute = this.schema.getAttribute(i);
            values.add(TypeSystem.convertValue(attribute.getType(), strings[i]));
         } else {
            values.add(null);
         }
      }
      final DataTuple tuple = new DataTuple(this.schema, values);
      // Prepare the next line
      this.retrieveNextLine();
      return tuple;
   }

   /**
    * Retrieves the next line from the file and stores it for the next call of {@link FileIterator#next()}.
    */
   private void retrieveNextLine() {
      try {
         this.nextLine = this.fileStream.readLine();
      } catch (final IOException e) {
         LOG.error("Could not read the next line from file '" + this.filename + "', closing file.", e);
         this.nextLine = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ControlTuple[] nextControls() {
      return EMPTY_CONTROL;
   }
}
