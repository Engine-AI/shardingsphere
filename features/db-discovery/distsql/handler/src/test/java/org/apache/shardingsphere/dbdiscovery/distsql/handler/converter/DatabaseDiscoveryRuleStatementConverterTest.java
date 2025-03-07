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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.converter;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseDiscoveryRuleStatementConverterTest {
    
    @Test
    void assertConvert() {
        DatabaseDiscoveryRuleConfiguration ruleConfig = DatabaseDiscoveryRuleStatementConverter.convert(createDatabaseDiscoveryRuleSegments());
        assertTrue(ruleConfig.getDiscoveryTypes().containsKey("definition_mysql_mgr"));
        assertTrue(ruleConfig.getDiscoveryHeartbeats().containsKey("definition_heartbeat"));
        Iterator<DatabaseDiscoveryDataSourceRuleConfiguration> iterator = ruleConfig.getDataSources().iterator();
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = iterator.next();
        assertThat(dataSourceRuleConfig.getDataSourceNames(), is(Arrays.asList("resource0", "resource1")));
        assertThat(dataSourceRuleConfig.getGroupName(), is("definition"));
        assertThat(dataSourceRuleConfig.getDiscoveryTypeName(), is("definition_mysql_mgr"));
        assertThat(dataSourceRuleConfig.getDiscoveryHeartbeatName(), is("definition_heartbeat"));
    }
    
    private Collection<DatabaseDiscoveryRuleSegment> createDatabaseDiscoveryRuleSegments() {
        Properties props = PropertiesBuilder.build(new Property("key", "value"));
        DatabaseDiscoveryRuleSegment databaseDiscoveryRuleSegment = new DatabaseDiscoveryRuleSegment(
                "definition", Arrays.asList("resource0", "resource1"), new AlgorithmSegment("MySQL.MGR", props), props);
        return Collections.singletonList(databaseDiscoveryRuleSegment);
    }
}
