/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.dbdiscovery.checker;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.exception.MissingRequiredDBDiscoveryConfigurationException;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseDiscoveryRuleConfigurationCheckerTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertValidCheck() {
        DatabaseDiscoveryRuleConfiguration config = getValidConfiguration();
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        checker.check("test", config, Collections.emptyMap(), Collections.emptyList());
    }
    
    private DatabaseDiscoveryRuleConfiguration getValidConfiguration() {
        DatabaseDiscoveryRuleConfiguration result = mock(DatabaseDiscoveryRuleConfiguration.class);
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = mock(DatabaseDiscoveryDataSourceRuleConfiguration.class);
        when(dataSourceRuleConfig.getDiscoveryTypeName()).thenReturn("jdbc");
        when(result.getDataSources()).thenReturn(Collections.singleton(dataSourceRuleConfig));
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertInvalidCheck() {
        DatabaseDiscoveryRuleConfiguration config = getInvalidConfiguration();
        RuleConfigurationChecker checker = OrderedSPILoader.getServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass())).get(config.getClass());
        assertThrows(MissingRequiredDBDiscoveryConfigurationException.class, () -> checker.check("test", config, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private DatabaseDiscoveryRuleConfiguration getInvalidConfiguration() {
        DatabaseDiscoveryRuleConfiguration result = mock(DatabaseDiscoveryRuleConfiguration.class);
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = mock(DatabaseDiscoveryDataSourceRuleConfiguration.class);
        when(dataSourceRuleConfig.getDiscoveryTypeName()).thenReturn("");
        when(result.getDataSources()).thenReturn(Collections.singleton(dataSourceRuleConfig));
        return result;
    }
}
