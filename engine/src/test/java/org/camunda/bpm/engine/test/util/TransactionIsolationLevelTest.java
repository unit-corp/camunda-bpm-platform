/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.util;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;

public class TransactionIsolationLevelTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule();

  Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  @Test
  public void getAndSetTransactionIsolationLevel() {
    ProcessEngineConfigurationImpl processEngineConfiguration = processEngineRule.getProcessEngineConfiguration();
    LOG.warn("Database type: {}", DatabaseHelper.getDatabaseType(processEngineConfiguration));
    LOG.warn("Transaction isolation level before: {}", DatabaseHelper.getTransactionIsolationLevel(processEngineConfiguration));
    DatabaseHelper.setTransactionIsolationLevel(processEngineConfiguration);
    LOG.warn("Transaction isolation level after: {}", DatabaseHelper.getTransactionIsolationLevel(processEngineConfiguration));
  }
}
