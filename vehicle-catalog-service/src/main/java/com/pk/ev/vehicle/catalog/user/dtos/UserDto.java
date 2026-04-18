package com.pk.ev.vehicle.catalog.user.dtos;

import com.pk.ev.vehicle.catalog.user.enums.EvUserRole;
import com.pk.ev.vehicle.catalog.user.model.EvUser;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String password;
    private List<EvUserRole> roles;

    public static UserDto fromEvUser(EvUser evUser) {
        return UserDto.builder()
                .id(evUser.getId())
                .email(evUser.getEmail())
                .roles(evUser.getRoles())
                .build();
    }

    public static EvUser toEvUser(UserDto userDto) {
        return EvUser.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .roles(userDto.getRoles())
                .build();
    }
}
