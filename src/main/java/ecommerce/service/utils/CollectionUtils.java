package ecommerce.service.utils;

import java.util.Collection;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionUtils {

    /**
     * Checks for duplicates in the collection
     * 
     * @param <T>
     * @param collection
     * @return true if collection contains duplicates
     */
    public static <T> boolean containsDuplicates(Collection<T> collection) {
        return containsDuplicates(collection, obj -> obj);
    }

    /**
     * Checks for duplicates in the collection.
     * Allows to map objects before comparisons.
     * 
     * @param <T>
     * @param <U>
     * @param collection
     * @param identityFn
     * @return true if collection contains duplicates
     */
    public static <T, U> boolean containsDuplicates(
        Collection<T> collection,
        Function<T, U> identityFn
    ) {
        final var distinctCount = collection.stream()
            .map(identityFn)
            .distinct()
            .count();

        final var containsDuplicates = distinctCount != collection.size();

        return containsDuplicates;
    }

}
