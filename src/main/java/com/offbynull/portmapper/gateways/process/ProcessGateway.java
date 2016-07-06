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
package com.offbynull.portmapper.gateways.process;

import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.gateway.Gateway;

/**
 * Process gateway.
 * @author Kasra Faghihi
 */
public final class ProcessGateway implements Gateway {

    private ProcessRunnable runnable;
    private Thread thread;
    
    /**
     * Creates a {@link ProcessGateway} object.
     * @return new {@link ProcessGateway}
     */
    public static ProcessGateway create() {
        ProcessGateway pg = new ProcessGateway();
        
        pg.runnable = new ProcessRunnable();
        pg.thread = new Thread(pg.runnable);
        pg.thread.setDaemon(true);
        pg.thread.setName("Process IO");
        
        pg.thread.start();
        
        return pg;
    }
    
    private ProcessGateway() {
        // do nothing
    }
    
    @Override
    public Bus getBus() {
        return runnable.getBus();
    }
    
    @Override
    public void join() throws InterruptedException {
        thread.join();
    }
}
