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

import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaAttribute;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaAttributes;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaListener;
import ch.fhnw.imvs.opcua2kafka.opcua.OpcUaService;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
class OpcUaServiceImpl implements OpcUaService {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaServiceImpl.class);

    private static final double DEFAULT_SUBSCRIPTION_POLLING_INTERVAL = 100.0;

    private CompletableFuture<UaClient> uaClient;

    private CompletableFuture<UaSubscription> uaSubscription;

    private final Collection<OpcUaListener> listeners;

    public OpcUaServiceImpl() {
        listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void connect(final String connectionUrl) {
        try {
            final List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(connectionUrl).get();
            final Optional<EndpointDescription> endpointDescription = endpoints.stream().filter((ed) -> ed.getSecurityMode() == MessageSecurityMode.None).findFirst();
            if (endpointDescription.isPresent()) {
                final OpcUaClientConfigBuilder configBuilder = new OpcUaClientConfigBuilder();
                configBuilder.setEndpoint(endpointDescription.get());
                configBuilder.setApplicationName(LocalizedText.english("opc2kafka"));
                uaClient = OpcUaClient.create(configBuilder.build()).connect();
            } else {
                logger.error("No compatible OPC UA endpoint found");
            }
        } catch (final UaException | InterruptedException | ExecutionException e) {
            logger.error("Could not create ua client", e);
        }
    }

    @Override
    public void disconnect() {
        if (uaClient != null) {
            uaClient.thenAccept((client) -> {
                final Collection<UaSubscription> subscriptions = client.getSubscriptionManager().getSubscriptions();
                subscriptions.forEach((s) -> client.getSubscriptionManager().deleteSubscription(s.getSubscriptionId()));
                client.disconnect();
                uaClient = null;
            });
        }
    }

    @Override
    public void monitorNodes(final Collection<String> nodeIds) {
        if (uaClient != null) {
            if (uaSubscription == null) {
                uaSubscription = uaClient.thenCompose((client) -> client.getSubscriptionManager().createSubscription(DEFAULT_SUBSCRIPTION_POLLING_INTERVAL));
            }
            final CompletableFuture<List<MonitoredItemCreateRequest>> monitorRequests = createMonitoredItemCreateRequests(nodeIds);

            final BiConsumer<UaMonitoredItem, Integer> onItemCreated =
                    (item, id) -> item.setValueConsumer(this::onSubscriptionValue);

            monitorRequests.thenAccept((requests) -> {
                uaSubscription.thenAccept((subscription) -> {
                    subscription.createMonitoredItems(TimestampsToReturn.Neither, requests, onItemCreated).thenAccept((uaMonitoredItems) -> {
                        for (final UaMonitoredItem item : uaMonitoredItems) {
                            if (item.getStatusCode().isGood()) {
                                logger.info("MonitoredItem created for nodeId={}", item.getReadValueId().getNodeId());
                            } else {
                                logger.warn(
                                        "failed to create item for nodeId={} (status={})",
                                        item.getReadValueId().getNodeId(), item.getStatusCode());
                            }
                        }
                    });
                });
            });
        }
    }

    @Override
    public void unMonitorNodes(final Collection<String> nodeIds) {
        if (uaClient != null) {
            if (uaSubscription == null) {
                return;
            }

            uaSubscription.thenAccept((subscription) -> {
                final List<NodeId> nodes = nodeIds
                        .stream()
                        .map(NodeId::parseSafe)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                final List<UaMonitoredItem> toRemove = subscription
                        .getMonitoredItems()
                        .stream()
                        .filter(mi -> nodes.contains(mi.getReadValueId().getNodeId()))
                        .collect(Collectors.toList());

                subscription.deleteMonitoredItems(toRemove)
                        .handle((r, t) -> {
                            if (t == null && r.stream().allMatch(StatusCode::isGood)) {
                                logger.info("Removed all nodes");
                            } else {
                                logger.warn("could not remove monitored items, ", t);
                            }
                            return null;
                        });
            });
        }
    }

    @Override
    public CompletableFuture<Map<OpcUaAttribute, String>> readAttributes(final String nodeId, final List<OpcUaAttribute> attributes) {
        logger.debug("Reading {} attributes for node '{}'", attributes.size(), nodeId);
        if (uaClient != null) {
            final Optional<NodeId> nodeIdOptional = NodeId.parseSafe(nodeId);
            if (nodeIdOptional.isPresent()) {
                return uaClient.thenCompose((client) -> {
                    final CompletableFuture<List<DataValue>> reads = client.read(
                            0,
                            TimestampsToReturn.Neither,
                            Collections.nCopies(attributes.size(), nodeIdOptional.get()),
                            attributes.stream().map(a -> a.getAttributeId().uid()).collect(Collectors.toList()));

                    return reads.handle((rs, throwable) -> {
                        if (throwable == null) {
                            final Map<OpcUaAttribute, String> results = new HashMap<>();
                            for (int i = 0; i < attributes.size(); i++) {
                                results.put(attributes.get(i), attributes.get(i).asString(rs.get(i).getValue()));
                            }
                            results.put(OpcUaAttributes.NodeId, nodeId);
                            return results;
                        } else {
                            logger.warn("could not read desired attributes", throwable);
                            return new HashMap<>();
                        }
                    });
                });
            }
        }
        return null;
    }

    @Override
    public void addListener(final OpcUaListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        logger.debug("OpcUaService.Listener {} added to OpcUaService", listener);
    }

    @Override
    public void removeListener(final OpcUaListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            logger.debug("OpcUaService.Listener {} removed from OpcUaService", listener);
        }
    }

    private MonitoringParameters createDefaultMonitoringParameters(final UInteger nextClientHandle) {
        return new MonitoringParameters(
                nextClientHandle,
                DEFAULT_SUBSCRIPTION_POLLING_INTERVAL,
                null,
                UInteger.valueOf(1),
                true
        );
    }

    private CompletableFuture<List<MonitoredItemCreateRequest>> createMonitoredItemCreateRequests(final Collection<String> nodeIds) {
        logger.debug("Creating MonitorRequests for {} nodes", nodeIds.size());
        return uaSubscription.thenApply(subscription -> {
            final List<MonitoredItemCreateRequest> monitorRequests = new ArrayList<>();
            for (final String nodeId : nodeIds) {
                final Optional<NodeId> nodeIdOptional = NodeId.parseSafe(nodeId);

                if (nodeIdOptional.isPresent()) {
                    final ReadValueId readValueId = new ReadValueId(
                            nodeIdOptional.get(), AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);

                    final UInteger nextClientHandle = subscription.nextClientHandle();
                    final MonitoringParameters parameters = createDefaultMonitoringParameters(nextClientHandle);

                    final MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                            readValueId,
                            MonitoringMode.Reporting,
                            parameters
                    );

                    monitorRequests.add(request);
                }
            }
            return monitorRequests;
        });
    }

    private void onSubscriptionValue(final UaMonitoredItem item, final DataValue value) {
        logger.debug("subscription value received: item={}, value={}", item.getReadValueId().getNodeId(), value.getValue());
        this.readAttributes(item.getReadValueId().getNodeId().toParseableString(), List.of(OpcUaAttributes.BrowseName, OpcUaAttributes.Description, OpcUaAttributes.DisplayName))
                .thenAccept((map) -> {
                    final Map<OpcUaAttribute, String> resultMap = new HashMap<>(map);
                    resultMap.put(OpcUaAttributes.Value, String.valueOf(value.getValue().getValue()));
                    logger.debug("Value map={}", map);
                    for (final OpcUaListener listener : listeners) {
                        listener.messageReceived(Collections.unmodifiableMap(resultMap));
                    }
                });
    }
}
