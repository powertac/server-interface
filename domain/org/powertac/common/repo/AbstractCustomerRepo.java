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

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import org.powertac.common.AbstractCustomer;

import org.springframework.stereotype.Repository;

/**
 * Repository for AbstractCustomer. This cannot be in common, because
 * AbstractCustomer is not in common.
 * 
 * @author Antonios Chrysopoulos
 */
@Repository
public class AbstractCustomerRepo implements DomainRepo
{
  static private Logger log = Logger.getLogger(TariffSubscriptionRepo.class.getName());

  private HashMap<Long, AbstractCustomer> idTable;

  public AbstractCustomerRepo()
  {
    super();
    idTable = new HashMap<Long, AbstractCustomer>();
  }

  public void add(AbstractCustomer customer)
  {
    idTable.put(customer.getId(), customer);
  }

  public Collection<AbstractCustomer> list()
  {
    return idTable.values();
  }

  public AbstractCustomer findById(long id)
  {
    return idTable.get(id);
  }

  public void recycle()
  {
    idTable.clear();
  }

}
