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

import java.util.Map;

/**
 * Generic listener for OPC UA relates services.
 *
 * Provides the received events as a {@link Map}.
 */
public interface OpcUaListener {

    /**
     * Notifies that a new message was received.
     *
     * All the received attributes are provided in the {@link Map}
     *
     * @param message Message or event from an OPC UA service.
     */
    void messageReceived(Map<OpcUaAttribute, String> message);
}
