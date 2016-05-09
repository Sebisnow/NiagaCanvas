/*
 * @(#)Scan.java   1.0   Feb 14, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipInputStream;

import niagarino.stream.Attribute;
import niagarino.stream.ControlTuple;
import niagarino.stream.ControlTuple.Type;
import niagarino.stream.DataTuple;
import niagarino.stream.Page;
import niagarino.stream.Schema;
import niagarino.stream.Stream;
import niagarino.stream.Stream.Flow;
import niagarino.stream.StreamElement;
import niagarino.util.PropertiesReader;
import niagarino.util.TypeSystem;

/**
 * Reads a stream of tuples from a file.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class Scan implements Operator, Runnable {

   /** Name of the operator. */
   private final String operatorId;
   /** Schema of this scan operator. */
   private final Schema schema;
   /** Output stream of this scan operator. */
   private Stream stream;
   /** Indicates whether this scan operator is running. */
   private boolean running;
   /** Indicates whether this scan operator is a sink. */
   private boolean sink;
   /** File to read from. */
   private final String fileName;
   /** Statistics collected by this operator. */
   private final OperatorEventListenerList listeners;
   /** Page. **/
   private Page page;
   /** Separator string in input file. */
   private final String separator;
   /** Flag if input file has a header. */
   private final boolean hasHeader;
   /** Is this operator paging? **/
   private final boolean isPaging = Boolean.parseBoolean(
         PropertiesReader.getPropertiesReader().getProperties().getProperty(PropertiesReader.PAGING_ENABLED));
   /** Number of tuples in a page. */
   private final int pageSize = Integer.parseInt(PropertiesReader.getPropertiesReader().getProperties()
         .getProperty(PropertiesReader.PAGING_PAGESIZE));

   /**
    * Constructs a new scan operator with the given output schema that reads from the given file.
    *
    * @param operatorId
    *           name of operator
    * @param schema
    *           output schema
    * @param file
    *           input file
    */
   public Scan(final String operatorId, final Schema schema, final String file) {
      this(operatorId, schema, file, true, ",");
   }

   /**
    * Constructs a new scan operator with the given output schema that reads from the given file.
    *
    * @param schema
    *           output schema
    * @param file
    *           input file
    */
   public Scan(final Schema schema, final String file) {
      this(Scan.class.getSimpleName(), schema, file, true, ",");
   }

   /**
    * Constructs a new scan operator with the given output schema that reads from the given file.
    *
    * @param schema
    *           output schema
    * @param file
    *           input file
    * @param separator
    *           string which is used as separator
    */
   public Scan(final Schema schema, final String file, final String separator) {
      this(Scan.class.getSimpleName(), schema, file, true, separator);
   }

   /**
    * Constructs a new scan operator with the given output schema that reads from the given file.
    *
    * @param operatorId
    *           name of operator
    * @param schema
    *           output schema
    * @param file
    *           input file
    * @param separator
    *           string which is used as separator
    */
   public Scan(final String operatorId, final Schema schema, final String file, final String separator) {
      this(operatorId, schema, file, true, separator);
   }

   /**
    * Constructs a new scan operator with the given output schema that reads from the given file.
    *
    * @param operatorId
    *           name of operator
    * @param schema
    *           output schema
    * @param file
    *           input file
    * @param hasHeader
    *           flag if input file has a header
    * @param separator
    *           string which is used as separator
    */
   public Scan(final String operatorId, final Schema schema, final String file, final boolean hasHeader,
         final String separator) {
      this.operatorId = operatorId;
      this.stream = null;
      this.schema = schema;
      this.running = false;
      this.sink = false;
      this.fileName = file;
      this.listeners = new OperatorEventListenerList();
      this.page = null;
      this.hasHeader = hasHeader;
      this.separator = separator;
   }

   @Override
   public String getName() {
      return this.operatorId;
   }

   @Override
   public Schema getOutputSchema() {
      return this.schema;
   }

   @Override
   public void addOutputStream(final Stream stream) {
      if (this.stream == null) {
         this.stream = stream;
      } else {
         throw new IllegalStateException("Maximum number of output streams exceeded.");
      }
   }

   @Override
   public void addInputStream(final Stream in) {
      throw new UnsupportedOperationException("A scan operator cannot have an input stream.");
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getOutputArity() {
      return 1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getInputArity() {
      return 0;
   }

   @Override
   public void setSink(final boolean sink) {
      this.sink = sink;
   }

   @Override
   public boolean isSink() {
      return this.sink;
   }

   @Override
   public void stop() {
      this.running = false;
   }

   @Override
   public boolean isRunning() {
      return this.running;
   }

   @Override
   public void run() {
      this.running = true;
      final File file = new File(this.fileName);
      if (file.isDirectory()) {
         final ArrayList<String> tmpfa = new ArrayList<>();
         final String[] tmpf = file.list();
         for (final String tmpff : tmpf) {
            tmpfa.add(tmpff);
         }
         Collections.sort(tmpfa);
         for (final String s : tmpfa) {
            try {
               this.processFile(this.fileName + "/" + s);
               if (!this.running) {
                  break;
               }
            } catch (final Exception e) {
               throw new OperatorException(this, e);
            }
         }
      } else {
         try {
            this.processFile(this.fileName);
         } catch (final Exception e) {
            throw new OperatorException(this, e);
         }
      }
      if (this.isPaging) {
         this.stream.pushElement(Flow.FORWARD, this.page);
         this.page = new Page(this.pageSize);
      }
      this.stream.pushElement(Flow.FORWARD, new ControlTuple(Type.EOS));
      // if the operator is still running, wait for the EOS signal from downstream
      while (this.running) {
         final StreamElement element = this.stream.pullElement(Flow.BACKWARD);
         if (element != null && element instanceof ControlTuple) {
            final ControlTuple control = (ControlTuple) element;
            if (Type.EOS.equals(control.getType())) {
               this.running = false;
            }
         }
      }
      this.running = false;
      this.listeners.fireOnShutdown(this);
      try {
         Thread.sleep(100);
      } catch (final InterruptedException e) {
         throw new OperatorException(this, e);
      }
   }

   /**
    * Gets a reader for the given file name.
    *
    * @param name
    *           file name
    * @return buffered reader
    * @throws Exception
    *            if the file cannot be found, opened, or uncompressed
    */
   private BufferedReader getReader(final String name) throws Exception {
      // try to find file in class path
      InputStream in = ClassLoader.getSystemResourceAsStream(name);
      if (in == null) {
         // file is not on class path
         in = new FileInputStream(new File(name));
      }
      if (name.endsWith("zip")) {
         // wrap input stream to unzip it
         in = new ZipInputStream(in);
         ((ZipInputStream) in).getNextEntry();
      }
      return new BufferedReader(new InputStreamReader(in));
   }

   /**
    * Processes a single file.
    *
    * @param fileName
    *           name of file
    * @throws Exception
    *            if file cannot be found, read or decoded
    */
   private void processFile(final String fileName) throws Exception {
      final BufferedReader in = this.getReader(fileName);
      // skip first line with column names
      if (this.hasHeader) {
         in.readLine();
      }
      String line;
      while ((line = in.readLine()) != null) {
         final String[] strings = line.split(this.separator);
         final List<Object> values = new ArrayList<Object>();
         for (int i = 0; i < this.getOutputSchema().getSize(); i++) {
            if (i < strings.length) {
               final Attribute attribute = this.schema.getAttribute(i);
               values.add(TypeSystem.convertValue(attribute.getType(), strings[i]));
            } else {
               values.add(null);
            }
         }
         final DataTuple tuple = new DataTuple(this.schema, values);
         if (this.isPaging) {
            if (this.page == null) {
               this.page = new Page(this.pageSize);
            }
            this.page.put(tuple);
            if (this.page.isFull()) {
               this.stream.pushElement(Flow.FORWARD, this.page);
               this.page = new Page(this.pageSize);
            }
         } else {
            this.stream.pushElement(Flow.FORWARD, tuple);
         }
         this.listeners.fireOnOutputTuple(this, tuple);
         // check downstream and kill operator if EOS has been received.
         final StreamElement element = this.stream.pullElement(Flow.BACKWARD);
         if (element != null && element instanceof ControlTuple) {
            final ControlTuple control = (ControlTuple) element;
            if (Type.EOS.equals(control.getType())) {
               this.running = false;
               break;
            }
         }
      }
      in.close();
   }

   @Override
   public void addOperatorEventListener(final OperatorEventListener listener) {
      this.listeners.addOperatorEventListener(listener);
   }

   @Override
   public boolean removeOperatorEventListener(final OperatorEventListener listener) {
      return this.listeners.removeOperatorEventListener(listener);
   }
}
