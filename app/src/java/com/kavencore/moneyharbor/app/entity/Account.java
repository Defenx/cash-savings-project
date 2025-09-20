package com.kavencore.moneyharbor.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @ToString.Include
    private String title;

    @Enumerated(EnumType.STRING)
    @ToString.Include
    private Currency currency;

    @ToString.Include
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @OneToMany(mappedBy = "account")
    @Builder.Default
    private List<Operation> operations = new ArrayList<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Account account = (Account) o;
        return getId() != null && Objects.equals(getId(), account.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    public void addOperation(Operation operation) {
        if (operation == null) return;
        if (!this.equals(operation.getAccount())) {
            this.operations.add(operation);
            operation.setAccount(this);
        }
    }
}
