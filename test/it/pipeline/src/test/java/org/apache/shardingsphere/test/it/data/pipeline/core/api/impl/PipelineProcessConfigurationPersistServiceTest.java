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

package org.apache.shardingsphere.test.it.data.pipeline.core.api.impl;

import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.swapper.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.api.impl.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.spi.job.JobType;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineProcessConfigurationPersistServiceTest {
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @Test
    void assertLoadAndPersist() {
        YamlPipelineProcessConfiguration yamlProcessConfig = new YamlPipelineProcessConfiguration();
        YamlPipelineReadConfiguration yamlReadConfig = YamlPipelineReadConfiguration.buildWithDefaultValue();
        yamlReadConfig.fillInNullFieldsWithDefaultValue();
        yamlReadConfig.setShardingSize(10);
        yamlProcessConfig.setRead(yamlReadConfig);
        YamlPipelineWriteConfiguration yamlWriteConfig = YamlPipelineWriteConfiguration.buildWithDefaultValue();
        yamlProcessConfig.setWrite(yamlWriteConfig);
        YamlAlgorithmConfiguration yamlStreamChannel = new YamlAlgorithmConfiguration("MEMORY", new Properties());
        yamlProcessConfig.setStreamChannel(yamlStreamChannel);
        String expectedYamlText = YamlEngine.marshal(yamlProcessConfig);
        PipelineProcessConfiguration processConfig = new YamlPipelineProcessConfigurationSwapper().swapToObject(yamlProcessConfig);
        PipelineProcessConfigurationPersistService persistService = new PipelineProcessConfigurationPersistService();
        JobType jobType = new MigrationJobType();
        persistService.persist(jobType, processConfig);
        String actualYamlText = YamlEngine.marshal(new YamlPipelineProcessConfigurationSwapper().swapToYamlConfiguration(persistService.load(jobType)));
        assertThat(actualYamlText, is(expectedYamlText));
    }
}
