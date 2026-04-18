package com.pk.ev.vehicle.catalog.user.model;

import com.pk.ev.vehicle.catalog.user.enums.EvUserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private String firstName;

    private String middleName;

    private String lastName;

    private String fullName;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private List<EvUserRole> roles = new ArrayList<>(List.of(EvUserRole.ROLE_EV_USER));


}
