/*
 * @(#)SegmentChangeDerivation.java   1.0   Jul 15, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import java.util.HashMap;
import java.util.List;

import niagarino.operator.function.DerivationFunction;
import niagarino.stream.Attribute;
import niagarino.stream.ElementMetadata;

/**
 * Adds information to tuples on whether the vehicle has changed segments since the last position report.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class SegmentChangeDerivation implements DerivationFunction {

   /** Definition of the new attribute. */
   private final Attribute attribute = new Attribute("old_segid", Integer.class);

   /** Map of segment information mapped to vehicles. */
   private final HashMap<Integer, SegStat> segStats = new HashMap<>();

   /** Position of attribute "lane". */
   private int posLane = -1;
   /** Position of attribute "segid". */
   private int posSegId = -1;
   /** Position of attribute "vid". */
   private int posVid = -1;

   @Override
   public boolean isApplicable(final List<Attribute> schema) {
      boolean lane = false;
      boolean segid = false;
      boolean vid = false;
      for (final Attribute attr : schema) {
         switch (attr.getName()) {
            case "lane":
               lane = true;
               break;
            case "segid":
               segid = true;
               break;
            case "vid":
               vid = true;
               break;
            default:
         }
      }
      return lane && segid && vid;
   }

   @Override
   public void setSchema(final List<Attribute> schema) {
      int position = 0;
      for (final Attribute attr : schema) {
         switch (attr.getName()) {
            case "lane":
               this.posLane = position;
               break;
            case "segid":
               this.posSegId = position;
               break;
            case "vid":
               this.posVid = position;
               break;
            default:
         }
         position++;
      }
   }

   @Override
   public Attribute getDerivedAttribute() {
      return this.attribute;
   }

   @Override
   public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
      final int vid = (Integer) tuple.get(this.posVid);
      final int segId = (Integer) tuple.get(this.posSegId);
      final int lane = (Integer) tuple.get(this.posLane);
      SegStat segStat = this.segStats.get(vid);
      if (segStat == null) {
         segStat = new SegStat();
         this.segStats.put(vid, segStat);
      }
      final int retVal;
      if (lane == 0 && !segStat.lane0) {
         // We are on an entry lane, this is our first report from there.
         retVal = -1;
         segStat.oldSegId = segId;
      } else {
         // Default case.
         retVal = segStat.oldSegId;
         segStat.oldSegId = segId;
      }

      segStat.lane0 = lane == 0;

      return retVal;
   }

   /**
    * Simple container with information needed for freshly starting trips.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   private class SegStat {

      /** Previously seen segment. */
      private int oldSegId = -1;
      /** Whether the vehicle was on lane 0 (entry lane). */
      private boolean lane0 = false;
   }
}
