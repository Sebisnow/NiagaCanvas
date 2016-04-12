/*
 * @(#)PhysicalQueryPlan.java   1.0   Mar 8, 2011
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.runtime;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import niagarino.QueryException;
import niagarino.operator.Operator;
import niagarino.stream.Stream;

/**
 * A physical query plan is a directed acyclic graph with operators as nodes and streams as edges.
 * Additionally, a physical query plan has two sets of specially designated nodes. First, there is a set of
 * source nodes, typically scan operators, that load the streaming data. Second, there is a set of sink nodes,
 * typically print operators, that output the results of the query. The execution of a physical query plan
 * terminates once all source nodes have terminated, i.e. once a backward {@code EOS} message has been
 * received as feedback by all source nodes. Upon receiving a {@code EOS} forward message, sink node reply
 * with a backward {@code EOS} message.
 *
 * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
 */
public class PhysicalQueryPlan implements Thread.UncaughtExceptionHandler {

   /**
    * Enumeration to mark certain kinds of operators.
    *
    * @author Michael Grossniklaus &lt;michagro@cecs.pdx.edu&gt;
    * @version 1.0
    */
   public enum OperatorType {
      /** Operator type to mark a source operator. */
      SOURCE,

      /** Operator type to mark a sink operator. */
      SINK
   };

   /** All operators in this physical query plan. */
   private final Map<String, Operator> operators;
   /** All streams in this physical query plan. */
   private final Set<Stream> streams;
   /** Source operators in this physical query plan. */
   private final Set<Operator> sources;
   /** Sink operators in this physical query plan. */
   private final Set<Operator> sinks;
   /** Indicates whether statistics are collected. */
   private boolean statisticsEnabled;
   /** List of all operator statistics. */
   private final List<OperatorStatistics> statistics;
   /** Records the execution time in nanoseconds of this physical query plan. */
   private long executionTime;
   /** Records the memory in bytes used by this physical query plan. */
   private long memoryUsage;
   /** List that collects exceptions that occur in operator threads during execution. */
   private final List<Throwable> throwables;

   /**
    * Creates a new physical query plan, which can optionally use paging.
    */
   public PhysicalQueryPlan() {
      this.operators = new LinkedHashMap<>();
      this.streams = new HashSet<>();
      this.sources = new HashSet<>();
      this.sinks = new HashSet<>();
      this.statisticsEnabled = false;
      this.statistics = new ArrayList<>();
      this.executionTime = 0;
      this.memoryUsage = 0;
      this.throwables = new ArrayList<>();
   }

   /**
    * Adds the given operator to this physical query plan.
    *
    * @param operator
    *           stream operator
    */
   public void addOperator(final Operator operator) {
      if (this.operators.containsKey(operator.getName())) {
         throw new IllegalStateException(
               "Physical query plan already contains an operator with id '" + operator.getName() + "'.");
      }
      this.operators.put(operator.getName(), operator);
   }

   /**
    * Adds the given operator to this physical query plan and marks it with the given type.
    *
    * @param operator
    *           stream operator
    * @param type
    *           operator type
    */
   public void addOperator(final Operator operator, final OperatorType type) {
      this.addOperator(operator);
      switch (type) {
         case SOURCE:
            this.sources.add(operator);
            break;
         case SINK:
            this.sinks.add(operator);
            break;
         default:
            // do nothing
      }
   }

   /**
    * Returns the operator with the given id.
    *
    * @param id
    *           id of the operator
    * @return operator
    */
   public Operator getOperator(final String id) {
      return this.operators.get(id);
   }

   /**
    * Adds a stream between the two given operators to this physical query plan.
    *
    * @param from
    *           source operator
    * @param to
    *           target operator
    */
   public void addStream(final Operator from, final Operator to) {
      if (this.operators.containsValue(from) && this.operators.containsValue(to)) {
         final Stream stream = new Stream();
         this.streams.add(stream);
         from.addOutputStream(stream);
         to.addInputStream(stream);
      } else {
         throw new IllegalStateException("Operator nodes not found in physical query plan.");
      }
   }

   /**
    * Returns all streams in this physical query plan as an unmodifiable set.
    *
    * @return stream of this physical query plan
    */
   public Set<Stream> getStreams() {
      return Collections.unmodifiableSet(this.streams);
   }

   /**
    * Set whether this physical query plan collects statistics.
    *
    * @param statisticsEnabled
    *           {@code true} if statistics are collected, {@code false} otherwise
    */
   public void setStatisticsEnabled(final boolean statisticsEnabled) {
      this.statisticsEnabled = statisticsEnabled;
   }

   /**
    * Returns whether this physical query plan collects statistics.
    *
    * @return {@code true} if statistics are collected, {@code false} otherwise
    */
   public boolean isStatisticsEnabled() {
      return this.statisticsEnabled;
   }

