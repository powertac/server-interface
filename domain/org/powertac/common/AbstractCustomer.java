/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.powertac.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.powertac.common.enumerations.PowerType;
import org.powertac.common.interfaces.Accounting;
import org.powertac.common.interfaces.TariffMarket;
import org.powertac.common.repo.AbstractCustomerRepo;
import org.powertac.common.repo.TariffRepo;
import org.powertac.common.repo.TariffSubscriptionRepo;
import org.powertac.common.spring.SpringApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract customer implementation
 * 
 * @author Antonios Chrysopoulos
 */
public class AbstractCustomer
{
  static private Logger log = Logger.getLogger(AbstractCustomer.class.getName());

  @Autowired
  private TimeService timeService;

  @Autowired
  private TariffMarket tariffMarketService;

  @Autowired
  private TariffSubscriptionRepo tariffSubscriptionRepo;

  /** The id of the Abstract Customer */
  private long custId;

  /** The Customer specifications */
  private CustomerInfo customerInfo;

  /**
   * >0: max power consumption (think consumer with fuse limit); <0: min power
   * production (think nuclear power plant with min output)
   */
  private double upperPowerCap = 100.0;

  /**
   * >0: min power consumption (think refrigerator); <0: max power production
   * (think power plant with max capacity)
   */
  private double lowerPowerCap = 0.0;

  /** >=0 - gram CO2 per kW/h */
  private double carbonEmissionRate = 0.0;

  /**
   * measures how wind changes translate into load / generation changes of the
   * customer
   */
  private double windToPowerConversion = 0.0;

  /**
   * measures how temperature changes translate into load / generation changes
   * of the customer
   */
  private double tempToPowerConversion = 0.0;

  /**
   * measures how sun intensity changes translate into load /generation changes
   * of the customer
   */
  private double sunToPowerConversion = 0.0;

  public AbstractCustomer(CustomerInfo customer)
  {
    super();

    // abstractCustomerRepo =
    // (AbstractCustomerRepo)SpringApplicationContext.getBean("abstractCustomerRepo");
    tariffSubscriptionRepo = (TariffSubscriptionRepo) SpringApplicationContext.getBean("tariffSubscriptionRepo");
    timeService = (TimeService) SpringApplicationContext.getBean("timeService");
    tariffMarketService = (TariffMarket) SpringApplicationContext.getBean("tariffMarketService");

    this.custId = customer.getId();
    this.customerInfo = customer;

  }

  public String toString()
  {
    return customerInfo.getName();
  }

  public int getPopulation()
  {
    return customerInfo.getPopulation();
  }

  public long getCustId()
  {
    return custId;
  }

  /** Synonym for getCustId() */
  public long getId()
  {
    return custId;
  }

  public CustomerInfo getCustomerInfo()
  {
    return customerInfo;
  }

  public double getUpperPowerCap()
  {
    return upperPowerCap;
  }

  public double getLowerPowerCap()
  {
    return lowerPowerCap;
  }

  public double getCarbonEmissionRate()
  {
    return carbonEmissionRate;
  }

  public double getWindToPowerConversion()
  {
    return windToPowerConversion;
  }

  public double getTempToPowerConversion()
  {
    return tempToPowerConversion;
  }

  public double getSunToPowerConversion()
  {
    return sunToPowerConversion;
  }

  // =============================SUBSCRIPTION=================================================

  /**
   * Function utilized at the beginning in order to subscribe to the default
   * tariff
   */
  public void subscribeDefault()
  {
    for (PowerType type : customerInfo.getPowerTypes()) {
      if (tariffMarketService.getDefaultTariff(type) == null) {
        log.info("No default Subscription for type " + type.toString() + " for " + this.toString() + " to subscribe to.");
      } else {
        TariffSubscription ts = tariffMarketService.subscribeToTariff(tariffMarketService.getDefaultTariff(type), this, getPopulation());
        tariffSubscriptionRepo.add(ts);
        log.info(this.toString() + " was subscribed to the default broker successfully.");
      }
    }
  }

  /** Subscribing certain subscription */
  void subscribe(Tariff tariff, int customerCount)
  {
    tariffSubscriptionRepo.add(tariffMarketService.subscribeToTariff(tariff, this, customerCount));
    log.info(this.toString() + " was subscribed to tariff " + tariff.getId() + " successfully.");
  }

