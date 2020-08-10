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

import ch.fhnw.imvs.opcua2kafka.config.OpcUaConfiguration;
import ch.fhnw.imvs.opcua2kafka.message.MessagePipe;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaAttribute;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaPollService;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MessageManager {

    private static final Logger logger = LoggerFactory.getLogger(MessageManager.class);

    private final OpcUaService opcUaService;

    private final OpcUaPollService opcUaPollService;

    private final OpcUaConfiguration opcUaConfiguration;

    private final MessagePipe<Map<OpcUaAttribute, String>> messagePipe;

    @Autowired
    public MessageManager(final OpcUaService opcUaService,
                          final OpcUaPollService opcUaPollService,
                          final OpcUaConfiguration opcUaConfiguration,
                          final MessagePipe<Map<OpcUaAttribute, String>> messagePipe) {
        this.opcUaService = opcUaService;
        this.opcUaPollService = opcUaPollService;
        this.opcUaConfiguration = opcUaConfiguration;
        this.messagePipe = messagePipe;
        logger.debug("MessageManager initialized");
    }

    public void start() {
        logger.info("Starting MessageManager");
        if (opcUaConfiguration != null) {
            logger.debug("OPC UA Configuration={}", opcUaConfiguration);
            startOpcUaService(opcUaConfiguration.getMonitorNodes());
            if (opcUaConfiguration.isUsePollingService()) {
                startOpcUaPollService(opcUaConfiguration.getPollingNodes());
            }
        } else {
            logger.warn("Unable to start OpcUaServices because 'opcUaConfiguration' is 'null'");
            logger.warn("Application not started...");
        }
    }

    private void pipe(final Map<OpcUaAttribute, String> values) {
        logger.debug("MessageManager received Message: {}", values);
        messagePipe.pipe(values);
    }

    private void startOpcUaService(final List<String> nodeIds) {
        opcUaService.connect(opcUaConfiguration.getOpcUaServer());
        opcUaService.monitorNodes(nodeIds);
        opcUaService.addListener(this::pipe);
        opcUaService.addListener(message -> logger.info("Received OPC UA Event Message: {}", message));
        logger.info("OpcUaService started.");
    }

    private void startOpcUaPollService(final List<String> nodeIds) {
        opcUaPollService.start(nodeIds, opcUaConfiguration.getDefaultPollingCycle(), TimeUnit.MILLISECONDS);
        opcUaPollService.addListener(this::pipe);
        logger.info("OpcUaPollService started.");
    }
}