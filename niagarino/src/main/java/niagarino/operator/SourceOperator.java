/*
 * @(#)SourceOperator.java   1.0   May 20, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.util.Arrays;

import niagarino.plan.Parameter;
import niagarino.plan.PlanOperatorByParametersFactory;
import niagarino.stream.ControlTuple;
import niagarino.stream.ControlTuple.Type;
import niagarino.stream.Schema;
import niagarino.stream.Stream.Flow;

/**
 * A {@link SourceOperator} emits tuples retrieved from a {@link TupleIterator}.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class SourceOperator extends AbstractOperator {

   /** An iterator over the tuples to emit. */
   private final TupleIterator iterator;

   /** Whether EOF has been sent already. */
   private boolean eofSent;

   /**
    * Constructs a new source operator which emits the tuples of the given {@link TupleIterator}.
    *
    * @param operatorId
    *           id of operator
    * @param schema
    *           schema of emitted tuples
    * @param iterator
    *           a {@link TupleIterator} over the tuples to emit
    */
   public SourceOperator(final String operatorId, final Schema schema, final TupleIterator iterator) {
      super(operatorId, Arrays.asList(schema));
      this.iterator = iterator;
      this.eofSent = false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean processStreams() {
      final boolean hasNext = this.iterator.hasNext();
      if (hasNext) {
         this.pushTuple(this.iterator.next());
         for (final ControlTuple ctrl : this.iterator.nextControls()) {
            this.pushControl(Flow.FORWARD, ctrl);
         }
      } else {
         if (!this.eofSent) {
            this.pushControl(Flow.FORWARD, new ControlTuple(Type.EOS));
            this.eofSent = true;
         }
      }
      return super.processStreams() || hasNext;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Schema getOutputSchema() {
      return this.getInputSchemas().get(0);
   }

   /**
    * Factory for new instances of the SourceOperator.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class Factory implements PlanOperatorByParametersFactory {

      @Override
      public Operator getOperatorByParameters(final String operatorId, final Schema inputSchema,
            final Parameter parameters) throws Exception {
         final String iteratorName = parameters.get("object").get("class").getString();
         final TupleIterator iterator = (TupleIterator) Class.forName(iteratorName).newInstance();
         return new SourceOperator(operatorId, inputSchema, iterator);
      }
   }
}
