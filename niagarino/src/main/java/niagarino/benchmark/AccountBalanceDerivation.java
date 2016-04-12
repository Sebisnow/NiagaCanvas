/*
 * @(#)AccountBalanceDerivation.java   1.0   Jul 16, 2015
 *
 * Copyright (c) 2011-2012 Portland State University.
 * Copyright (c) 2013-2015 University of Konstanz.
 *
 * This software is the proprietary information of the above-mentioned institutions.
 * Use is subject to license terms. Please refer to the included copyright notice.
 */
package niagarino.benchmark;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import niagarino.benchmark.AccountManagerOperator.AccountManager;
import niagarino.operator.function.DerivationFunction;
import niagarino.stream.Attribute;
import niagarino.stream.ElementMetadata;

/**
 * Reads from the account storage and returns the current balance for the requested account.
 *
 * @author Florian 'Sammy' Junghanns &lt;florian.junghanns@uni-konstanz.de&gt;
 * @version 1.0
 */
public class AccountBalanceDerivation implements DerivationFunction {

   /** The logger for this class. */
   private static final Logger LOG = LogManager.getLogger(AccountBalanceDerivation.class);

   /** The account manager to get data from. */
   private AccountManager mgr = null;

   /** Position of the "time" attribute. */
   private int posTime = -1;

   /** Position of the "vid" attribute. */
   private int posVid = -1;

   @Override
   public boolean isApplicable(final List<Attribute> schema) {
      boolean time = false;
      boolean vid = false;
      for (final Attribute attr : schema) {
         switch (attr.getName()) {
            case "time":
               time = true;
               break;
            case "vid":
               vid = true;
               break;
            default:
         }
      }
      return time && vid;
   }

   @Override
   public void setSchema(final List<Attribute> schema) {
      for (int i = 0; i < schema.size(); i++) {
         switch (schema.get(i).getName()) {
            case "time":
               this.posTime = i;
               break;
            case "vid":
               this.posVid = i;
               break;
            default:
         }
      }
      this.mgr = AccountManagerOperator.AccountManager.getInstance();
   }

   @Override
   public Attribute getDerivedAttribute() {
      return new Attribute("balance", String.class);
   }

   @Override
   public Object derive(final List<Object> tuple, final ElementMetadata metadata) {
      while (this.mgr == null) {
         // TODO dirty hack, remove ASAP
         try {
            Thread.sleep(100);
         } catch (final InterruptedException e) {
            // Nothing to do here.
         }
         this.mgr = AccountManagerOperator.AccountManager.getInstance();
      }
      final int vid = (Integer) tuple.get(this.posVid);
      final int time = (Integer) tuple.get(this.posTime);
      int balanceTime;
      final int balance;
      synchronized (this.mgr) {
         while (this.mgr.getTime() < time - 60) {
            try {
               LOG.info("Waiting for balance (Last update: " + this.mgr.getTime() + ", current time: " + time
                     + ") ...");
               this.mgr.wait();
            } catch (final InterruptedException e) {
               // Nothing to do here.
            }
            LOG.info("Woken for balance, checking again.");
         }
         balanceTime = this.mgr.getTime();
         balance = this.mgr.getAccount(vid, time);
         if (balanceTime > time) {
            // Accounting is ahead. We cannot use the manager's time.
            // The state was accurate at the time we checked, else we wouldn't have gotten it.
            balanceTime = time;
         }
      }
      return balanceTime + ":" + balance;
   }
}
