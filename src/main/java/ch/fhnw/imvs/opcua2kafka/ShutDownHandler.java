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
package ch.fhnw.imvs.opcua2kafka;

import ch.fhnw.imvs.opcua2kafka.kafka.KafkaService;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaPollService;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * Handles the shutdown of this application and cleans up resources e.g. OPC UA Server connections.
 */
@Component
public class ShutDownHandler {

    private static final Logger logger = LoggerFactory.getLogger(ShutDownHandler.class);

    private final OpcUaService opcUaService;

    private final OpcUaPollService opcUaPollService;

    private final KafkaService kafkaService;

    @Autowired
    public ShutDownHandler(final OpcUaService opcUaService,
                           final OpcUaPollService opcUaPollService,
                           final KafkaService kafkaService) {
        this.opcUaService = opcUaService;
        this.opcUaPollService = opcUaPollService;
        this.kafkaService = kafkaService;
    }

    @PreDestroy
    public void destroy() {
        logger.info("Shutting down application...");
        opcUaPollService.stop();
        logger.info("Successfully stopped OPC Poll service");

        opcUaService.disconnect();
        logger.info("Successfully disconnected OPC UA service");

        kafkaService.flush();
        logger.info("Successfully flushed to Kafka");
        logger.info("Application shutdown completed successfully");
    }
}
