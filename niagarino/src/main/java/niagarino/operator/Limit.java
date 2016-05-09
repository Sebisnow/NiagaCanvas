/*
 * @(#)Limit.java   1.0   Jun 25, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;
import java.util.Objects;

import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;

/**
 * <p>
 * Limits the number of tuples that can pass through the stream at the insertion point.
 * </p>
 * <p>
 * <em>Note:</em> This is mostly useful at the start of the stream, if the stream should be limited (like with
 * the unix {@code head(1)} command) to a fixed amount of tuples.
 * </p>
 *
 * @author Manuel Hotz &lt;manuel.hotz@uni-konstanz.de&gt
 */
public class Limit extends AbstractOperator {

   /**
    * Set limit.
    */
   private final long limit;
   /**
    * Current number of seen tuples.
    */
   private long curr = 0;

   /**
    * Constructor.
    * @param schema the schema of the tuples
    * @param limit the limit for the amount of tuples that can pass through
    */
   public Limit(final Schema schema, final long limit) {
      super(Limit.class.getSimpleName(), Arrays.asList(Objects.requireNonNull(schema)));
      if (limit < 0) {
         throw new IllegalArgumentException("Limit has to be non-negative; was " + limit);
      }
      this.limit = limit;
   }

   @Override
   public Schema getOutputSchema() {
      return this.getInputSchemas().get(0);
   }

   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      if (this.curr < this.limit) {
         this.pushTuple(tuple);
         this.curr++;
      } else if (this.curr == this.limit) {
         // u shall not pass!
         this.pushControl(Flow.FORWARD, new ControlTuple(ControlTuple.Type.EOS));
         this.curr++;
      }
      // *drops mic*
   }
}
