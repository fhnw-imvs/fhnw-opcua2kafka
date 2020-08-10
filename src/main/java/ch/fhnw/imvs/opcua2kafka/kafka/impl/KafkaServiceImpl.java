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
package ch.fhnw.imvs.opcua2kafka.kafka.impl;

import ch.fhnw.imvs.opcua2kafka.kafka.KafkaService;
import ch.fhnw.imvs.opcua2kafka.schemas.Key;
import ch.fhnw.imvs.opcua2kafka.schemas.Message;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class KafkaServiceImpl implements KafkaService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaServiceImpl.class);

    private final KafkaTemplate<Key, Message> kafkaTemplate;

    private final KafkaTopicConfig topicConfig;

    @Autowired
    public KafkaServiceImpl(final KafkaTemplate<Key, Message> kafkaTemplate, final KafkaTopicConfig topicConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicConfig = topicConfig;
    }

    @Override
    public void sendMessage(final Message message) {
        final Key key = Key.newBuilder().setNodeId(message.getPayload().getNodeId()).build();
        sendMessage(key, message);
    }

    @Override
    public void sendMessage(final Key key, final Message message) {
        final NewTopic t = topicConfig.topic();
        logger.debug("Topic to be sending to: topicName: {}, topicNumPartitions: {}, replicationFactor: {}", t.name(), t.numPartitions(), t.replicationFactor());
        logger.debug("Going to send Message with TraceId {}", message.getMetadata().getTraceId());
        final ListenableFuture<SendResult<Key, Message>> future = kafkaTemplate.send(t.name(), key, message);

        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(final SendResult<Key, Message> result) {
                logSuccess(result, key, message);
            }

            @Override
            public void onFailure(final Throwable ex) {
                logger.warn("Unable to send message {} due to {}", message, ex);
            }
        });
    }

    @Override
    public void flush() {
        kafkaTemplate.flush();
    }

    private void logSuccess(final SendResult<Key, Message> result, final Key key, final Message message) {
        logger.debug("Sent message [key={{}} message={}] with offset {}", key, message, result.getRecordMetadata().offset());
        logger.debug("Result record metadata: {}, topic: {}, partition: {}, offset: {}, serializedKeySize: {}, serializedValueSize: {}, original timestamp: {}, timestamp: {}",
                result.getRecordMetadata(),
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset(),
                result.getRecordMetadata().serializedKeySize(),
                result.getRecordMetadata().serializedValueSize(),
                result.getRecordMetadata().timestamp(),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(result.getRecordMetadata().timestamp()), ZoneId.systemDefault())
        );
    }
}
