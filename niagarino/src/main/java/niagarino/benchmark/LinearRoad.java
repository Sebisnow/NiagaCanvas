/*
 * @(#)LinearRoad.java   1.0   Sep 8, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import javax.xml.bind.JAXBException;

import niagarino.QueryException;
import niagarino.operator.function.AbstractDerivationFunction;
import niagarino.operator.function.DerivationFunction;
import niagarino.operator.predicate.Predicate;
import niagarino.plan.XMLReader;
import niagarino.runtime.PhysicalQueryPlan;
import niagarino.storage.MapStorage.MapStorageFactory;
import niagarino.storage.SegmentedStorage;
import niagarino.stream.Attribute;
import niagarino.stream.DataTuple;
import niagarino.stream.ElementMetadata;
import niagarino.stream.Schema;

/**
 * The Linear Road benchmark was developed in 2004. It simulates cars entering and exiting linear highways.
 * The evaluated program has to process position updates for accident detecten as well as requests for
 * information issued by cars. The rating is based on the number of highways that can be processed in
 * real-time. More information can be found on the official homepage. In order to run this benchmark, the data
 * generated by the data generator one can get from the official homepage is to be used as input, and the
 * output files have to be validated by the data validator from the homepage.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 * @see <a href="http://www.cs.brandeis.edu/~linearroad/"> http://www.cs.brandeis.edu/~linearroad/</a>
 */
public class LinearRoad {

   /**
    * Constructs a new Linear Road benchmark.
    *
    * @param planPath
    *           path to the XML query plan to use
    * @throws IOException
    *            If an IOException occurs.
    * @throws InterruptedException
    *            If an interruption occurs.
    * @throws QueryException
    *            If an exception occurs with the query.
    */
   public LinearRoad(final String planPath) throws IOException,
         InterruptedException, QueryException {
      new SegmentedStorage<SegmentData>(LinearRoadUtil.STORAGE_SEGMENTDATA_CARS,
            new MapStorageFactory<SegmentData>(30000), 20);
      new SegmentedStorage<SegmentData>(LinearRoadUtil.STORAGE_SEGMENTDATA,
            new MapStorageFactory<SegmentData>(30000), 20);

      final XMLReader reader = new XMLReader(planPath);
      PhysicalQueryPlan plan = null;
      try {
         reader.read();
         plan = reader.createPlan();
      } catch (final ClassNotFoundException | JAXBException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         System.exit(1);
      }
      plan.execute();
   }

   /**
    * Container for information regarding highway segments.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class SegmentData {

      /** Whether there is an accident. */
      private boolean accident = false;
      /** Count of cars in the segment. */
      private int cars = -1;
      /** Latest average velocity in the segment. */
      private int lav = -1;
      /** Current toll for the segment. */
      private int toll = -1;

      /**
       * Whether there is an accident in this segment.
       *
       * @return whether there is an accident
       */
      public boolean hasAccident() {
         return this.accident;
      }

      /**
       * Sets an accident in this segment.
       */
      public void setAccident() {
         this.accident = true;
      }

      /**
       * Get amount of cars in this segment.
       *
       * @return amount of cars
       */
      public int getCars() {
         return this.cars;
      }

      /**
       * Sets amount of cars in this segment.
       *
       * @param cars
       *           amount of cars
       */
      public void setCars(final int cars) {
         this.cars = cars;
      }

      /**
       * Get latest average velocity in this segment.
       *
       * @return LAV
       */
      public int getLAV() {
         return this.lav;
      }

      /**
       * Sets latest average velocity in this segment.
       *
       * @param lav
       *           the LAV
       */
      public void setLAV(final int lav) {
         this.lav = lav;
      }

      /**
       * Gets the current toll for this segment.
       *
       * @return current toll
       */
      public int getToll() {
         return this.toll;
      }

      /**
       * Sets the current toll for this segment.
       *
       * @param toll
       *           current toll
       */
      public void setToll(final int toll) {
         this.toll = toll;
      }

