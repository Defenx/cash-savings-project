package com.kavencore.moneyharbor.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "operations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    private Account account;

    @ToString.Include
    @Column(nullable = false)
    @NotNull
    private LocalDate date;

    @CreationTimestamp
    @Column(nullable = false)
    @NotNull
    private OffsetDateTime createdDate;

    private String description;

    @ToString.Include
    @Column(nullable = false)
    @NotNull
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @ToString.Include
    @Column(nullable = false)
    @NotNull
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    private Category category;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Operation operation = (Operation) o;
        return getId() != null && Objects.equals(getId(), operation.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}