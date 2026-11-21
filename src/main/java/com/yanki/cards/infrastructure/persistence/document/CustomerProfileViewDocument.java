package com.yanki.cards.infrastructure.persistence.document;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento MongoDB para la coleccion de vistas de perfil de cliente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customer_profile_view")
public class CustomerProfileViewDocument {

    @Id
    private String customerId;

    private String customerType;
    private String profile;
    private String status;
    private LocalDateTime updatedAt;
}
