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

package org.swblocks.jbl.tasks;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.junit.Test;
import org.swblocks.jbl.eh.EhSupport;
import org.swblocks.jbl.util.MutableObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * The tests for the {@link ThreadPool} class.
 */
public class ThreadPoolTest {
    @Test
    public void testCreateAndShutdown() {
        final Consumer<ThreadPool.ThreadPoolId> threadPoolTester = (threadPoolId) ->
                EhSupport.propagate(() -> {
                    final ThreadPool pool = ThreadPool.getInstance(threadPoolId);
                    assertEquals(threadPoolId, pool.id());
                    assertNotNull(pool.executorService());

                    final BiFunction<Runnable, String, Object> errorTester = (callback, expectedMessage) -> {
                        try {
                            callback.run();
                        } catch (final Throwable fatal) {
                            assertEquals(
                                    fatal.getClass().getCanonicalName(),
                                    "org.swblocks.jbl.eh.FatalApplicationError"
                            );
                            assertEquals(
                                    fatal.getMessage(),
                                    "A fatal application error has occurred"
                            );
                            assertEquals(fatal.getCause().getMessage(), expectedMessage);
                        }
                        ;

                        return null;
                    };

                    if (ThreadPool.ThreadPoolId.NonBlocking.equals(threadPoolId)) {
                        assertNotNull(pool.ioService());
                    } else {
                        errorTester.apply(
                                () -> pool.ioService(),
                                /* expectedMessage */
                                "I/O service requested for thread pool which does not support it"
                        );
                    }

                    final MutableObject<Boolean> invoked = new MutableObject<>(Boolean.FALSE);
                    pool.executorService().submit(() -> invoked.setValue(Boolean.TRUE)).get();
                    assertEquals(invoked.getValue(), Boolean.TRUE);

                    pool.shutdown();

                    errorTester.apply(
                            () -> pool.ioService(),
                            /* expectedMessage */
                            "I/O service requested for thread pool which does not support it"
                    );
                    errorTester.apply(
                            () -> pool.executorService(),
                            /* expectedMessage */
                            "The executor service requested for thread pool is not available"
                    );
                });

        threadPoolTester.accept(ThreadPool.ThreadPoolId.GeneralPurpose);
        threadPoolTester.accept(ThreadPool.ThreadPoolId.WorkStealing);
        threadPoolTester.accept(ThreadPool.ThreadPoolId.NonBlocking);
    }
}
