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

package org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.advice;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.apache.shardingsphere.agent.plugin.tracing.advice.AbstractSQLParserEngineAdviceTest;
import org.apache.shardingsphere.agent.plugin.tracing.core.RootSpanContext;
import org.apache.shardingsphere.agent.plugin.tracing.core.constant.AttributeConstants;
import org.apache.shardingsphere.agent.plugin.tracing.opentelemetry.constant.OpenTelemetryConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class OpenTelemetrySQLParserEngineAdviceTest extends AbstractSQLParserEngineAdviceTest {
    
    private static final String SQL_STATEMENT = "select 1";
    
    private final InMemorySpanExporter testExporter = InMemorySpanExporter.create();
    
    private Span parentSpan;
    
    @BeforeEach
    void setup() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(testExporter)).build();
        OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).buildAndRegisterGlobal().getTracer(OpenTelemetryConstants.TRACER_NAME);
        parentSpan = GlobalOpenTelemetry.getTracer(OpenTelemetryConstants.TRACER_NAME).spanBuilder("parent").startSpan();
        RootSpanContext.set(parentSpan);
    }
    
    @AfterEach
    void clean() {
        parentSpan.end();
        GlobalOpenTelemetry.resetForTest();
        testExporter.reset();
    }
    
    @Test
    void assertMethod() {
        OpenTelemetrySQLParserEngineAdvice advice = new OpenTelemetrySQLParserEngineAdvice();
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, "OpenTelemetry");
        advice.afterMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, null, "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertThat(spanItems.size(), is(1));
        assertThat(spanItems.get(0).getName(), is("/ShardingSphere/parseSQL/"));
        assertThat(spanItems.get(0).getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
        Attributes attributes = spanItems.get(0).getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(SQL_STATEMENT));
    }
    
    @Test
    void assertExceptionHandle() {
        OpenTelemetrySQLParserEngineAdvice advice = new OpenTelemetrySQLParserEngineAdvice();
        advice.beforeMethod(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, "OpenTelemetry");
        advice.onThrowing(getTargetObject(), null, new Object[]{SQL_STATEMENT, true}, new IOException(), "OpenTelemetry");
        List<SpanData> spanItems = testExporter.getFinishedSpanItems();
        assertThat(spanItems.size(), is(1));
        assertThat(spanItems.get(0).getName(), is("/ShardingSphere/parseSQL/"));
        assertThat(spanItems.get(0).getStatus().getStatusCode(), is(StatusCode.ERROR));
        assertThat(spanItems.get(0).getParentSpanId(), is(parentSpan.getSpanContext().getSpanId()));
        Attributes attributes = spanItems.get(0).getAttributes();
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.COMPONENT)), is(AttributeConstants.COMPONENT_NAME));
        assertThat(attributes.get(AttributeKey.stringKey(AttributeConstants.DB_STATEMENT)), is(SQL_STATEMENT));
    }
}
