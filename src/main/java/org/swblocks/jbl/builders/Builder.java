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

package org.swblocks.jbl.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Generic Builder class to define a creator and build operations to apply to the created object.
 * Then a {@link Predicate} validator to validate buildFn function before constructing the result.
 *
 * <p>The class is typed with two generic types.
 * {@code B} the domain buildFn class.
 * {@code R} the domain results class generated from the domain buildFn.
 */
public final class Builder<B, R> {
    private final Supplier<B> creator;
    private final Function<B, R> buildFn;
    private final Predicate<B> validator;
    private final List<Consumer<B>> instanceModifiers = new ArrayList<>(1);

    private Builder(final Supplier<B> creator, final Predicate<B> validator, final Function<B, R> buildFn) {
        this.creator = creator;
        this.validator = validator;
        this.buildFn = buildFn;
    }

    /**
     * Creates an instance of the Builder given creater and buildFn functions.  Validation defaults to always true.
     *
     * @param creator {@code Supplier} to generate the domain buildFn class.
     * @param builder {@code Function} to generate the domain results class from the domain buildFn class.
     * @param <B>     domain buildFn class
     * @param <R>     domain results class
     * @return Created Builder object
     */
    public static <B, R> Builder<B, R> instanceOf(final Supplier<B> creator, final Function<B, R> builder) {
        return new Builder<>(creator, predicate -> true, builder);
    }

    /**
     * Creates an instance of the Builder given creater, validator and buildFn functions.
     *
     * @param creator   {@code Supplier} to generate the domain buildFn class.
     * @param validator {@code Predicate} to validate the domain buildFn class.
     * @param builder   {@code Function} to generate the domain results class from the domain buildFn class.
     * @param <B>       domain buildFn class
     * @param <R>       domain results class
     * @return Created Builder object
     */
    public static <B, R> Builder<B, R> instanceOf(final Supplier<B> creator, final Predicate<B> validator,
                                                  final Function<B, R> builder) {
        return new Builder<>(creator, validator, builder);
    }

    /**
     * Defines an operation and value to be applied to the domain buildFn class.
     *
     * @param consumer consumer method
     * @param value    Value to be applied to the consumer
     * @param <U>      Type of value
     * @return this class for chaining methods.
     */
    public <U> Builder<B, R> with(final BiConsumer<B, U> consumer, final U value) {
        final Consumer<B> c = instance -> consumer.accept(instance, value);
        this.instanceModifiers.add(c);
        return this;
    }

    /**
     * Creates the domain buildFn, applies all the operations defined and then generates the domain results class.
     *
     * @return domain results class built from the domain buildFn class
     */
    public R build() {
        final B objBuilder = this.creator.get();
        this.instanceModifiers.forEach(modifier -> modifier.accept(objBuilder));
        this.instanceModifiers.clear();
        if (this.validator.test(objBuilder)) {
            return this.buildFn.apply(objBuilder);
        }
        return null;
    }
}
