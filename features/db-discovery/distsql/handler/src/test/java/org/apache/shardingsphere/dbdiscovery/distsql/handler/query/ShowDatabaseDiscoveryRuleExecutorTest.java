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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.query;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryDataSourceRule;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDatabaseDiscoveryRuleExecutorTest {
    
    @Test
    void assertGetRows() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        DatabaseDiscoveryRule rule = mock(DatabaseDiscoveryRule.class, RETURNS_DEEP_STUBS);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        DatabaseDiscoveryDataSourceRule dataSourceRule = mock(DatabaseDiscoveryDataSourceRule.class);
        when(dataSourceRule.getPrimaryDataSourceName()).thenReturn("ds_0");
        when(rule.getDataSourceRules()).thenReturn(Collections.singletonMap("ms_group", dataSourceRule));
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(rule)));
        RQLExecutor<ShowDatabaseDiscoveryRulesStatement> executor = new ShowDatabaseDiscoveryRuleExecutor();
        assertColumns(executor.getColumnNames());
        assertRowData(new ArrayList<>(executor.getRows(database, mock(ShowDatabaseDiscoveryRulesStatement.class))));
    }
    
    @Test
    void assertGetRowsWithSpecifiedRuleName() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        DatabaseDiscoveryRule rule = mock(DatabaseDiscoveryRule.class, RETURNS_DEEP_STUBS);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        DatabaseDiscoveryDataSourceRule dataSourceRule = mock(DatabaseDiscoveryDataSourceRule.class);
        when(dataSourceRule.getPrimaryDataSourceName()).thenReturn("ds_0");
        when(rule.getDataSourceRules()).thenReturn(Collections.singletonMap("ms_group", dataSourceRule));
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(rule)));
        RQLExecutor<ShowDatabaseDiscoveryRulesStatement> executor = new ShowDatabaseDiscoveryRuleExecutor();
        assertColumns(executor.getColumnNames());
        assertRowData(new ArrayList<>(executor.getRows(database, new ShowDatabaseDiscoveryRulesStatement("ms_group", null))));
    }
    
    @Test
    void assertGetColumnNames() {
        RQLExecutor<ShowDatabaseDiscoveryRulesStatement> executor = new ShowDatabaseDiscoveryRuleExecutor();
        assertColumns(executor.getColumnNames());
    }
    
    private DatabaseDiscoveryRuleConfiguration createRuleConfiguration() {
        DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration(
                "ms_group", Arrays.asList("ds_0", "ds_1"), "heartbeat_test", "type_test");
        AlgorithmConfiguration shardingSphereAlgorithmConfig = new AlgorithmConfiguration("MySQL.MGR", new Properties());
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartbeat = Collections.singletonMap("heartbeat_test", new DatabaseDiscoveryHeartBeatConfiguration(new Properties()));
        Map<String, AlgorithmConfiguration> discoverTypes = Collections.singletonMap("type_test", shardingSphereAlgorithmConfig);
        return new DatabaseDiscoveryRuleConfiguration(Collections.singleton(databaseDiscoveryDataSourceRuleConfig), discoveryHeartbeat, discoverTypes);
    }
    
    private void assertColumns(final Collection<String> actual) {
        assertThat(actual.size(), is(5));
        assertTrue(actual.containsAll(Arrays.asList("group_name", "data_source_names", "primary_data_source_name", "discovery_type", "discovery_heartbeat")));
    }
    
    private void assertRowData(final Collection<LocalDataQueryResultRow> actual) {
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("ms_group"));
        assertThat(row.getCell(2), is("ds_0,ds_1"));
        assertThat(row.getCell(3), is("ds_0"));
        assertThat(row.getCell(4).toString(), is("{name=type_test, type=MySQL.MGR, props={}}"));
        assertThat(row.getCell(5).toString(), is("{name=heartbeat_test, props={}}"));
    }
}
