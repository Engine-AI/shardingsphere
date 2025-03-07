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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.update;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryHeartbeatStatement;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DropDatabaseDiscoveryHeartbeatStatementUpdaterTest {
    
    private final DropDatabaseDiscoveryHeartbeatStatementUpdater updater = new DropDatabaseDiscoveryHeartbeatStatementUpdater();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Test
    void assertCheckSQLStatementWithoutCurrentHeartbeat() {
        assertThrows(MissingRequiredRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement(), null));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeDroppedHeartbeat() {
        assertThrows(MissingRequiredRuleException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap())));
    }
    
    @Test
    void assertCheckSQLStatementWithInUsed() {
        DatabaseDiscoveryDataSourceRuleConfiguration ruleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("name", Collections.emptyList(), "heartbeat_name", "");
        assertThrows(RuleInUsedException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.singletonList(ruleConfig),
                        Collections.singletonMap("heartbeat_name", null), Collections.emptyMap())));
    }
    
    @Test
    void assertUpdateCurrentRuleConfiguration() {
        DatabaseDiscoveryRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement(), ruleConfig);
        assertFalse(ruleConfig.getDiscoveryHeartbeats().containsKey("heartbeat_name"));
    }
    
    @Test
    void assertUpdateCurrentRuleConfigurationWithIfExists() {
        DatabaseDiscoveryRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        DropDatabaseDiscoveryHeartbeatStatement dropDatabaseDiscoveryHeartbeatStatement = createSQLStatementWithIfExists();
        updater.checkSQLStatement(database, dropDatabaseDiscoveryHeartbeatStatement, ruleConfig);
        assertFalse(updater.updateCurrentRuleConfiguration(dropDatabaseDiscoveryHeartbeatStatement, ruleConfig));
        assertTrue(ruleConfig.getDiscoveryHeartbeats().containsKey("heartbeat_name"));
        assertThat(ruleConfig.getDiscoveryHeartbeats().size(), is(2));
    }
    
    private DropDatabaseDiscoveryHeartbeatStatement createSQLStatement() {
        return new DropDatabaseDiscoveryHeartbeatStatement(false, Collections.singleton("heartbeat_name"));
    }
    
    private DropDatabaseDiscoveryHeartbeatStatement createSQLStatementWithIfExists() {
        return new DropDatabaseDiscoveryHeartbeatStatement(true, Collections.singleton("heartbeat_name_0"));
    }
    
    private DatabaseDiscoveryRuleConfiguration createCurrentRuleConfiguration() {
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartbeat = new HashMap<>(2, 1);
        discoveryHeartbeat.put("heartbeat_name", new DatabaseDiscoveryHeartBeatConfiguration(new Properties()));
        discoveryHeartbeat.put("other", new DatabaseDiscoveryHeartBeatConfiguration(new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), discoveryHeartbeat, Collections.emptyMap());
    }
}
