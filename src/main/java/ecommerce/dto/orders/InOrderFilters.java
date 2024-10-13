package ecommerce.dto.orders;

import org.springframework.lang.Nullable;

import ecommerce.dto.validation.nullablenotblank.NullableNotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InOrderFilters {
    @Nullable 
    private Boolean completed;

    @Nullable
    @NullableNotBlank
    private String username;
}
