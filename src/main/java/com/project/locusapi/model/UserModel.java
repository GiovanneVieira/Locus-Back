package com.project.locusapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.locusapi.constant.AuthProvider;
import com.project.locusapi.constant.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity(name = "user_table")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserModel implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "role", updatable = true)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "pfpUrl", columnDefinition = "TEXT", updatable = true)
    private String pfpUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<RefreshToken> refreshToken = new ArrayList<>();

    @Column(name = "auth_provider", updatable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "personal_address_id", referencedColumnName = "address_id")
    @JsonBackReference
    private PersonalAddressModel personalAddress;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    @JsonBackReference
    private List<RentableAddressModel> rentableAddress = new ArrayList<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == Role.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority(Role.ADMIN.getRoleString()),
                    new SimpleGrantedAuthority(Role.HOST.getRoleString()),
                    new SimpleGrantedAuthority(Role.USER.getRoleString())
            );
        }
        if (this.role == Role.HOST) {
            return List.of(
                    new SimpleGrantedAuthority(Role.HOST.getRoleString()),
                    new SimpleGrantedAuthority(Role.USER.getRoleString())
            );
        }
        return List.of(new SimpleGrantedAuthority(Role.USER.getRoleString()));
    }

    public void addRefreshToken(RefreshToken token) {
        if (this.refreshToken == null) {
            this.refreshToken = new ArrayList<>();
        }
        this.refreshToken.add(token);

        token.setUser(this);
    }

    public void addRentableAddress(RentableAddressModel rentableAddress) {
        if (this.rentableAddress == null) {
            this.rentableAddress = new ArrayList<>();
        }
        this.rentableAddress.add(rentableAddress);

        rentableAddress.setUser(this);
    }


    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

}
