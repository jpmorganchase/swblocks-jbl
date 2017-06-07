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

import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadFactory;

import org.swblocks.jbl.eh.EhSupport;

/**
 * The default thread pool implementation.
 *
 * <p>It creates a wrapper for both thread pool + associated async I/O channel group service to allow execution of both
 * compute / CPU based tasks and async I/O tasks at the same time
 *
 * <p>The thread pool that handles actual CPU bound tasks can be regular thread pool or work stealing queue pool (i.e.
 * fork-join thread pool)
 */
public class ThreadPoolImpl implements ThreadPool {
    private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

    private final ThreadPoolId id;
    private ExecutorService executorService;
    private AsynchronousChannelGroup ioService;

    private ThreadPoolImpl(final int numberOfThreads, final ThreadPoolId id) {
        this.id = id;

        if (ThreadPoolId.WorkStealing == id) {
            this.executorService = new ForkJoinPool(
                    numberOfThreads                                    /* parallelism level */,
                    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                    EhSupport.getUncaughtHandler()                     /* uncaught exception handler */,
                    true                                    /* asyncMode */
            );
        } else {
            this.executorService = Executors.newFixedThreadPool(
                    numberOfThreads,
                    (runnable) -> defaultFactory.newThread(() -> EhSupport.fatalOnException(() -> {
                        Thread.currentThread().setUncaughtExceptionHandler(EhSupport.getUncaughtHandler());
                        runnable.run();
                    }))
            );
        }

        if (ThreadPoolId.NonBlocking == this.id) {
            try {
                this.ioService = EhSupport.propagateFn(
                        () -> AsynchronousChannelGroup.withThreadPool(this.executorService)
                );
            } catch (final Throwable throwable) {
                this.executorService.shutdownNow();
                throw throwable;
            }
        } else {
            this.ioService = null;
        }
    }

    @Override
    public void shutdown() {
        EhSupport.fatalOnException(() -> {
            /*
             * For the I/O service completion queue we always force shutdown as we
             * don't want to wait, but we want to know the completion handlers are
             * executed
             *
             * For the executor itself we need to wait for all pending tasks to be
             * flushed and executed
             */
            if (null != this.ioService) {
                this.ioService.shutdownNow();
                this.ioService = null;
            }
            this.executorService.shutdown();
            this.executorService = null;
        });
    }

    @Override
    public ThreadPoolId id() {
        return this.id;
    }

    @Override
    public AsynchronousChannelGroup ioService() {
        EhSupport.ensureOrFatal(
                null != this.ioService,
                "I/O service requested for thread pool which does not support it"
        );

        return this.ioService;
    }

    @Override
    public ExecutorService executorService() {
        EhSupport.ensureOrFatal(
                null != this.executorService,
                "The executor service requested for thread pool is not available"
        );

        return this.executorService;
    }

    private static class LazyInitializerGeneralPurpose {
        private static final int NUMBER_OF_THREADS_DEFAULT = 32;
        private static final ThreadPool g_instance =
                new ThreadPoolImpl(NUMBER_OF_THREADS_DEFAULT, ThreadPoolId.GeneralPurpose);
    }

    private static class LazyInitializerWorkStealing {
        private static final int PARALLELISM_LEVEL_DEFAULT = Runtime.getRuntime().availableProcessors();
        private static final ThreadPool g_instance =
                new ThreadPoolImpl(PARALLELISM_LEVEL_DEFAULT, ThreadPoolId.WorkStealing);
    }

    private static class LazyInitializerNonBlocking {
        private static final int NUMBER_OF_THREADS_NON_BLOCKING = 8;
        private static final ThreadPool g_instance =
                new ThreadPoolImpl(NUMBER_OF_THREADS_NON_BLOCKING, ThreadPoolId.NonBlocking);
    }

    /**
     * See documentation for {@link ThreadPool#getInstance(ThreadPoolId)}.
     */
    public static ThreadPool getInstance(final ThreadPoolId threadPoolId) {
        return EhSupport.propagateFn(() -> {
            switch (threadPoolId) {
                case GeneralPurpose:
                    return LazyInitializerGeneralPurpose.g_instance;
                case WorkStealing:
                    return LazyInitializerWorkStealing.g_instance;
                case NonBlocking:
                    return LazyInitializerNonBlocking.g_instance;
                default:
                    throw EhSupport.enhance(new IllegalStateException(
                            String.format("The requested thread pool id {} is not supported", threadPoolId)));
            }
        });
    }
}
