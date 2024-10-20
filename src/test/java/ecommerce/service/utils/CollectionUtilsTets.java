package ecommerce.service.utils;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ecommerce.dto.orders.InOrderProduct;

public class CollectionUtilsTets {
    @Test
    public void containsDuplicates_noDuplicates() {
        final var collection = List.of(1, 2, 3);

        final var result = CollectionUtils.containsDuplicates(collection);

        Assertions.assertFalse(result, "list does not contain duplicates");
    }

    @Test
    public void containsDuplicates_dulpicates() {
        final var collection = List.of(1, 2, 1);

        final var result = CollectionUtils.containsDuplicates(collection);

        Assertions.assertTrue(result, "list does contain duplicates");
    }

    @Test
    public void containsDuplicates_identityFn_noDuplicates() {
        final var collection = List.of(
            new InOrderProduct(1L, 10),
            new InOrderProduct(2L, 20),
            new InOrderProduct(3L, 30)
        );

        final var result = CollectionUtils.containsDuplicates(collection, InOrderProduct::productId);

        Assertions.assertFalse(result, "list does not contain duplicates");
    }

    @Test
    public void containsDuplicates_identityFn_duplicates() {
        final var collection = List.of(
            new InOrderProduct(1L, 10),
            new InOrderProduct(1L, 20),
            new InOrderProduct(3L, 30)
        );

        final var result = CollectionUtils.containsDuplicates(collection, InOrderProduct::productId);

        Assertions.assertTrue(result, "list does contain duplicates");
    }
}
