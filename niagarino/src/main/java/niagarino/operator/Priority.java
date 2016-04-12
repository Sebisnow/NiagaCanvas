/*
 * @(#)Priority.java   1.0   Mar 7, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import niagarino.stream.Attribute;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.ElementPriority;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;
import niagarino.util.TypeSystem;

/**
 * Reads priority values from a file and "joins" them with the tuples on the stream. The file format is
 * expected to be comma-separated values, where each line consists of the join attribute value, followed by a
 * priority in terms of an x and y value.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class Priority extends AbstractOperator {

   /** Position of the join attribute in the input schema. */
   private final int joinAttribute;
   /** File to read priority values from. */
   private final String file;
   /** Priority table linking join attribute values to their priority. */
   private final Map<Object, ElementPriority> table;

   /**
    * Constructs a new priority operator with the given input schema, which annotates tuples based on their
    * value for the given join attribute with priorities that are read from the given file.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           input schema
    * @param joinAttribute
    *           join attribute that determines priority
    * @param file
    *           file containing the priority table
    */
   public Priority(final String operatorId, final Schema inputSchema, final int joinAttribute,
         final String file) {
      super(operatorId, Arrays.asList(inputSchema));
      this.joinAttribute = joinAttribute;
      this.file = file;
      try {
         this.table = this.loadTable(file);
      } catch (final Exception e) {
         throw new IllegalArgumentException("Unable to load priority table", e);
      }
   }

   /**
    * Constructs a new priority operator with the given input schema, which annotates tuples based on their
    * value for the given join attribute with priorities that are read from the given file.
    *
    * @param inputSchema
    *           input schema
    * @param joinAttribute
    *           join attribute that determines priority
    * @param file
    *           file containing the priority table
    */
   public Priority(final Schema inputSchema, final int joinAttribute, final String file) {
      this(Priority.class.getSimpleName(), inputSchema, joinAttribute, file);
   }

   @Override
   public Schema getOutputSchema() {
      return this.getInputSchema();
   }

   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      final Object value = tuple.getAttributeValue(this.joinAttribute);
      final ElementPriority priority = this.table.get(value);
      if (priority != null) {
         tuple.getElementMetadata().setElementPriority(priority);
      }
      this.pushTuple(tuple);
   }

   @Override
   protected void processBackwardControl(final int input, final ControlTuple control) {
      switch (control.getType()) {
         case PRIORITY:
            // TODO implement priority feedback
            break;
         default:
            // do nothing
      }
      this.pushControl(Flow.BACKWARD, control);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void handleEoS(final Socket socket, final int input, final ControlTuple message) {
      if (Socket.OUTPUT.equals(socket)) {
         this.saveTable(this.table, this.file);
      }
      super.handleEoS(socket, input, message);
   }

   /**
    * Returns the input schema of this operator.
    *
    * @return input schema
    */
   private Schema getInputSchema() {
      return this.getInputSchemas().get(0);
   }

   /**
    * Loads the priority table that assigns values of the join attribute a priority values.
    *
    * @param file
    *           file containing the priority table
    * @return priority table linking join attribute values to their priorities.
    * @throws IOException
    *            if the file cannot be read
    * @throws FileNotFoundException
    *            if the file cannot be found
    */
   private Map<Object, ElementPriority> loadTable(final String file) throws IOException,
         FileNotFoundException {
      final Map<Object, ElementPriority> result = new TreeMap<Object, ElementPriority>();
      final Attribute attribute = this.getInputSchema().getAttribute(this.joinAttribute);
      final Class< ? > type = attribute.getType();
      final BufferedReader in = new BufferedReader(new FileReader(new File(file)));
      String line = null;
      while ((line = in.readLine()) != null) {
         final String[] tokens = line.split(",");
         if (tokens.length >= 3) {
            final Object value = TypeSystem.convertValue(type, tokens[0]);
            final int x = Integer.parseInt(tokens[1]);
            final int y = Integer.parseInt(tokens[2]);
            result.put(value, new ElementPriority(x, y));
         }
      }
      in.close();
      return result;
   }

   /**
    * Saves the in-memory priority table to the given file.
    *
    * @param table
    *           in-memory priority table
    * @param file
    *           file name
    */
   private void saveTable(final Map<Object, ElementPriority> table, final String file) {
      try {
         final BufferedWriter out = new BufferedWriter(new FileWriter(new File(file)));
         for (final Map.Entry<Object, ElementPriority> entry : table.entrySet()) {
            // TODO improve serialization of the key
            out.write(entry.getKey() + "," + entry.getValue().getX() + "," + entry.getValue().getY());
            out.newLine();
         }
         out.flush();
         out.close();
      } catch (final IOException e) {
         throw new OperatorException(this, e);
      }
   }
}
