package ecommerce.service;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ecommerce.dto.orders.InOrderProduct;
import ecommerce.service.utils.CollectionUtils;

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
            new InOrderProduct(1, 10),
            new InOrderProduct(2, 20),
            new InOrderProduct(3, 30)
        );

        final var result = CollectionUtils.containsDuplicates(collection, InOrderProduct::productId);

        Assertions.assertFalse(result, "list does not contain duplicates");
    }

    @Test
    public void containsDuplicates_identityFn_duplicates() {
        final var collection = List.of(
            new InOrderProduct(1, 10),
            new InOrderProduct(1, 20),
            new InOrderProduct(3, 30)
        );

        final var result = CollectionUtils.containsDuplicates(collection, InOrderProduct::productId);

        Assertions.assertTrue(result, "list does contain duplicates");
    }
}
