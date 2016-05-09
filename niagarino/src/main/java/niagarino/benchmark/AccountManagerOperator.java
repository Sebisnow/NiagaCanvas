/*
 * @(#)AccountManagerOperator.java   1.0   Jul 15, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import java.util.Arrays;
import java.util.HashMap;

import niagarino.operator.AbstractOperator;
import niagarino.stream.DataTuple;
import niagarino.stream.Schema;

/**
 * Keeps track of the accounts of all cars.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class AccountManagerOperator extends AbstractOperator {

   /** The manager itself. */
   private final AccountManager mgr;

   /**
    * Creates a new operator ingesting account balance changes.
    *
    * @param operatorId
    *           if of operator
    * @param inputSchema
    *           schema of incoming tuples
    */
   public AccountManagerOperator(final String operatorId, final Schema inputSchema) {
      super(operatorId, Arrays.asList(inputSchema));
      this.mgr = new AccountManager();
   }

   @Override
   protected void processTuple(final int input, final DataTuple tuple) {
      final int oldSegId = (Integer) tuple.getAttributeValue("old_segid");
      final int time = (Integer) tuple.getAttributeValue("time");
      final int vid = (Integer) tuple.getAttributeValue("vid");
      if (oldSegId > -1) {
         final int toll = (Integer) tuple.getAttributeValue("toll");
         this.mgr.updateAccount(vid, toll, time);
      } else {
         this.mgr.resetAccount(vid, time);
      }
      this.pushTuple(tuple);
   }

   @Override
   public Schema getOutputSchema() {
      return super.getInputSchemas().get(0);
   }

   /**
    * A single account with a current and a timestamped previous value.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static class Account {

      /** Current balance. */
      private int balance;
      /** The next toll to add to the balance. */
      private int pending;
      /** Timestamp of the current balance. */
      private int time;

      /** Previous balance. */
      private int prevBalance;
      /** Timestamp of previous balance. */
      private int prevTime;

      /**
       * Constructs a new account.
       */
      public Account() {
         this.balance = 0;
         this.pending = 0;
         this.time = 0;
         this.prevBalance = 0;
         this.prevTime = 0;
      }

      /**
       * Starts a new trip for the owner of the account.
       *
       * @param time
       *           the start of the trip
       */
      private void newTrip(final int time) {
         this.prevTime = this.time;
         this.prevBalance = this.balance;

         this.time = time;
         this.pending = 0;
      }

      /**
       * Initialises the segment change with new toll and new timestamp.
       *
       * @param newToll
       *           the next toll to note
       * @param time
       *           the new timestamp
       */
      public synchronized void makeSegmentChange(final int newToll, final int time) {
         this.prevBalance = this.balance;
         this.prevTime = this.time;

         this.balance += this.pending;
         this.pending = newToll;
         this.time = time;
      }

      /**
       * Returns the appropriate balance for the given timestamp.
       *
       * @param time
       *           the request timestamp
       * @return the balance at the requested timestamp
       */
      public synchronized int getBalance(final int time) {
         if (time < this.time) {
            return this.prevBalance;
         } else if (time < this.prevTime) {
            throw new IllegalStateException("Account reports requested a balance too old to keep.");
         } else {
            return this.balance;
         }
      }
   }

   /**
    * Account manager to keep track of all accounts and organise them.
    *
    * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
    * @version 1.0
    */
   public static final class AccountManager {

      /** Singleton instance. */
      private static AccountManager instance = null;

      /**
       * Returns the instance of {@link AccountManager}.
       *
       * @return the instance of {@link AccountManager}
       */
      public static AccountManager getInstance() {
         return instance;
      }

      /** Map with all accounts mapped to vehicle id. */
      private final HashMap<Integer, Account> accounts;

      /** Last second this manager did anything. */
      private int latestSecond;

      /**
       * Constructs a new {@link AccountManager}.
       */
      private AccountManager() {
         this.accounts = new HashMap<>();
         this.latestSecond = 0;
         instance = this;
      }

      /**
       * Updates the account of the given vehicle.
       *
       * @param vid
       *           the vehicle owning the account
       * @param toll
       *           the toll to note
       * @param time
       *           the timestamp for the change
       */
      private void updateAccount(final int vid, final int toll, final int time) {
         Account acc = this.accounts.get(vid);
         if (acc == null) {
            acc = new Account();
            this.accounts.put(vid, acc);
         }
         acc.makeSegmentChange(toll, time);
         if (this.latestSecond < time) {
            // If we made progress in time, notify waiting threads.
            synchronized (this) {
               this.latestSecond = time;
               this.notifyAll();
            }
         }
      }

      /**
       * Starts a new trip (in case a vehicle re-enters the highway).
       *
       * @param vid
       *           the vehicle owning the account
       * @param time
       *           the start of the new trip
       */
      private void resetAccount(final int vid, final int time) {
         Account acc = this.accounts.get(vid);
         if (acc == null) {
            acc = new Account();
            this.accounts.put(vid, acc);
            // Set the time.
            acc.makeSegmentChange(0, time);
         } else {
            // Prepare for next trip.
            acc.newTrip(time);
         }
         if (this.latestSecond < time) {
            synchronized (this) {
               this.latestSecond = time;
               this.notifyAll();
            }
         }
      }

      /**
       * The last time this account manager did anything.
       *
       * @return the timestamp of the latest change
       */
      public int getTime() {
         return this.latestSecond;
      }

      /**
       * Returns the balance of the given vehicle at the given time.
       *
       * @param vid
       *           the vehicle owning the account
       * @param time
       *           the timestamp for which to get the balance
       * @return the balance at the given timestamp for the given vehicle
       */
      public int getAccount(final int vid, final int time) {
         final Account acc = this.accounts.get(vid);
         if (acc == null) {
            return 0;
         } else {
            return acc.getBalance(time);
         }
      }
   }
}
