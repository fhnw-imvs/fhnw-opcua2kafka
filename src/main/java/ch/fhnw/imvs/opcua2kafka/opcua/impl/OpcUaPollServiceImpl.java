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
package ch.fhnw.imvs.opcua2kafka.opcua.impl;

import ch.fhnw.imvs.opcua2kafka.opcua.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class OpcUaPollServiceImpl implements OpcUaPollService {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaPollServiceImpl.class);

    private final OpcUaService opcUaService;

    private final Collection<OpcUaListener> listeners;

    private ScheduledExecutorService executorService;

    private List<String> nodeIds;

    @Autowired
    public OpcUaPollServiceImpl(final OpcUaService opcUaService) {
        listeners = new CopyOnWriteArrayList<>();
        this.opcUaService = opcUaService;
    }

    @Override
    public void start(final List<String> nodeIds, final long interval, final TimeUnit timeUnit) {
        logger.info("OpcUaPollService starting...");
        this.nodeIds = nodeIds;
        if (executorService != null) {
            executorService.shutdown();
        }
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::poll, 0, interval, timeUnit);
        logger.debug("OpcUaPollService executorService scheduled with pollingInterval {} {}", interval, timeUnit);
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
        }
        executorService = null;
        logger.debug("OpcUaPollService executorService stopped");
    }

    @Override
    public void addListener(final OpcUaListener listener) {
        if (listener != null) {
            listeners.add(listener);
            logger.debug("OpcUaService.Listener {} added to OpcUaPollService", listener);
        }
    }

    @Override
    public void removeListener(final OpcUaListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            logger.debug("OpcUaService.Listener {} removed from OpcUaPollService", listener);
        }
    }

    private void poll() {
        logger.debug("Polling started for {} nodes", nodeIds.size());
        for (final String nodeId : nodeIds) {
            final CompletableFuture<Map<OpcUaAttribute, String>> result = opcUaService.readAttributes(
                    nodeId,
                    List.of(OpcUaAttributes.BrowseName, OpcUaAttributes.Value, OpcUaAttributes.DisplayName, OpcUaAttributes.Description));
            result.handle((values, throwable) -> {
                if (throwable == null) {
                    logger.debug("Poll for NodeId={} succeeded, values={}", nodeId, values);
                    final Map<OpcUaAttribute, String> unmodifiableValues = Collections.unmodifiableMap(values);
                    for (final OpcUaListener listener : listeners) {
                        listener.messageReceived(unmodifiableValues);
                    }
                } else {
                    logger.warn("Poll for NodeId={} failed, {}", nodeId, throwable);
                }
                return null;
            });
        }
    }
}
