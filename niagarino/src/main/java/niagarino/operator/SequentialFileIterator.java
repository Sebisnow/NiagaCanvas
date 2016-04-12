/*
 * @(#)SequentialFileIterator.java   1.0   May 28, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.PunctuationControl;
import niagarino.stream.Schema;

/**
 * Reads files named by a certain schema.
 *
 * @author Johann Bornholdt &lt;johann.bornholdt@uni-konstanz.de&gt;
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class SequentialFileIterator implements TupleIterator {

   /** Empty {@link Control} array. */
   private static final ControlTuple[] EMPTY_CONTROL = new ControlTuple[0];

   /** The schema of tuples to emit. */
   private final Schema schema;

   /** The number of the current file. */
   private int fileNo;

   /** The file iterator of the current file. */
   private FileIterator currentFileIterator;

   /** The naming schema for all files. */
   private final NamingEnumeration<String> fileNameSchema;

   /**
    * Constructs SequentialFileIterator for the given file name schema.
    *
    * @param schema
    *           the schema of the tuples within the file
    * @param fileNameSchema
    *           naming schema for given files
    */
   public SequentialFileIterator(final Schema schema, final NamingEnumeration<String> fileNameSchema) {
      this.schema = schema;
      this.fileNameSchema = fileNameSchema;
      this.fileNo = -1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean hasNext() {
      boolean retval;
      if (this.currentFileIterator == null || !this.currentFileIterator.hasNext()) {
         try {
            if (this.fileNameSchema.hasMore()) {
               final String filename = this.fileNameSchema.next();
               this.currentFileIterator = new FileIterator(this.schema, filename, System.lineSeparator(),
                     false, false);
               this.fileNo++;
               retval = this.currentFileIterator.hasNext();
            } else {
               retval = false;
            }
         } catch (final NamingException | IOException e) {
            retval = false;
         }
      } else {
         retval = true;
      }

      return retval;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public DataTuple next() throws NoSuchElementException {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      }
      return this.currentFileIterator.next();
   }

   /**
    * {@inheritDoc}
    */
   public ControlTuple[] nextControls() {
      final ControlTuple[] retval;

      if (this.currentFileIterator == null || this.currentFileIterator.hasNext()) {
         retval = EMPTY_CONTROL;
      } else {
         retval = new ControlTuple[] { new PunctuationControl(PunctuationControl.Type.END_OF_ENTITY, 0, 0,
               this.fileNo, 0) };
      }
      return retval;
   }
}
