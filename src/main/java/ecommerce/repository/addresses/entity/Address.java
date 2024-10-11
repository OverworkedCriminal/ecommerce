package ecommerce.repository.addresses.entity;

import ecommerce.repository.countries.entity.Country;
import ecommerce.repository.orders.entity.Order;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Nonnull
    private String street;
    @Nonnull
    private String house;

    @Nonnull
    private String postalCode;
    @Nonnull
    private String city;

    @ManyToOne
    @JoinColumn(
        name = "country_id",
        nullable = false
    )
    private Country country;

    @OneToOne(mappedBy = "address")
    private Order order;
}
