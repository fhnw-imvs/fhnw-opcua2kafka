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
package ch.fhnw.imvs.opcua2kafka;

import ch.fhnw.imvs.opcua2kafka.message.impl.MessageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Opcua2kafkaApplication implements CommandLineRunner {

    private final MessageManager messageManager;

    @Autowired
    public Opcua2kafkaApplication(final MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    public static void main(final String[] args) {
        SpringApplication.run(Opcua2kafkaApplication.class, args);
    }

    @Override
    public void run(final String... args) throws Exception {
        messageManager.start();
    }
}
