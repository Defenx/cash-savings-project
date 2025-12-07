package com.kavencore.moneyharbor.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    private UUID id;

    @ToString.Include
    private LocalDate date;

    @ToString.Include
    @CreationTimestamp
    private OffsetDateTime createdDate;

    @ToString.Include
    private String description;

    @OneToMany(mappedBy = "transaction")
    @Builder.Default
    List<Entry> entries = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    public void addEntry(Entry entry) {
        if (entry == null) return;
        if (!this.equals(entry.getTransaction())) {
            this.entries.add(entry);
            entry.setTransaction(this);
        }
    }

    public void removeEntry(Entry entry) {
        if (entry == null) return;
        if (this.entries.remove(entry)) {
            if (entry.getTransaction() == this) {
                entry.setTransaction(null);
            }
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Transaction transaction = (Transaction) o;
        return getId() != null && Objects.equals(getId(), transaction.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
