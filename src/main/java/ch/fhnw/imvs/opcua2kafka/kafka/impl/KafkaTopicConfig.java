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

import ch.fhnw.imvs.opcua2kafka.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaTopicConfig.class);

    private final KafkaConfiguration kafkaConfiguration;

    @Autowired
    public KafkaTopicConfig(final KafkaConfiguration kafkaConfiguration) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        final Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfiguration.getBootstrapAddress());
        logger.debug("KafkaAdmin configured with: {}", configs);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topic() {
        final NewTopic newTopic = new NewTopic(kafkaConfiguration.getTopicName(),
                kafkaConfiguration.getTopicNumPartitions(),
                (short) kafkaConfiguration.getReplicationFactor());
        logger.debug("Topic= topicName: {}, topicNumPartitions: {}, replicationFactor: {}",
                newTopic.name(), newTopic.numPartitions(), newTopic.replicationFactor());
        return newTopic;
    }
}
