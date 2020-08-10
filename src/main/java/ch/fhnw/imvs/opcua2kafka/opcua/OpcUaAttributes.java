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
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.util.List;
import java.util.function.Function;

public enum OpcUaAttributes implements OpcUaAttribute {
    NodeId(AttributeId.NodeId, (v) -> String.valueOf(v.getValue())),
    BrowseName(AttributeId.BrowseName, createQualifiedNameFormatter()),
    DisplayName(AttributeId.DisplayName, createLocalizedTextFormatter()),
    Description(AttributeId.Description, createLocalizedTextFormatter()),
    Value(AttributeId.Value, (v) -> String.valueOf(v.getValue())),
    Timestamp(null, String::valueOf);

    private final AttributeId attributeId;

    private final Function<Variant, String> formatFunction;

    OpcUaAttributes(final AttributeId attributeId, final Function<Variant, String> formatFunction) {
        this.attributeId = attributeId;
        this.formatFunction = formatFunction;
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public AttributeId getAttributeId() {
        return attributeId;
    }

    @Override
    public String asString(final Variant object) {
        return formatFunction.apply(object);
    }

    public static List<OpcUaAttribute> getAll() {
        return List.of(values());
    }

    private static Function<Variant, String> createQualifiedNameFormatter() {
        return (v) -> {
            if (v.getValue() instanceof QualifiedName) {
                return ((QualifiedName) v.getValue()).getName();
            }
            return "";
        };
    }

    private static Function<Variant, String> createLocalizedTextFormatter() {
        return (v) -> {
            if (v.getValue() instanceof LocalizedText) {
                return ((LocalizedText) v.getValue()).getText();
            }
            return "";
        };
    }
}
