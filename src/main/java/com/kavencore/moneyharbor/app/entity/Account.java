package com.kavencore.moneyharbor.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @ToString.Include
    private String title;

    @Enumerated(EnumType.STRING)
    @ToString.Include
    private Currency currency;

    @ToString.Include
    private BigDecimal amount;
}
