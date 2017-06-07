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

import java.io.Closeable;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * The basic thread pool interface.
 *
 * <p>This interface encapsulates a thread pool for execution of CPU bound tasks plus an I/O service for execution
 * of I/O / async operations
 *
 * <p>The thread pool designated to execute CPU bound operations and async callbacks can be regular fixed size thread
 * pool (GeneralPurpose) or work stealing queue thread pool (WorkStealing) and all 3 types of thread pools are also
 * global singletons which can be acquired via get
 */

public interface ThreadPool {
    /**
     * The thread pool ids of the JVM wide global thread pools.
     */
    enum ThreadPoolId {
        GeneralPurpose,
        WorkStealing,
        NonBlocking,
    }

    /**
     * Shuts down the thread pool.
     *
     * <p>After the thread pool is shutdown any attempts to use it will throw an exception
     */
    void shutdown();

    /**
     * @return The thread pool is of this thread pool.
     */
    ThreadPoolId id();

    /**
     * Returns the I/O service associated with the thread pool.
     *
     * <p>If invoked on a thread pool which does not have an I/O service associated with it it will throw an exception
     *
     * @return I/O service associated with the thread pool to support async I/O operations
     */
    AsynchronousChannelGroup ioService();

    /**
     * Returns the executor service associated with the thread pool.
     *
     * @return Executor service associated with the thread pool to support CPU bound tasks / operations
     */
    ExecutorService executorService();

    /**
     * Obtains the respective global / JVM wide thread pool instance.
     *
     * <p>Note that this is lazy initialization singleton and the thread pool maybe initialized on the first call
     *
     * @param threadPoolId The thread pool id of the global instance requested
     * @return Thread pool instance
     */
    static ThreadPool getInstance(final ThreadPoolId threadPoolId) {
        return ThreadPoolImpl.getInstance(threadPoolId);
    }
}
