/*
 * Copyright 2020 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package ch.fhnw.imvs.opcua2kafka.message.impl;

import ch.fhnw.imvs.opcua2kafka.message.MessagePipe;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaAttribute;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaAttributes;
import ch.fhnw.imvs.opcua2kafka.schemas.Message;
import ch.fhnw.imvs.opcua2kafka.schemas.Metadata;
import ch.fhnw.imvs.opcua2kafka.schemas.Payload;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public final class CreateMessagePipeTest {

    private static final String aggregateId = "test";

    private SinkMessagePipe sinkMessagePipe;

    private CreateMessagePipe createMessagePipe;

    @Before
    public void setUp() {
        sinkMessagePipe = new SinkMessagePipe();
        createMessagePipe = new CreateMessagePipe(sinkMessagePipe, aggregateId);
    }

    @Test
    public void testPipeWithNull() {
        // should not throw exception - null safe
        createMessagePipe.pipe(null);
    }

    @Test
    public void testPipeWithEmptyMap() {
        createMessagePipe.pipe(new HashMap<>());
        assertNull(sinkMessagePipe.receivedMessage);
    }

    @Test
    public void testPipeWithFullMap() {
        final Map<OpcUaAttribute, String> values = new HashMap<>();
        values.put(OpcUaAttributes.NodeId, "NodeId");
        values.put(OpcUaAttributes.BrowseName, "Browse");
        values.put(OpcUaAttributes.DisplayName, "Display");
        values.put(OpcUaAttributes.Description, "Description");
        values.put(OpcUaAttributes.Value, "Value");

        createMessagePipe.pipe(values);

        final Message receivedMessage = sinkMessagePipe.receivedMessage;
        assertNotNull(receivedMessage);
        final Metadata metadata = receivedMessage.getMetadata();
        final Payload payload = receivedMessage.getPayload();
        assertNotNull(metadata);
        assertNotNull(payload);

        assertEquals(aggregateId, metadata.getAggregateId());
        assertNotNull(metadata.getTraceId());
        assertNotNull(metadata.getTimestamp());

        assertEquals("NodeId", payload.getNodeId());
        assertEquals("Browse", payload.getBrowseName());
        assertEquals("Display", payload.getDisplayName());
        assertEquals("Description", payload.getDescription());
        assertEquals("Value", payload.getValue());
    }

    @Test
    public void testPipeWithFullMapEmptyValues() {
        final Map<OpcUaAttribute, String> values = new HashMap<>();
        values.put(OpcUaAttributes.NodeId, "");
        values.put(OpcUaAttributes.BrowseName, "");
        values.put(OpcUaAttributes.DisplayName, "");
        values.put(OpcUaAttributes.Description, "");
        values.put(OpcUaAttributes.Value, "");

        createMessagePipe.pipe(values);

        final Message receivedMessage = sinkMessagePipe.receivedMessage;
        assertNotNull(receivedMessage);
        final Metadata metadata = receivedMessage.getMetadata();
        final Payload payload = receivedMessage.getPayload();
        assertNotNull(metadata);
        assertNotNull(payload);

        assertEquals(aggregateId, metadata.getAggregateId());
        assertNotNull(metadata.getTraceId());
        assertNotNull(metadata.getTimestamp());

        assertEquals("", payload.getNodeId());
        assertEquals("", payload.getBrowseName());
        assertEquals("", payload.getDisplayName());
        assertEquals("", payload.getDescription());
        assertEquals("", payload.getValue());
    }

    private static final class SinkMessagePipe implements MessagePipe<Message> {

        private volatile Message receivedMessage;

        @Override
        public void pipe(final Message message) {
            this.receivedMessage = message;
        }
    }
}