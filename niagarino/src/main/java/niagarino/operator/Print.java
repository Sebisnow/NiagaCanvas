/*
 * @(#)Print.java   1.0   Feb 15, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import niagarino.plan.Parameter;
import niagarino.plan.PlanOperatorByParametersFactory;
import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * An operator to print tuples on a stream.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 * @version 1.0
 */
public class Print extends AbstractOperator {

   /** Format definition to render date values according to ISO 8601. */
   private static final String ISO_DATE_FORMAT = "%#$tY-%#$tm-%#$tdT%#$tH:%#$tM:%#$tSZ";
   /** Format definition to render date values as represented in Microsoft Excel. */
   private static final String XLS_DATE_FORMAT = "%#$tm/%#$td/%#$tY %#$tH:%#$tM:%#$tS";
   /** Format definition to render date values in milliseconds. */
   private static final String MSEC_DATE_FORMAT = "%#$TQ";
   /** Format definition to render long values. */
   private static final String LONG_FORMAT = "%#$d";
   /** Format definition to render double values. */
   private static final String DOUBLE_FORMAT = "%#$f";
   /** Format definition to render string values. */
   private static final String STRING_FORMAT = "%#$s";

   /** Format definition used by this print operator. */
   private final String format;
   /** Output stream used by this print operator. */
   private final PrintStream out;

   /**
    * Constructs a new print operator for the given schema and format definition that prints to the given
    * output stream. Optionally, the operator prints a schema header before outputting the data tuples.
    *
    * @param operatorId
    *           name of operator
    * @param inputSchema
    *           schema of input tuples
    * @param format
    *           format definition to render tuples
    * @param printSchema
    *           <code>true</code> if a schema header should be printed, <code>false</code> otherwise
    * @param out
    *           output print stream
    */
   public Print(final String operatorId, final Schema inputSchema, final String format,
         final boolean printSchema, final PrintStream out) {
      super(operatorId, Arrays.asList(inputSchema));
      this.format = format;
      this.out = out;
      if (printSchema) {
         this.printSchema(inputSchema);
      }
   }

   /**
    * Constructs a new print operator for the given schema and format definition that prints to the given
    * output stream. Optionally, the operator prints a schema header before outputting the data tuples.
    *
    * @param inputSchema
    *           schema of input tuples
    * @param format
    *           format definition to render tuples
    * @param printSchema
    *           <code>true</code> if a schema header should be printed, <code>false</code> otherwise
    * @param out
    *           output print stream
    */
   public Print(final Schema inputSchema, final String format, final boolean printSchema,
         final PrintStream out) {
      this(Print.class.getSimpleName(), inputSchema, format, printSchema, out);
   }

   /**
    * Constructs a new print operator for the given schema that prints to the given output stream. Optionally,
    * the operator print a schema header before outputting the data tuples. The format definition used by this
    * operator to render tuples on the output stream is automatically inferred based on the schema of the
    * tuples.
    *
    * @param inputSchema
    *           schema of input tuples
    * @param printSchema
    *           <code>true</code> if a schema header should be printed, <code>false</code> otherwise.
    * @param out
    *           output print stream
    */
   public Print(final Schema inputSchema, final boolean printSchema, final PrintStream out) {
      this(inputSchema, Print.initFormat(inputSchema), printSchema, out);
   }

   /**
    * Constructs a new print operator for the given schema that prints to the given output stream. Optionally,
    * the operator print a schema header before outputting the data tuples. The format definition used by this
    * operator to render tuples on the output stream is automatically inferred based on the schema of the
    * tuples.
    *
    * @param operatorId
    *           id of operator
    * @param inputSchema
    *           schema of input tuples
    * @param printSchema
    *           <code>true</code> if a schema header should be printed, <code>false</code> otherwise.
    * @param out
    *           output print stream
    */
   public Print(final String operatorId, final Schema inputSchema, final boolean printSchema,
         final PrintStream out) {
      this(operatorId, inputSchema, Print.initFormat(inputSchema), printSchema, out);
   }

   @Override
   public Schema getOutputSchema() {
      return this.getInputSchemas().get(0);
   }

   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      final List<Long> segmentIds = tuple.getElementMetadata().getSegmentIds();
      if (segmentIds.size() > 0) {
         final long startId = segmentIds.get(0).longValue();
         final long endId = segmentIds.get(segmentIds.size() - 1).longValue();
         this.out.printf("[%d:%d],", startId, endId);
      } else {
         this.out.print("null,");
      }
      this.out.printf(this.format, tuple.getValues().toArray());
      this.pushTuple(tuple);
   }

   /**
    * Initializes the default format definition based on the given schema.
    *
    * @param schema
    *           schema of data tuples
    * @return format definition
    */
   private static String initFormat(final Schema schema) {
      final StringBuffer result = new StringBuffer();
      for (int i = 0; i < schema.getSize(); i++) {
         final Class< ? > type = schema.getAttribute(i).getType();
         if (Date.class.equals(type)) {
            result.append(Print.XLS_DATE_FORMAT.replaceAll("#", String.valueOf(i + 1)));
         } else if (Float.class.equals(type) || Double.class.equals(type)) {
            result.append(Print.DOUBLE_FORMAT.replace("#", String.valueOf(i + 1)));
         } else if (Integer.class.equals(type) || Long.class.equals(type)) {
            result.append(Print.LONG_FORMAT.replace("#", String.valueOf(i + 1)));
         } else if (String.class.equals(type) || Boolean.class.equals(type)) {
            result.append(Print.STRING_FORMAT.replace("#", String.valueOf(i + 1)));
         } else {
            result.append("%" + String.valueOf(i + 1) + "$s");
         }
         if (i + 1 < schema.getSize()) {
            result.append(",");
         }
      }
      result.append("%n");
      return result.toString();
   }

   /**
    * Prints the header for the given schema to the given output stream.
    *
    * @param inputSchema
    *           schema of data tuples
    */
   private void printSchema(final Schema inputSchema) {
      this.out.print("segment_id,");
      for (int i = 0; i < inputSchema.getSize(); i++) {
         final Attribute attribute = inputSchema.getAttribute(i);
         this.out.print(attribute.getName());
         if (i + 1 < inputSchema.getSize()) {
            this.out.print(",");
         }
      }
      this.out.println();
   }

   /**
    * Factory for new instances of the Print operator.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class Factory implements PlanOperatorByParametersFactory {

      @Override
      public Print getOperatorByParameters(final String operatorId, final Schema inputSchema,
            final Parameter parameters) throws Exception {
         final String type = parameters.get("type").getString();
         if (type != null) {
            switch (type) {
               case "devnull":
                  return new Print(operatorId, inputSchema, false, new PrintStream(new OutputStream() {

                     @Override
                     public void write(final int b) throws IOException {
                        // NOOP
                     }
                  }));
               case "print":
                  return new Print(operatorId, inputSchema, true, System.out);
               case "file":
                  final String filename = parameters.get("file").getString();
                  return new Print(operatorId, inputSchema, false, new PrintStream(filename));
               default:
                  throw new UnsupportedOperationException("Type '" + type
                        + "' is not known for operator Print.");
            }
         }
         if (parameters.getMap().containsKey("file")) {
            final String filename = parameters.get("file").getString();
            return new Print(operatorId, inputSchema, false, new PrintStream(filename));
         }
         throw new IllegalArgumentException("Operator Print has an invalid description.");
      }
   }
}
