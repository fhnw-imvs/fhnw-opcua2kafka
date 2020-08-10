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

import ch.fhnw.imvs.opcua2kafka.kafka.KafkaService;
import ch.fhnw.imvs.opcua2kafka.message.MessagePipe;
import ch.fhnw.imvs.opcua2kafka.schemas.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class KafkaMessagePipe implements MessagePipe<Message> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessagePipe.class);

    private final KafkaService kafkaService;

    public KafkaMessagePipe(final KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Override
    public void pipe(final Message message) {
        logger.debug("KafkaMessagePipe received message with TraceId {}", message.getMetadata().getTraceId());
        kafkaService.sendMessage(message);
    }
}
