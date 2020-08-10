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

import ch.fhnw.imvs.opcua2kafka.config.MessageConfiguration;
import ch.fhnw.imvs.opcua2kafka.kafka.KafkaService;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaAttribute;
import ch.fhnw.imvs.opcua2kafka.message.MessagePipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DefaultMessagePipeFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMessagePipeFactory.class);

    @Bean
    @Autowired
    public MessagePipe<Map<OpcUaAttribute, String>> createDefaultPipe(final KafkaService kafkaService,
                                                                      final MessageConfiguration messageConfiguration) {
        final KafkaMessagePipe kafkaMessagePipe = new KafkaMessagePipe(kafkaService);
        final CreateMessagePipe createMessagePipe = new CreateMessagePipe(kafkaMessagePipe, messageConfiguration.getAggregateId());
        logger.info("MessagePipes configured");
        return createMessagePipe;
    }
}
