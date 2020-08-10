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

import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaAttribute;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaAttributes;
import ch.fhnw.imvs.opcua2kafka.schemas.Metadata;
import ch.fhnw.imvs.opcua2kafka.schemas.Payload;
import ch.fhnw.imvs.opcua2kafka.message.MessagePipe;
import ch.fhnw.imvs.opcua2kafka.schemas.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


public final class CreateMessagePipe implements MessagePipe<Map<OpcUaAttribute, String>> {

    private static final Logger logger = LoggerFactory.getLogger(CreateMessagePipe.class);

    private final MessagePipe<Message> nextPipe;

    private final String aggregateId;

    public CreateMessagePipe(final MessagePipe<Message> nextPipe, final String aggregateId) {
        this.nextPipe = nextPipe;
        this.aggregateId = aggregateId;
        logger.info("CreateMessagePipe initialized");
    }

    @Override
    public void pipe(final Map<OpcUaAttribute, String> message) {
        if (message != null && !message.isEmpty()) {
            logger.debug("Message received by CreateMessagePipe. Going to build 'Message'");
            final Metadata metadata = Metadata.newBuilder()
                    .setAggregateId(aggregateId)
                    .setTraceId(UUID.randomUUID().toString())
                    .setTimestamp(Instant.now())
                    .build();
            final Payload payload = Payload.newBuilder()
                    .setNodeId(message.get(OpcUaAttributes.NodeId))
                    .setBrowseName(message.get(OpcUaAttributes.BrowseName))
                    .setDisplayName(message.get(OpcUaAttributes.DisplayName))
                    .setDescription(message.get(OpcUaAttributes.Description))
                    .setValue(message.get(OpcUaAttributes.Value))
                    .build();

            final Message m = Message.newBuilder()
                    .setMetadata(metadata)
                    .setPayload(payload)
                    .build();
            logger.debug("Message built. Going to forward message: {}", m);
            nextPipe.pipe(m);
        }
    }
}
