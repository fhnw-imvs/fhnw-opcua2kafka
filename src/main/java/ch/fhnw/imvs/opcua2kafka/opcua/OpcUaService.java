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
package ch.fhnw.imvs.opcua2kafka.opcua;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract connection handling with an OPC UA Server.
 *
 * This service monitors OPC UA Node and generates a message if its value has changed.
 */
public interface OpcUaService {

    /**
     * Connects to an OPC UA server.
     *
     * This call is blocking and will return if the connection was successful or failed.
     * @param connectionUrl Connection URL of the OPC UA server.
     */
    void connect(String connectionUrl);

    /**
     * Disconnects from the connected server.
     */
    void disconnect();

    /**
     * Monitors the specified nodes.
     *
     * @param nodeIds Node Ids to monitor.
     */
    void monitorNodes(Collection<String> nodeIds);

    /**
     * Stops the monitoring of the specified nodes.
     *
     * @param nodeIds Nodes Ids to stop monitoring.
     */
    void unMonitorNodes(Collection<String> nodeIds);

    /**
     * Reads async the specified attributes of the specified node.
     * @param nodeId Node Id to fetch attributes.
     * @param attributes Attributes to read from the node.
     * @return Future wich contains the attributes and its values in their {@link String} representation.
     */
    CompletableFuture<Map<OpcUaAttribute, String>> readAttributes(String nodeId, List<OpcUaAttribute> attributes);

    /**
     * Adds a listener to get value changes from the monitored nodes.
     *
     * @param listener Listener
     */
    void addListener(OpcUaListener listener);

    /**
     * Removes the specified listener.
     * @param listener Listener
     */
    void removeListener(OpcUaListener listener);
}
