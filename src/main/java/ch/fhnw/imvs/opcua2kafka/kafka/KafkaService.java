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
package ch.fhnw.imvs.opcua2kafka.kafka;

import ch.fhnw.imvs.opcua2kafka.schemas.Key;
import ch.fhnw.imvs.opcua2kafka.schemas.Message;

/**
 * Abstracts a Kafka Connection and enables sending messages to the configured topic.
 *
 * Handles connection setup and releasing resources.
 * All messages are sent as Avro schemas with the message type {@link Message} and key type {@link Key}.
 */
public interface KafkaService {

    /**
     * Sends a message to the specified topic with a default key.
     *
     * @param message message to send to the topic.
     */
    void sendMessage(Message message);

    /**
     * Sens a message with a custom key to the topic.
     * @param key key of the message
     * @param message message to send
     */
    void sendMessage(Key key, Message message);

    /**
     * Flush all current messages to the Kafka Broker(s).
     *
     * Usually all messages are flushed periodically.
     * This method should be called, when the application is during its shutdown to ensure all messages are sent to the Kafka broker.
     */
    void flush();
}
