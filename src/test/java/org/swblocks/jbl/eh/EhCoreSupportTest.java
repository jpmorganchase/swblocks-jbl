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

package org.swblocks.jbl.eh;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;
import org.swblocks.jbl.util.MutableObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Basic core exception support helpers tests.
 */
public class EhCoreSupportTest {
    private static void throwsCheckedException() throws IOException {
        throw new IOException("Test IO exception");
    }

    private static void throwsSilently() {
        EhCoreSupport.propagate(EhCoreSupportTest::throwsCheckedException);
    }

    private static Long throwsSilentlyFn() {
        return EhCoreSupport.propagateFn(() -> {
            throwsCheckedException();
            return 42L;
        });
    }

    private static Long doesNotThrowFn() {
        return EhCoreSupport.propagateFn(() -> 42L
        );
    }

    /**
     * Invokes the protected default instructor to remove code coverage noise.
     */
    @Test
    public void invokeProtectedDefaultConstructor() {
        EhCoreSupport.propagate(() -> {
            final Constructor<EhCoreSupport> constructor = EhCoreSupport.class.getDeclaredConstructor();
            assertTrue(Modifier.isProtected(constructor.getModifiers()));
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    public void propagateExceptionsTests() {
        try {
            throwsSilently();
            fail("This call must throw");
        } catch (final Exception exception) {
            assertEquals(exception.getClass(), IOException.class);
        }
    }

    @Test
    public void propagateExceptionsFnTests() {
        try {
            throwsSilentlyFn();
            fail("This call must throw");
        } catch (final Exception exception) {
            assertEquals(exception.getClass(), IOException.class);
        }

        assertEquals(doesNotThrowFn(), Long.valueOf(42L));
    }

    @Test
    public void fatalOnExceptionTests() {

        assertNotNull(EhCoreSupport.getAbortHandler());
        assertNotNull(EhCoreSupport.defaultAbortHandler);
        assertEquals(EhCoreSupport.defaultAbortHandler, EhCoreSupport.getAbortHandler());

        try {
            EhCoreSupport.fatalOnException(() -> {
                throwsSilently();
                fail("This call must throw");
            });
        } catch (final FatalApplicationError throwable) {
            assertEquals(throwable.getMessage(), "A fatal application error has occurred");
            assertNotNull(throwable.getCause());
            assertTrue(throwable.getCause() instanceof IOException);
            assertEquals(throwable.getCause().getMessage(), "Test IO exception");
        }
    }

    @Test
    public void fatalOnExceptionFnTests() {

        assertNotNull(EhCoreSupport.getAbortHandler());
        assertNotNull(EhCoreSupport.defaultAbortHandler);
        assertEquals(EhCoreSupport.defaultAbortHandler, EhCoreSupport.getAbortHandler());

        try {
            EhCoreSupport.fatalOnExceptionFn(EhCoreSupportTest::throwsSilentlyFn);
        } catch (final FatalApplicationError throwable) {
            assertEquals(throwable.getMessage(), "A fatal application error has occurred");
            assertNotNull(throwable.getCause());
            assertTrue(throwable.getCause() instanceof IOException);
            assertEquals(throwable.getCause().getMessage(), "Test IO exception");
        }

        final Long expectedValue = EhCoreSupport.fatalOnExceptionFn(EhCoreSupportTest::doesNotThrowFn);
        assertEquals(expectedValue, (Long) 42L);
    }

    @Test
    public void fatalOnExceptionNewHandlerTests() {

        assertNotNull(EhCoreSupport.getAbortHandler());
        assertNotNull(EhCoreSupport.defaultAbortHandler);
        assertEquals(EhCoreSupport.defaultAbortHandler, EhCoreSupport.getAbortHandler());

        try {
            final String exceptionMessage = "My illegal state exception message";

            final Consumer<Throwable> newHandler = throwable -> {
                throw new IllegalStateException(exceptionMessage, throwable);
            };

            EhCoreSupport.setAbortHandler(newHandler);

            assertNotNull(EhCoreSupport.getAbortHandler());
            assertEquals(newHandler, EhCoreSupport.getAbortHandler());

            try {
                EhCoreSupport.fatalOnException(() -> {
                    throwsSilently();
                    fail("This call must throw");
                });
            } catch (final IllegalStateException exception) {
                assertEquals(exception.getMessage(), exceptionMessage);
                assertNotNull(exception.getCause());
                assertTrue(exception.getCause() instanceof IOException);
                assertEquals(exception.getCause().getMessage(), "Test IO exception");
            }

            EhCoreSupport.setAbortHandler(null);
            assertEquals(EhCoreSupport.defaultAbortHandler, EhCoreSupport.getAbortHandler());
        } finally {
            EhCoreSupport.setAbortHandler(EhCoreSupport.defaultAbortHandler);
        }
    }

    @Test
    public void ensureParameterConditionPasses() {
        EhCoreSupport.ensureArg(true, "argument %s is invalid", "arg1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureParameterConditionFails() {
        EhCoreSupport.ensureArg(false, "argument %s is invalid", "arg1");
    }

    @Test
    public void ensureInvariantConditionPasses() {
        EhCoreSupport.ensure(true, "argument %s is invalid", "arg1");
    }

    @Test(expected = IllegalStateException.class)
    public void ensureInvariantConditionFails() {
        EhCoreSupport.ensure(false, "argument %s is invalid", "arg1");
    }

    @Test
    public void ensureOrFatalConditionPasses() {
        EhCoreSupport.ensureOrFatal(true, "no error");
        EhCoreSupport.ensureOrFatal(true, new NullPointerException("NPE"), "no error");
    }

    @Test
    public void ensureOrFatalConditionFailsWithMessage() {
        try {
            EhCoreSupport.ensureOrFatal(false, "error");
            fail("did not throw exception");
        } catch (final FatalApplicationError exception) {
            assertEquals("A fatal application error has occurred", exception.getMessage());
            assertNotNull(exception.getCause());
            assertEquals("error", exception.getCause().getMessage());
        }
    }

    @Test
    public void ensureOrFatalConditionFailsWithFormattedMessage() {
        try {
            EhCoreSupport.ensureOrFatal(false, "error: %s", 42L);
            fail("did not throw exception");
        } catch (final FatalApplicationError exception) {
            assertNotNull(exception.getCause());
            assertEquals("error: 42", exception.getCause().getMessage());
        }
    }

    @Test
    public void ensureOrFatalConditionFailsWithCause() {
        try {
            EhCoreSupport.ensureOrFatal(false, new NullPointerException("NPE"), "error");
            fail("did not throw exception");
        } catch (final FatalApplicationError exception) {
            assertEquals("A fatal application error has occurred", exception.getMessage());
            assertNotNull(exception.getCause());
            assertEquals("error", exception.getCause().getMessage());
            assertNotNull(exception.getCause().getCause());
            assertTrue(exception.getCause().getCause() instanceof NullPointerException);
            assertEquals("NPE", exception.getCause().getCause().getMessage());
        }
    }

    @Test
    public void abortThrowsErrorWithMessage() {
        try {
            EhCoreSupport.abort("error");
            fail("did not throw exception");
        } catch (final FatalApplicationError exception) {
            assertNotNull(exception.getCause());
            assertEquals("error", exception.getCause().getMessage());
        }
    }

    @Test
    public void abortThrowsErrorWithMessageAndCause() {
        try {
            EhCoreSupport.abort("error", new NullPointerException("NPE"));
            fail("did not throw exception");
        } catch (final FatalApplicationError exception) {
            assertEquals("A fatal application error has occurred", exception.getMessage());
            assertNotNull(exception.getCause());
            assertEquals("error", exception.getCause().getMessage());
            assertNotNull(exception.getCause().getCause());
            assertTrue(exception.getCause().getCause() instanceof NullPointerException);
            assertEquals("NPE", exception.getCause().getCause().getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void enhancedFinallyWithException() {
        EhCoreSupport.enhancedFinally(() -> EhCoreSupport.rethrow(new IllegalStateException("Test")),
                throwable -> {
                    assertNotNull(throwable);
                    assertTrue(throwable instanceof IllegalStateException);
                    assertEquals("Test", throwable.getMessage());
                });
    }

    @Test
    public void enhancedFinallyWithoutException() {
        EhCoreSupport.enhancedFinally(() -> {
        }, Assert::assertNull);
    }

    @Test(expected = IllegalStateException.class)
    public void enhancedFinallyFnWithException() {
        final Long longValue = EhCoreSupport.enhancedFinallyFn(() -> {
            EhCoreSupport.rethrow(new IllegalStateException("Test"));
            return 42L;
        }, throwable -> {
            assertNotNull(throwable);
            assertTrue(throwable instanceof IllegalStateException);
            assertEquals("Test", throwable.getMessage());
        });
    }

    @Test
    public void enhancedFinallyFnWithoutException() {
        final Long longValue = EhCoreSupport.enhancedFinallyFn(() -> 42L,
                Assert::assertNull);
        assertEquals((Long) 42L, longValue);
    }

    @Test
    public void trySuppressExceptionsSuppressed() {
        final Throwable throwable = new IllegalArgumentException("Main exception");
        EhCoreSupport.trySuppressExceptions(throwable, () -> EhCoreSupport.rethrow(new IllegalStateException("Test")));
        final Throwable[] suppressed = throwable.getSuppressed();
        assertEquals(1, suppressed.length);
        assertTrue(suppressed[0] instanceof IllegalStateException);
        assertEquals("Test", suppressed[0].getMessage());
    }

    @Test
    public void trySuppressExceptionsNoException() {
        final Throwable throwable = new IllegalArgumentException("Main exception");
        EhCoreSupport.trySuppressExceptions(throwable, () -> {
        });
        assertEquals(0, throwable.getSuppressed().length);
    }

    @Test(expected = IllegalStateException.class)
    public void trySuppressExceptionsNotSuppressedNullThrowable() {
        EhCoreSupport.trySuppressExceptions(
                null /* throwable */,
                () -> EhCoreSupport.rethrow(new IllegalStateException("Test")));
    }

    @Test
    public void trySuppressExceptionsNotSuppressedNotEnabled() {
        /*
         * If the exception suppression is not enabled then the exception will be simply ignored
         */
        final Throwable throwable = new SuppressNotEnabledException("Main exception");
        EhCoreSupport.trySuppressExceptions(
                throwable,
                () -> EhCoreSupport.rethrow(new IllegalStateException("Test")));
        assertEquals(0, throwable.getSuppressed().length);
    }

    @Test
    public void suppressExceptionsOrFatalNoException() {
        final Throwable throwable = new IllegalArgumentException("Main exception");
        EhCoreSupport.suppressExceptionsOrFatal(throwable, () -> {
        });
        assertEquals(0, throwable.getSuppressed().length);
    }

    @Test(expected = FatalApplicationError.class)
    public void suppressExceptionsOrFatalNotSuppressedNullThrowable() {
        EhCoreSupport.suppressExceptionsOrFatal(
                null /* throwable */,
                () -> EhCoreSupport.rethrow(new IllegalStateException("Test")));
    }

    @Test
    public void tryCloseQuietlyNullClosable() {
        final Throwable throwable = new IllegalArgumentException("Main exception");
        EhCoreSupport.tryCloseQuietly(
                throwable, null /* closeable */, Closeable::close);
        assertEquals(0, throwable.getSuppressed().length);
    }

    @Test
    public void tryCloseQuietlyNormalClose() {
        final MutableObject<Boolean> isClosed = new MutableObject<>(Boolean.FALSE);
        final Closeable closeable = () -> isClosed.setValue(Boolean.TRUE);
        final Throwable throwable = new IllegalArgumentException("Main exception");
        EhCoreSupport.tryCloseQuietly(throwable, closeable, Closeable::close);
        assertEquals(0, throwable.getSuppressed().length);
        assertEquals(Boolean.TRUE, isClosed.getValue());
    }

    @Test
    public void tryCloseQuietlyCloseThrowsException() {
        final MutableObject<Boolean> isCloseAttempted = new MutableObject<>(Boolean.FALSE);
        final Closeable closeable = () -> {
            isCloseAttempted.setValue(Boolean.TRUE);
            EhCoreSupport.rethrow(new IllegalStateException("Test"));
        };
        final Throwable throwable = new IllegalArgumentException("Main exception");
        EhCoreSupport.tryCloseQuietly(throwable, closeable, Closeable::close);
        final Throwable[] suppressed = throwable.getSuppressed();
        assertEquals(1, suppressed.length);
        assertTrue(suppressed[0] instanceof IllegalStateException);
        assertEquals("Test", suppressed[0].getMessage());
        assertEquals(Boolean.TRUE, isCloseAttempted.getValue());
    }

    @Test
    public void closeQuietlyOrFatalNormalClose() {
        final MutableObject<Boolean> isClosed = new MutableObject<>(Boolean.FALSE);
        final Closeable closeable = () -> isClosed.setValue(Boolean.TRUE);
        EhCoreSupport.closeQuietlyOrFatal(
                null /* throwable */,
                closeable,
                Closeable::close);
        assertEquals(Boolean.TRUE, isClosed.getValue());
    }

    @Test(expected = FatalApplicationError.class)
    public void closeQuietlyOrFatalThrowsException() {
        final MutableObject<Boolean> isCloseAttempted = new MutableObject<>(Boolean.FALSE);
        final Closeable closeable = () -> {
            isCloseAttempted.setValue(Boolean.TRUE);
            EhCoreSupport.rethrow(new IllegalStateException("Test"));
        };
        EhCoreSupport.closeQuietlyOrFatal(
                null /* throwable */,
                closeable,
                Closeable::close);
    }

    @Test
    public void tryCloseQuietlyClosableNormalClose() {
        final MutableObject<Boolean> isClosed = new MutableObject<>(Boolean.FALSE);
        final Closeable closeable = () -> isClosed.setValue(Boolean.TRUE);
        final Throwable throwable = new IllegalArgumentException("Main exception");
        EhCoreSupport.tryCloseQuietly(throwable, closeable);
        assertEquals(0, throwable.getSuppressed().length);
        assertEquals(Boolean.TRUE, isClosed.getValue());
    }

    @Test
    public void tryCloseQuietlyClosableCloseThrowsException() {
        final MutableObject<Boolean> isCloseAttempted = new MutableObject<>(Boolean.FALSE);
        final Closeable closeable = () -> {
            isCloseAttempted.setValue(Boolean.TRUE);
            EhCoreSupport.rethrow(new IllegalStateException("Test"));
        };
        final Throwable throwable = new IllegalArgumentException("Main exception");
        EhCoreSupport.tryCloseQuietly(throwable, closeable);
        final Throwable[] suppressed = throwable.getSuppressed();
        assertEquals(1, suppressed.length);
        assertTrue(suppressed[0] instanceof IllegalStateException);
        assertEquals("Test", suppressed[0].getMessage());
        assertEquals(Boolean.TRUE, isCloseAttempted.getValue());
    }

    @Test
    public void closeQuietlyOrFatalCloseableNormalClose() {
        final MutableObject<Boolean> isClosed = new MutableObject<>(Boolean.FALSE);
        final Closeable closeable = () -> isClosed.setValue(Boolean.TRUE);
        EhCoreSupport.closeQuietlyOrException(null /* throwable */, closeable);
        assertEquals(Boolean.TRUE, isClosed.getValue());
    }

    @Test
    public void closeQuietlyOrFatalCloseableWithNullCloseable() {
        EhCoreSupport.closeQuietlyOrException(null, null);
    }

    @Test(expected = FatalApplicationError.class)
    public void closeQuietlyOrFatalCloseableThrowsException() {
        final MutableObject<Boolean> isCloseAttempted = new MutableObject<>(Boolean.FALSE);
        final Closeable closeable = () -> {
            isCloseAttempted.setValue(Boolean.TRUE);
            EhCoreSupport.rethrow(new IllegalStateException("Test"));
        };
        EhCoreSupport.closeQuietlyOrException(null /* throwable */, closeable);
    }

    public class SuppressNotEnabledException extends RuntimeException {
        SuppressNotEnabledException(final String message) {
            super(message, null /* cause */, false /* enableSuppression */, true /* writableStackTrace */);
        }
    }

    @Test
    public void uncaughtExceptionHandler() {
        try {
            final Thread.UncaughtExceptionHandler defaultUncaughtHandler = EhCoreSupport.getDefaultUncaughtHandler();
            assertNotNull(defaultUncaughtHandler);

            final Thread.UncaughtExceptionHandler uncaughtHandler = EhCoreSupport.getUncaughtHandler();
            assertNotNull(uncaughtHandler);

            assertSame(defaultUncaughtHandler, uncaughtHandler);

            final MutableObject<Boolean> invoked = new MutableObject<>(Boolean.FALSE);
            final String message = "Test uncaught handler exception message";

            final Thread.UncaughtExceptionHandler newHandler = (thread, exception) -> {
                assertSame(exception.getClass(), IllegalStateException.class);
                assertSame(message, exception.getMessage());
                invoked.setValue(Boolean.TRUE);
            };

            EhCoreSupport.setUncaughtHandler(newHandler);
            assertSame(newHandler, EhCoreSupport.getUncaughtHandler());

            EhCoreSupport.propagate(() -> {
                final Thread thread = new Thread(() -> EhSupport.throwEnhanced(new IllegalStateException(message)));
                thread.start();
                thread.join();
            });

            assertEquals(Boolean.TRUE, invoked.getValue());

            assertSame(newHandler, EhCoreSupport.getUncaughtHandler());
            EhCoreSupport.setUncaughtHandler(defaultUncaughtHandler);
            assertSame(defaultUncaughtHandler, EhCoreSupport.getUncaughtHandler());
            EhCoreSupport.setUncaughtHandler(null);
            assertSame(defaultUncaughtHandler, EhCoreSupport.getUncaughtHandler());
        } finally {
            /**
             * Make sure to restore the default handler no matter what happens
             */
            EhCoreSupport.setUncaughtHandler(null);
        }
    }
}
