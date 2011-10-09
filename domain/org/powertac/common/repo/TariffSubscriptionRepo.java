/*
 * Copyright (c) 2011 by the original author
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.powertac.common.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import org.powertac.common.AbstractCustomer;
import org.powertac.common.Tariff;
import org.powertac.common.TariffSubscription;

import org.springframework.stereotype.Repository;

/**
 * Repository for TariffSubscriptions. This cannot be in common, because
 * TariffSubscription is not in common.
 * 
 * @author John Collins
 */
@Repository
public class TariffSubscriptionRepo implements DomainRepo
{
  static private Logger log = Logger.getLogger(TariffSubscriptionRepo.class.getName());

  private HashMap<Tariff, List<TariffSubscription>> tariffMap;
  private HashMap<AbstractCustomer, List<TariffSubscription>> customerMap;

  public TariffSubscriptionRepo()
  {
    super();
    tariffMap = new HashMap<Tariff, List<TariffSubscription>>();
    customerMap = new HashMap<AbstractCustomer, List<TariffSubscription>>();
  }

  /**
   * Returns the TariffSubscription for the given Tariff/Customer pair, creating
   * it if necessary.
   */
  public TariffSubscription getSubscription(AbstractCustomer customer, Tariff tariff)
  {
    TariffSubscription result = findSubscriptionForCustomer(tariffMap.get(tariff), customer);
    if (result != null) {
      return result;
    }
    result = new TariffSubscription(customer, tariff);
    storeSubscription(result, customer, tariff);
    return result;
  }

  /** Returns the list of subscriptions for a given tariff. */
  public List<TariffSubscription> findSubscriptionsForTariff(Tariff tariff)
  {
    // new list allows caller to smash the return value
    List<TariffSubscription> result = tariffMap.get(tariff);
    if (result == null)
      return new ArrayList<TariffSubscription>();
    else
      return new ArrayList<TariffSubscription>(result);
  }

  /** Returns the list of subscriptions for a given customer. */
  public List<TariffSubscription> findSubscriptionsForCustomer(AbstractCustomer customer)
  {
    // new list allows caller to smash the return value
    List<TariffSubscription> result = customerMap.get(customer);
    if (result == null)
      return new ArrayList<TariffSubscription>();
    else
      return new ArrayList<TariffSubscription>(result);
  }

  /** Adds an existing subscription to the repo. */
  public TariffSubscription add(TariffSubscription subscription)
  {
    storeSubscription(subscription, subscription.getCustomer(), subscription.getTariff());
    return subscription;
  }

  /** Removes a subscription from the repo. */
  public void remove(TariffSubscription subscription)
  {
    tariffMap.get(subscription.getTariff()).remove(subscription);
    customerMap.get(subscription.getCustomer()).remove(subscription);
  }

  /** Clears out the repo in preparation for another simulation. */
  public void recycle()
  {
    tariffMap.clear();
    customerMap.clear();
  }

  public TariffSubscription findSubscriptionForCustomerAndTariff(AbstractCustomer customer, Tariff tariff)
  {
    List<TariffSubscription> subs = findSubscriptionsForTariff(tariff);
    return findSubscriptionForCustomer(subs, customer);
  }

  private TariffSubscription findSubscriptionForCustomer(List<TariffSubscription> subs, AbstractCustomer customer)
  {
    if (subs == null)
      return null;
    for (TariffSubscription sub : subs) {
      if (sub.getCustomer() == customer)
        return sub;
    }
    return null;
  }

  private void storeSubscription(TariffSubscription subscription, AbstractCustomer customer, Tariff tariff)
  {

    if (tariffMap.get(tariff) == null)
      tariffMap.put(tariff, new ArrayList<TariffSubscription>());
    tariffMap.get(tariff).add(subscription);
    // System.out.println("One Subscription Added to tariffMap");
    if (customerMap.get(customer) == null)
      customerMap.put(customer, new ArrayList<TariffSubscription>());
    customerMap.get(customer).add(subscription);
    // System.out.println("One Subscription Added to customerMap");
  }
}