  /** Unsubscribing certain subscription */
  void unsubscribe(TariffSubscription subscription, int customerCount)
  {
    // System.out.println("Population: " + getPopulation() + " Unsubscribing: "
    // + customerCount);
    subscription.unsubscribe(customerCount);
    log.info(this.toString() + " has unsubscribed " + customerCount + " subscribers from tariff " + subscription.getTariff().getId() + " successfully.");

    // System.out.println(subscription.getCustomersCommitted());
    if (subscription.getCustomersCommitted() == 0)
      removeSubscription(subscription);
  }

  /** Subscribing certain subscription */
  void addSubscription(TariffSubscription ts)
  {
    tariffSubscriptionRepo.add(ts);
    log.info(this.toString() + " was subscribed to tariff " + ts.getTariff().getId() + " successfully.");
  }

  /** Unsubscribing certain subscription */
  void removeSubscription(TariffSubscription ts)
  {
    tariffSubscriptionRepo.remove(ts);
    log.info(this.toString() + " was unsubscribed from tariff " + ts.getTariff().getId() + " successfully.");
  }

  // =============================CONSUMPTION-PRODUCTION==================================================

  /**
   * The first implementation of the power consumption function. I utilized the
   * mean consumption of a neighborhood of households with a random variable
   */
  void consumePower()
  {
  }

  /**
   * The first implementation of the power consumption function. I utilized the
   * mean consumption of a neighborhood of households with a random variable
   */
  void producePower()
  {
  }

  // =============================TARIFF_SELECTION_PROCESS=================================================

  /**
   * The first implementation of the changing subscription function. Here we
   * just put the tariff we want to change and the whole population is moved to
   * another random tariff.
   * 
   * @param tariff
   */
  void changeSubscription(Tariff tariff)
  {
    TariffSubscription ts = tariffSubscriptionRepo.getSubscription(this, tariff);
    int populationCount = ts.getCustomersCommitted();
    unsubscribe(ts, populationCount);

    Tariff newTariff = selectTariff(tariff.getTariffSpec().getPowerType());
    subscribe(newTariff, populationCount);
  }

  /**
   * In this overloaded implementation of the changing subscription function,
   * Here we just put the tariff we want to change and the whole population is
   * moved to another random tariff.
   * 
   * @param tariff
   */
  void changeSubscription(Tariff tariff, Tariff newTariff)
  {
    TariffSubscription ts = tariffSubscriptionRepo.getSubscription(this, tariff);
    int populationCount = ts.getCustomersCommitted();
    unsubscribe(ts, populationCount);
    subscribe(newTariff, populationCount);
  }

  /**
   * In this overloaded implementation of the changing subscription function,
   * Here we just put the tariff we want to change and amount of the population
   * we want to move to the new tariff.
   * 
   * @param tariff
   */
  void changeSubscription(Tariff tariff, Tariff newTariff, int populationCount)
  {
    TariffSubscription ts = tariffSubscriptionRepo.getSubscription(this, tariff);
    unsubscribe(ts, populationCount);
    subscribe(newTariff, populationCount);
  }

  /**
   * The first implementation of the tariff selection function. This is a random
   * chooser of the available tariffs, totally insensitive.
   */
  Tariff selectTariff(PowerType powerType)
  {
    Tariff result;
    List<Tariff> available = new ArrayList<Tariff>();
    int ran, index;
    available = tariffMarketService.getActiveTariffList(powerType);
    // log.info("Available Tariffs for " + powerType + ": "
    // ${available.toString()} "
    index = available.indexOf(tariffMarketService.getDefaultTariff(powerType));
    log.info("Index of Default Tariff: " + index);
    ran = index;
    while (ran == index) {
      ran = (int) (available.size() * Math.random());
    }
    result = available.get(ran);
    return result;
  }

  /**
   * The first implementation of the checking for revoked subscriptions
   * function.
   */
  void checkRevokedSubscriptions()
  {

    List<TariffSubscription> revoked = tariffMarketService.getRevokedSubscriptionList(this);
    for (TariffSubscription revokedSubscription : revoked) {
      TariffSubscription ts = revokedSubscription.handleRevokedTariff();
      removeSubscription(revokedSubscription);
      addSubscription(ts);
    }
  }

  void step()
  {
  }
}
