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

import java.util.function.Supplier;

/**
 * Result class used to store the outcome of an operation.
 *
 * <p>The outcome of an operation is stored as a success or failure result and does not terminate the processing. The
 * result can be used to determine what to do next.
 *
 * <p>Example usage:
 * <blockquote><pre>
 * public boolean carryOutTask() {
 *     Result result = doProcessing();
 *     if (result.isSuccess()) {
 *         return true;
 *     }
 *     return false;
 * }
 *
 *  private Result doProcessing() {
 *      try {
 *          callAnOperation();
 *          return Result.success("my success data");
 *      } catch (Exception exception) {
 *          return Result.failure(exception);
 *      }
 *  }
 * </pre></blockquote>
 */
public final class Result<T> {
    private final boolean isSuccess;
    private final T data;
    private final Supplier<Exception> exceptionSupplier;
    private Exception exception;

    private Result(final boolean isSuccess, final T data, final Supplier<Exception> exceptionSupplier) {
        this.isSuccess = isSuccess;
        this.data = data;
        this.exceptionSupplier = exceptionSupplier;
        this.exception = null;
    }

    /**
     * Create a success Result object.
     *
     * @param data the success data
     * @param <T>  the object type
     * @return the result object
     */
    public static <T> Result<T> success(final T data) {
        return new Result<>(true, data, null);
    }

    /**
     * Create a failure Result object from an exception supplier.
     *
     * @param exceptionSupplier the supplier
     * @param <T>               the object type
     * @return the result object
     */
    public static <T> Result<T> failure(final Supplier<Exception> exceptionSupplier) {
        return new Result<>(false, null, exceptionSupplier);
    }

    /**
     * Check if a success result.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return this.isSuccess;
    }

    /**
     * Returns underlying data stored when the result is a success.
     *
     * @return the success data
     */
    public T getData() {
        EhSupport.ensureOrFatal(this.isSuccess(), "Result.getData is called on failed operation");
        return this.data;
    }

    /**
     * Get the failure exception.
     *
     * @return the exception
     */
    public Exception getException() {
        EhSupport.ensureOrFatal(!this.isSuccess(), "Result.getException is called on successful operation");

        if (this.exception == null && this.exceptionSupplier != null) {
            this.exception = this.exceptionSupplier.get();
        }
        EhSupport.ensureOrFatal(this.exception != null, "Result.getException - exception not provided");

        return this.exception;
    }
}