      @Override
      public String toString() {
         return "Accident: " + this.accident + "; Cars: " + this.cars;
      }
   }

   /**
    * Detects segment changes between two position reports.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class SegmentChangePredicate implements Predicate {

      @Override
      public boolean isApplicable(final Schema schema) {
         return schema.getAttributeIndex("MIN(segid)") != -1
               && schema.getAttributeIndex("MAX(segid)") != -1
               && schema.getAttributeIndex("COUNT(segid)") != -1;
      }

      @Override
      public boolean evaluate(final DataTuple tuple) {
         return !tuple.getAttributeValue("MIN(segid)").equals(tuple.getAttributeValue("MAX(segid)"))
               && ((Long) tuple.getAttributeValue("COUNT(segid)")) == 2;
      }
   }

   /**
    * Prevents reports emitting from a window that are too late from continuing and screwing up the results.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class LengthLimit implements Predicate {

      @Override
      public boolean isApplicable(final Schema schema) {
         return true;
      }

      @Override
      public boolean evaluate(final DataTuple tuple) {
         return ((Integer) tuple.getAttributeValue("time")) < 10800;
      }
   }

   /**
    * Derivation function to calculate the segment ID.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class SegmentIdDerivationFunction extends AbstractDerivationFunction {

      /** Position of attribute "xway". */
      private int xway = -1;
      /** Position of attribute "dir". */
      private int dir = -1;
      /** Position of attribute "seg". */
      private int seg = -1;

      @Override
      public boolean isApplicable(final List<Attribute> schema) {
         for (int i = 0; i < schema.size(); i++) {
            switch (schema.get(i).getName()) {
               case "xway":
                  this.xway = i;
                  break;
               case "dir":
                  this.dir = i;
                  break;
               case "seg":
                  this.seg = i;
                  break;
               default:
            }
         }
         if (Math.min(this.xway, Math.min(this.dir, this.seg)) == -1) {
            return false;
         }
         return schema.get(this.xway).getType() == Integer.class
               && schema.get(this.dir).getType() == Integer.class
               && schema.get(this.seg).getType() == Integer.class;
      }

      @Override
      public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
         final int xway = (int) tuple.get(this.xway);
         final int dir = (int) tuple.get(this.dir);
         final int seg = (int) tuple.get(this.seg);
         return xway << 8 | dir << 7 | seg;
      }

      @Override
      protected Attribute initDerivedAttribute(final List<Attribute> schema) {
         return new Attribute("segid", Integer.class);
      }
   }

   /**
    * Predicate to select all tuples of type 0.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    */
   public static class Type0Predicate implements Predicate {

      @Override
      public boolean isApplicable(final Schema schema) {
         // I don't care for this method.
         return true;
      }

      @Override
      public boolean evaluate(final DataTuple tuple) {
         return ((Integer) tuple.getAttributeValue(0)).equals(0);
      }
   }

   /**
    * Predicate to select all tuples with stopped cars.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    */
   public static class StoppedCarsPredicate implements Predicate {

      /** Position of attribute "MIN(pos)". */
      private int minPos = -1;
      /** Position of attribute "MAX(pos)". */
      private int maxPos = -1;
      /** Position of attribute "COUNT(pos)". */
      private int countPos = -1;

      @Override
      public boolean isApplicable(final Schema schema) {
         if (this.minPos < 0 || this.maxPos < 0 || this.countPos < 0) {
            this.minPos = schema.getAttributeIndex("MIN(pos)");
            this.maxPos = schema.getAttributeIndex("MAX(pos)");
            this.countPos = schema.getAttributeIndex("COUNT(pos)");
         }
         return this.minPos > -1 && this.maxPos > -1 && this.countPos > -1;
      }

      @Override
      public boolean evaluate(final DataTuple tuple) {
         return Double.compare((Double) tuple.getAttributeValue(this.minPos), (Double) tuple
               .getAttributeValue(this.maxPos)) == 0 && ((Long) tuple.getAttributeValue(this.countPos)) == 4;
      }
   }

   /**
    * Predicate to select all tuples with stopped cars.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    */
   public static class AccidentPredicate implements Predicate {

      /** Position of attribute "COUNT(vid)". */
      private int countVid = -1;

      @Override
      public boolean isApplicable(final Schema schema) {
         if (this.countVid < 0) {
            this.countVid = schema.getAttributeIndex("COUNT(vid)");
         }
         return this.countVid > -1;
      }

      @Override
      public boolean evaluate(final DataTuple tuple) {
         final long countVid = (Long) tuple.getAttributeValue("COUNT(vid)");
         final int car1 = ((Double) tuple.getAttributeValue("MIN(vid)")).intValue();
         final int car2 = ((Double) tuple.getAttributeValue("MAX(vid)")).intValue();
         final int time = (Integer) tuple.getAttributeValue("time");
         final int earliestTimestamp = ((Double) tuple.getAttributeValue("MIN(MAX(time2))")).intValue();
         final int lowerBoundary = time - 60 + ((time + 1) % 60);
         final boolean youngEnough = (countVid < 3 && lowerBoundary < earliestTimestamp) || countVid > 2;
         return car1 != car2 && (countVid > 2 || countVid == 2 && youngEnough);
      }
   }

   /**
    * Determines whether there is an accident ahead and the vehicle is not on the exit lane (4).
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class AccidentReportSelectionPredicate implements Predicate {

      @Override
      public boolean isApplicable(final Schema schema) {
         return true;
      }

      @Override
      public boolean evaluate(final DataTuple tuple) {
         // There is an accident ahead AND we're not on the exit lane.
         return (Integer) tuple.getAttributeValue("accident_segment") > -1
               && (Integer) tuple.getAttributeValue("lane") != 4;
      }
   }

   /**
    * Derives the minute from the seconds (starts with 1).
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class MinuteDerivation implements DerivationFunction {

      /** Position of attribute "time". */
      private int posTime = -1;

      @Override
      public boolean isApplicable(final List<Attribute> schema) {
         for (final Attribute attr : schema) {
            if (attr.getName().equalsIgnoreCase("time")) {
               return true;
            }
         }
         return false;
      }

      @Override
      public void setSchema(final List<Attribute> schema) {
         for (int i = 0; i < schema.size(); i++) {
            if (schema.get(i).getName().equalsIgnoreCase("time")) {
               this.posTime = i;
               return;
            }
         }
      }

      @Override
      public Attribute getDerivedAttribute() {
         return new Attribute("minute", Integer.class);
      }

      @Override
      public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
         return (Integer) (((Integer) tuple.get(this.posTime)) / 60);
      }
   }

   /**
    * Derives the current EPOCH timestamp.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class TimestampDerivation implements DerivationFunction {

      @Override
      public boolean isApplicable(final List<Attribute> schema) {
         return true;
      }

      @Override
      public void setSchema(final List<Attribute> schema) {
      }

      @Override
      public Attribute getDerivedAttribute() {
         return new Attribute("epoch_millis", Long.class);
      }

      @Override
      public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
         return Instant.now().toEpochMilli();
      }
   }

   /**
    * Copies the "time" attribute to "time2".
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class Time2Derivation implements DerivationFunction {

      @Override
      public boolean isApplicable(final List<Attribute> schema) {
         return true;
      }

      @Override
      public void setSchema(final List<Attribute> schema) {
      }

      @Override
      public Attribute getDerivedAttribute() {
         return new Attribute("time2", Integer.class);
      }

      @Override
      public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
         return tuple.get(1);
      }
   }

   /**
    * Determines whether this is a type 2 request.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class Type2Predicate implements Predicate {

      @Override
      public boolean isApplicable(final Schema schema) {
         return schema.getAttributeIndex("type") > -1;
      }

      @Override
      public boolean evaluate(final DataTuple tuple) {
         return (Integer) tuple.getAttributeValue("type") == 2;
      }
   }
}
