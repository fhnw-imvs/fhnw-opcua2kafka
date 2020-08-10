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

import ch.fhnw.imvs.opcua2kafka.kafka.KafkaService;
import ch.fhnw.imvs.opcua2kafka.schemas.Message;
import ch.fhnw.imvs.opcua2kafka.schemas.Metadata;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KafkaMessagePipeTest {

    @Mock
    private KafkaService kafkaService;

    @Mock
    private Message message;

    @Mock
    private Metadata metaData;

    @Test
    public void pipe() {
        Mockito.when(message.getMetadata()).thenReturn(metaData);
        Mockito.when(metaData.getTraceId()).thenReturn("1234");

        final KafkaMessagePipe kafkaMessagePipe = new KafkaMessagePipe(kafkaService);
        kafkaMessagePipe.pipe(message);

        Mockito.verify(kafkaService).sendMessage(message);
    }
}