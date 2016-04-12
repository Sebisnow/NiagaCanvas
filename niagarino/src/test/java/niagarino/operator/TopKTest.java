/*
 * @(#)TopKTest.java   1.0   Jul 9, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.operator;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Test;

import niagarino.runtime.PhysicalQueryPlan;
import niagarino.runtime.PhysicalQueryPlan.OperatorType;
import niagarino.stream.Attribute;
import niagarino.stream.ControlTuple;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Test cases for the TopK operator.
 *
 * @author Manuel Hotz &lt;manuel.hotz@uni-konstanz.de&gt
 */
public class TopKTest {

   /** Test schema. */
   private static final Schema SCHEMA = new Schema(0, new Attribute("prog", Integer.class), new Attribute(
         "name", String.class), new Attribute("bid", Integer.class));

   /** Constant for K. */
   private static final int K = 3;

   /** File to which to write result stream. */
   private static final String OUT_FILENAME = "outstream.csv";

   /**
    * Small "test" for the top-k operator.
    *
    * @throws Exception
    *            if the query cannot be executed
    */
   @Test
   public void testTopK() throws Exception {

      final Iterator< ? >[] iters = new Iterator[] { new ProgIterator(0, 1000, 100), new NameIterator(),
            new BidIterator() };

      final PersonEventGenerator it = new PersonEventGenerator(SCHEMA, iters);

      final PhysicalQueryPlan plan = new PhysicalQueryPlan();

      final SourceOperator src = new SourceOperator("src", SCHEMA, it);
      final Operator win = new ValueWindow(src.getOutputSchema(), 100, 100);
      final Operator topK = new TopK("top-k", win.getOutputSchema(), K, 2);
      final Operator print = new Print(topK.getOutputSchema(), true, new PrintStream(new File(OUT_FILENAME)));

      plan.addOperator(src, OperatorType.SOURCE);
      plan.addOperator(win);
      plan.addOperator(topK);
      plan.addOperator(print, OperatorType.SINK);

      plan.addStream(src, win);
      plan.addStream(win, topK);
      plan.addStream(topK, print);

      plan.execute();
   }

   /** Iterator over names. */
   private static final class NameIterator implements Iterator<String> {

      /** Array of names. */
      private static final String[] NAMES = { "Derbyshire", "Gica", "Lemm", "Haislett", "Vaudrain",
         "Schweitzer", "Kinas", "Moro", "Wojtaszek", "Mulcahey", "Rouillard", "Karlsen", "Mecum", "Bila",
         "Glenister", "Klaus", "Deigado", "Sapko", "Santti", "Josue", "Alba", "Gruen", "Colona",
         "Glickman", "Geant", "Fetzer", "Blagg", "Stampe", "Wowk", "Suozzi", "Mcgarrigle", "Hamblin",
         "Billus", "Zincke", "Mixon", "Loeza", "Meador", "Helle", "Hoogland", "Sickle", "Bakey",
         "Alemany", "Nagtalon", "Mcconico", "Zigler", "Churchfield", "Pazderski", "Wrights", "Fiscal",
         "Esterbrook", "Otto", "Newbold", "Zurmiller", "Sours", "Deyon", "Holmquist", "Neborak", "Wait",
         "Bigwood", "Musca", "Jollimore", "Ogden", "Bucaram", "Audet", "Konigsberg", "Capuchin", "Rieder",
         "Seals", "Cookerly", "Shipmen", "Bokal", "Dragoo", "Calver", "Schwendeman", "Horesco",
         "Providence", "Thayne", "Heitner", "Nunnery", "Jensen", "Bonne", "Deardon", "Natalello", "Tarkey" };

      /** Random number generator. */
      private final Random rnd;

      /**
       * Constructs a name iterator.
       */
      public NameIterator() {
         this.rnd = new Random(1337);
      }

      @Override
      public boolean hasNext() {
         return true;
      }

      @Override
      public String next() {
         return NAMES[this.rnd.nextInt(NAMES.length - 1)];
      }
   }

   /**
    * Integer iterator for the progressing attribute.
    *
    * @author Manuel Hotz &lt;manuel.hotz@uni-konstanz.de&gt
    */
   private static final class ProgIterator implements Iterator<Integer> {

      /** Start value. */
      private final int start;
      /** Count value. */
      private final int count;
      /** Rep value. */
      private final int rep;

      /** Current value. */
      private int curr = 0;
      /** Current rep value. */
      private int currRep = 0;

      /**
       * Constructs a new iterator for the progressing attribute.
       *
       * @param start
       *           start value
       * @param count
       *           count value
       * @param rep
       *           rep value
       */
      public ProgIterator(final int start, final int count, final int rep) {
         this.start = start < 0 ? 0 : start;
         this.count = count;
         this.rep = rep;
      }

      @Override
      public boolean hasNext() {
         return this.curr * this.currRep < (this.count - 1) * this.rep;
      }

      @Override
      public Integer next() {
         if (this.currRep < this.rep) {
            this.currRep++;
            return this.curr;
         }
         this.currRep = 1;
         return ++this.curr;
      }
   }

   /**
    * Integer iterator for the {@code bid} attribute.
    *
    * @author Manuel Hotz &lt;manuel.hotz@uni-konstanz.de&gt
    */
   private static final class BidIterator implements Iterator<Integer> {

      /** Random number generator. */
      private final Random rnd;

      /**
       * Constructs a new iterator for the {@code bid} attribute.
       */
      public BidIterator() {
         this.rnd = new Random();
      }

      @Override
      public boolean hasNext() {
         return true;
      }

      @Override
      public Integer next() {
         return this.rnd.nextInt(10000000);
      }
   }

   /**
    * Tuple element generator.
    *
    * @author Manuel Hotz &lt;manuel.hotz@uni-konstanz.de&gt
    */
   private static final class PersonEventGenerator implements TupleIterator {

      /** Array of control tuples. */
      private static final ControlTuple[] EMPTY_CONTROL = new ControlTuple[0];
      /** Schema of the generated tuples. */
      private final Schema schema;
      /** Iterators for all tuple values. */
      private final Iterator< ? >[] iterators;

      /**
       * Constructs a new tuple element generator.
       *
       * @param schema
       *           tuple schema
       * @param iterators
       *           tuple value iterators
       */
      public PersonEventGenerator(final Schema schema, final Iterator< ? >... iterators) {
         this.schema = schema;
         this.iterators = iterators;
      }

      @Override
      public boolean hasNext() {
         // we stop if one iterator has done enough
         final boolean hasNext = Arrays.stream(this.iterators).allMatch(it -> it.hasNext());
         return hasNext;
      }

      @Override
      public DataTuple next() throws NoSuchElementException {
         if (!this.hasNext()) {
            return null;
         }
         final List<Object> values = new ArrayList<>();
         for (final Iterator< ? > it : this.iterators) {
            values.add(it.next());
         }
         return new DataTuple(this.schema, values);
      }

      @Override
      public ControlTuple[] nextControls() {
         return EMPTY_CONTROL;
      }
   }
}
