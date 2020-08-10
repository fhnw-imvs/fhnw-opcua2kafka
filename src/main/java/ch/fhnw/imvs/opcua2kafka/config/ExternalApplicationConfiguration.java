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
package ch.fhnw.imvs.opcua2kafka.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class ExternalApplicationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApplicationConfiguration.class);

    private static final String OPC_NODE_SEPARATOR = ",";

    @Value(value = "${configuration.opcua.server}")
    private String opcUaServer;

    @Value(value="${configuration.opcua.nodes}")
    private String opcUaNodes;

    @Value(value = "${configuration.opcua.polling.enabled}")
    private String opcUaPollingEnabled;

    @Value(value = "${configuration.opcua.polling.cycle}")
    private String opcUaPollingCycle;

    @Value(value = "${configuration.opcua.polling.nodes}")
    private String opcUaPollingNodes;

    @Value(value = "${configuration.kafka.bootstrapServers}")
    private String kafkaBootstrapServers;

    @Value(value = "${configuration.kafka.schemaRegistryAddress}")
    private String kafkaSchemaRegistryAddress;

    @Value(value = "${configuration.kafka.topic.name}")
    private String kafkaTopicName;

    @Value(value = "${configuration.kafka.topic.partitionCount}")
    private String kafkaTopicPartitionCount;

    @Value(value = "${configuration.kafka.topic.replicationFactor}")
    private String kafkaTopicReplicationFactor;

    @Value(value = "${configuration.aggregateId}")
    private String aggregateId;

    private ApplicationConfiguration applicationConfiguration;

    @Bean
    public KafkaConfiguration getKafkaConfiguration() {
        final ApplicationConfiguration applicationConfiguration = getApplicationConfiguration();
        if (applicationConfiguration != null) {
            return applicationConfiguration.getKafkaConfiguration();
        }
        return null;
    }

    @Bean
    public OpcUaConfiguration getOpcUaConfiguration() {
        final ApplicationConfiguration applicationConfiguration = getApplicationConfiguration();
        if (applicationConfiguration != null) {
            return applicationConfiguration.getOpcUaConfiguration();
        }
        return null;
    }

    @Bean
    public MessageConfiguration getMessageConfiguration() {
        final ApplicationConfiguration applicationConfiguration = getApplicationConfiguration();
        if (applicationConfiguration != null) {
            return applicationConfiguration.getMessageConfiguration();
        }
        return null;
    }

    private ApplicationConfiguration getApplicationConfiguration() {
        if (applicationConfiguration == null) {
            applicationConfiguration = readApplicationConfiguration();
            logger.debug("ApplicationConfiguration initialized: {}", applicationConfiguration);
        }
        return applicationConfiguration;
    }

    private ApplicationConfiguration readApplicationConfiguration() {
        final MessageConfiguration messageConfiguration = MessageConfiguration.builder().aggregateId(aggregateId).build();
        final KafkaConfiguration kafkaConfiguration = KafkaConfiguration
                .builder()
                .bootstrapAddress(kafkaBootstrapServers)
                .schemaRegistryAddress(kafkaSchemaRegistryAddress)
                .topicName(kafkaTopicName)
                .topicNumPartitions(Integer.parseInt(kafkaTopicPartitionCount))
                .replicationFactor(Integer.parseInt(kafkaTopicReplicationFactor))
                .build();


        final OpcUaConfiguration opcUaConfiguration = OpcUaConfiguration
                .builder()
                .opcUaServer(opcUaServer)
                .nodes(parseNodeIds(opcUaNodes))
                .usePollingService(Boolean.parseBoolean(opcUaPollingEnabled))
                .defaultPollingCycle(Integer.parseInt(opcUaPollingCycle))
                .pollingNodes(parseNodeIds(opcUaPollingNodes))
                .build();

        final ApplicationConfiguration applicationConfiguration = ApplicationConfiguration
                .builder()
                .kafkaConfiguration(kafkaConfiguration)
                .messageConfiguration(messageConfiguration)
                .opcUaConfiguration(opcUaConfiguration)
                .build();
        logger.info("Application Configuration: {}", applicationConfiguration);
        return applicationConfiguration;
    }

    private List<String> parseNodeIds(final String nodeIds) {
        if (nodeIds == null || nodeIds.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(nodeIds.split(OPC_NODE_SEPARATOR));
    }
}
