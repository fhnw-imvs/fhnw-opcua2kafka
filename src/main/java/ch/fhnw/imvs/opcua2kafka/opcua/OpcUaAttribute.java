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

import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

/**
 * Identifies an OPC UA Attribute which can be received from an OPC UA Server.
 */
public interface OpcUaAttribute {

    /**
     * Custom attribute key.
     *
     * @return Attribute Key.
     */
    String getKey();

    /**
     * Eclipse Milo attribute id.
     *
     * @return Attribute id
     */
    AttributeId getAttributeId();

    /**
     * Formats a provieded attribute value into its {@link String} representation.
     * @param object Object to convert
     * @return String representation of value.
     */
    String asString(Variant object);
}
