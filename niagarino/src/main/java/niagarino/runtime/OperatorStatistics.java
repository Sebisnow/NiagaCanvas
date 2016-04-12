/*
 * @(#)OperatorStatistics.java   1.0   May 15, 2014
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.runtime;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import niagarino.operator.Operator;
import niagarino.operator.OperatorEventListener;
import niagarino.stream.DataTuple;

/**
 * Operator statistics collect information about the lifecycle of an operator.
 *
 * @author Bernhard Fischer &lt;bernhard.fischer@uni.kn&gt;
 * @author Michael Grossniklaus &lt;michael.grossniklaus@uni.kn&gt;
 * @version 1.0
 */
public class OperatorStatistics implements OperatorEventListener {

   /** Name of the operator. */
   private final String operatorName;

   /** ID of the operator thread. */
   private final long threadId;

   /** Thread management bean to access thread information. */
   private final ThreadMXBean threadMXBean;

   /** Counts the number of all input tuples consumed by this operator. */
   private long inputTuples;

   /** Counts the number of all output tuples produced by this operator. */
   private long outputTuples;

   /** Counts the number of times that this operator blocked. */
   private long blockedCount;

   /** Accumulates the total amount of time in milliseconds that this operator was blocked. */
   private long blockedTime;

   /** Counts the number of times that this operator waited. */
   private long waitedCount;

   /** Accumulates the total amount of time in milliseconds that his operator was waiting. */
   private long waitedTime;

   /** Records the total CPU time of the monitored operator thread. */
   private long threadCpuTime;

   /** Records the CPU time in user mode of the monitored operator thread. */
   private long threadUserTime;

   /**
    * Constructs an instance and throws an exception if the used JVM does not support thread wise CPU timing.
    *
    * @param operatorName
    *           name of the operator
    * @param threadId
    *           ID of the operator thread
    * @param threadMXBean
    *           thread management bean
    */
   public OperatorStatistics(final String operatorName, final long threadId,
         final ThreadMXBean threadMXBean) {
      this.operatorName = operatorName;
      this.threadId = threadId;
      this.threadMXBean = threadMXBean;
      this.outputTuples = 0;
      this.blockedCount = 0;
      this.blockedTime = 0;
      this.waitedCount = 0;
      this.waitedTime = 0;
      this.threadCpuTime = 0;
      this.threadUserTime = 0;
   }

   @Override
   public void onInputTuple(final Operator source, final DataTuple tuple) {
      this.inputTuples++;
   }

   @Override
   public void onOutputTuple(final Operator source, final DataTuple tuple) {
      this.outputTuples++;
   }

   @Override
   public void onShutdown(final Operator source) {
      if (this.threadMXBean.isThreadCpuTimeSupported()) {
         this.threadCpuTime = this.threadMXBean.getThreadCpuTime(this.threadId);
         this.threadUserTime = this.threadMXBean.getThreadUserTime(this.threadId);
      }
      final ThreadInfo threadInfo = this.threadMXBean.getThreadInfo(this.threadId);
      this.blockedCount = threadInfo.getBlockedCount();
      this.waitedCount = threadInfo.getWaitedCount();
      if (this.threadMXBean.isThreadContentionMonitoringEnabled()) {
         this.blockedTime = threadInfo.getBlockedTime();
         this.waitedTime = threadInfo.getWaitedTime();
      }
   }

   /**
    * Returns the name of the monitored operator.
    *
    * @return operator name
    */
   public String getOperatorName() {
      return this.operatorName;
   }

   /**
    * Returns the number of input tuples processed by the monitored operator.
    *
    * @return number of input tuples
    */
   public long getInputTuples() {
      return this.inputTuples;
   }

   /**
    * Returns the number of output tuples processed by the monitored operator.
    *
    * @return number of output tuples
    */
   public long getOutputTuples() {
      return this.outputTuples;
   }

   /**
    * Returns the number of times the monitored operator was blocked. This information is recorded upon
    * operator shutdown and therefore not available prior.
    *
    * @return number of blocks
    */
   public long getBlockedCount() {
      return this.blockedCount;
   }

   /**
    * Returns the amount of time in milliseconds the monitored operator was blocked. This information is
    * recorded upon operator shutdown and therefore not available prior.
    *
    * @return blocking time
    */
   public long getBlockedTime() {
      return this.blockedTime;
   }

   /**
    * Returns the number of time the monitored operator was waiting. This information is recorded upon
    * operator shutdown and therefore not available prior.
    *
    * @return number of waits
    */
   public long getWaitedCount() {
      return this.waitedCount;
   }

   /**
    * Returns the amount of time in milliseconds the monitored operator was waiting. This information is
    * recorded upon operator shutdown and therefore not available prior.
    *
    * @return waiting time
    */
   public long getWaitedTime() {
      return this.waitedTime;
   }

   /**
    * Returns the total CPU time in nanoseconds that the monitored operator thread was running. This
    * information is recorded upon operator shutdown and therefore not available prior.
    *
    * @return total CPU time
    */
   public long getThreadCpuTime() {
      return this.threadCpuTime;
   }

   /**
    * Returns the CPU time in nanoseconds that the monitored operator thread was running in user mode. This
    * information is recorded upon operator shutdown and therefore not available prior.
    *
    * @return user mode CPU time
    */
   public long getThreadUserTime() {
      return this.threadUserTime;
   }
}
