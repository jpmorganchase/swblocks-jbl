/*
 * This file is part of the swblocks-jbl library.
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

package org.swblocks.jbl.apps;

import org.swblocks.jbl.eh.EhSupport;

/**
 * A test application for random manual tests.
 */

public class TestApplication {
    /**
     * Manual test for uncaught exception termination.
     */
    public static void testUncaughtExceptionTermination() {
        EhSupport.propagate(() -> {
            final Thread thread = new Thread(() ->
                    EhSupport.throwEnhanced(
                            new IllegalStateException("Exception thrown from a thread should halt the VM"))
            );
            thread.start();
            thread.join();
            System.err.println("Thread completed without halting the VM. This should not happen!");
        });
    }

    public static void main(final String[] args) {
        EhSupport.throwEnhanced(new IllegalStateException("This code must be replaced with actual test code"));
        // testUncaughtExceptionTermination();
    }
}