   /**
    * Executes this physical query plan.
    *
    * @throws QueryException
    *            if executing the query plan fails
    */
   public void execute() throws QueryException {
      if (this.sources.size() < 1 || this.sinks.size() < 1) {
         throw new IllegalStateException(
               "A physical query plan must have at least one source and one sink node.");
      }
      ThreadMXBean threadMXBean = null;
      Runtime runtime = null;
      long startTime = 0;
      long endTime = 0;
      long startMemory = 0;
      long endMemory = 0;
      if (this.statisticsEnabled) {
         // Get the thread management bean to collect information about operator threads
         threadMXBean = ManagementFactory.getThreadMXBean();
         // In order to get wait and block times, contention monitoring needs to be enabled
         threadMXBean.setThreadContentionMonitoringEnabled(true);
         // Record the start time of the physical query plan
         startTime = System.nanoTime();
         // Get the Java runtime to access memory usage information
         runtime = Runtime.getRuntime();
         // Run the garbage collector
         runtime.gc();
         // Record the memory used at the start of the physical query plan
         startMemory = runtime.totalMemory() - runtime.freeMemory();
      }
      try {
         final List<Thread> sourceThreads = new ArrayList<>();
         for (final Operator operator : this.operators.values()) {
            operator.setSink(this.sinks.contains(operator));
            final Thread thread = new Thread(operator);
            // Register handler for exception that are not caught by the operator thread in order to propagate
            // them to the main thread
            thread.setUncaughtExceptionHandler(this);
            // Set the thread name to the operator name in order to get better debugging information
            thread.setName(operator.getName());
            if (this.sources.contains(operator)) {
               sourceThreads.add(thread);
            }
            if (this.statisticsEnabled) {
               // Collect operator statistics using an operator event listener
               final OperatorStatistics stats = new OperatorStatistics(operator.getName(), thread.getId(),
                     threadMXBean);
               operator.addOperatorEventListener(stats);
               this.statistics.add(stats);
            }
            // Operator is fully configured, start the corresponding thread
            thread.start();
         }
         // Wait for all operators to finish
         for (final Thread thread : sourceThreads) {
            thread.join();
         }
      } catch (final InterruptedException e) {
         throw new QueryException(e);
      }
      if (this.statisticsEnabled) {
         // Record the end time of the physical query plan
         endTime = System.nanoTime();
         // Record the memory used at the end of the physical query plan
         endMemory = runtime.totalMemory() - runtime.freeMemory();
      }
      // If an operator finished due to an exception, re-throw this exception in the main thread
      for (final Throwable throwable : this.throwables) {
         throw new QueryException(throwable);
      }
      this.executionTime = endTime - startTime;
      this.memoryUsage = endMemory - startMemory;
   }

   /**
    * Terminates this physical query plan by stopping all operators.
    */
   public void terminate() {
      for (final Operator operator : this.operators.values()) {
         if (operator.isRunning()) {
            operator.stop();
         }
      }
   }

   /**
    * Prints statistics about all operators of this plan to the given print writer.
    *
    * @param out
    *           print writer
    */
   public void printStatistics(final PrintStream out) {
      if (this.statisticsEnabled) {
         for (final OperatorStatistics stats : this.statistics) {
            final String s = "---" + padRight(stats.getOperatorName(), 42, '-') + "\nProcessed tuples\t"
                  + stats.getInputTuples() + " -> " + stats.getOutputTuples() + "\nBlocked count\t\t"
                  + stats.getBlockedCount() + "\nBlocked time\t\t" + stats.getBlockedTime()
                  + " ms\nWaited count\t\t" + stats.getWaitedCount() + "\nWaited time\t\t"
                  + stats.getWaitedTime() + " ms\nTotal CPU time\t\t"
                  + (long) (stats.getThreadCpuTime() / 1E06) + " ms" + "\nUser-mode CPU time\t"
                  + (long) (stats.getThreadUserTime() / 1E06) + " ms";
            out.println(s);
         }
         final String s = "===Summary===================================" + "\nTotal execution time\t"
               + (long) (this.executionTime / 1E06) + " ms\nTotal memory usage\t"
               + this.memoryUsage / 1024 / 1024 + " MB";
         out.println(s);
      }
   }

   /**
    * Adds padding with the given padding char to the right of the given string.
    *
    * @param s
    *           string to pad
    * @param size
    *           padding size
    * @param pad
    *           padding char
    * @return padded string
    */
   private static String padRight(final String s, final int size, final char pad) {
      final StringBuffer padded = new StringBuffer(s);
      while (padded.length() < size) {
         padded.append(pad);
      }
      return padded.toString();
   }

   @Override
   public void uncaughtException(final Thread thread, final Throwable throwable) {
      this.throwables.add(throwable);
      this.terminate();
   }
}
