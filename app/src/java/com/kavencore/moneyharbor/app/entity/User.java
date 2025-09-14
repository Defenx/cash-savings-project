package com.kavencore.moneyharbor.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    private UUID id;

    @ToString.Include
    private String email;

    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private Set<Account> accounts = new HashSet<>();

    @CreationTimestamp
    private OffsetDateTime createdDate;

    @UpdateTimestamp
    private OffsetDateTime updatedDate;


    @PrePersist
    @PreUpdate
    void normalize() {
        if (email != null) email = email.toLowerCase(Locale.ROOT);
    }

    public void addRole(Role role) {
        if (role == null) return;
        if (this.roles.add(role)) {
            role.getUsers().add(this);
        }
    }

    public void removeRole(Role role) {
        if (role == null) return;
        if (this.roles.remove(role)) {
            role.getUsers().remove(this);
        }
    }

    public void addAccount(Account account) {
        if (account == null) return;
        if (!this.equals(account.getUser())) {
            this.accounts.add(account);
            account.setUser(this);
        }
    }

    public void removeAccount(Account account) {
        if (account == null) return;
        if (this.accounts.remove(account)) {
            if (account.getUser() == this) {
                account.setUser(null);
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
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
