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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service which handles polling of OPC UA attributes.
 *
 * This class relies on the {@link OpcUaService} implementation, which handles the connection setup.
 * Exposes the polled values over the Observer Pattern.
 */
public interface OpcUaPollService {

    /**
     * Start polling the provided OPC UA Node Ids.
     *
     * @param nodeIds Node Ids to poll.
     * @param interval Intervall between two polling cycles.
     * @param timeUnit Time Unit of the interval parameter.
     */
    void start(List<String> nodeIds, long interval, TimeUnit timeUnit);

    /**
     * Stops the polling of all polling nodes.
     */
    void stop();

    /**
     * Adds a listener to consume polled values.
     *
     * @param listener Listener
     */
    void addListener(OpcUaListener listener);

    /**
     * Removes a listener.
     *
     * @param listener Listener.
     */
    void removeListener(OpcUaListener listener);
}
