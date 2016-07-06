/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offbynull.portmapper.gateways.network;

import com.offbynull.portmapper.gateway.Bus;
import java.nio.channels.Selector;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang3.Validate;

final class NetworkBus implements Bus {
    private Selector selector;
    private LinkedBlockingQueue<Object> queue;

    NetworkBus(Selector selector, LinkedBlockingQueue<Object> queue) {
        Validate.notNull(selector);
        Validate.notNull(queue);
        this.selector = selector;
        this.queue = queue;
    }

    @Override
    public void send(Object msg) {
        Validate.notNull(msg);
        queue.add(msg);
        selector.wakeup();
    }
}
