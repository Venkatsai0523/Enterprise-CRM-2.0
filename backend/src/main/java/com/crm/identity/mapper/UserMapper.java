package com.crm.identity.mapper;

import com.crm.identity.api.dto.UserResponseDto;
import com.crm.identity.entity.Role;
import com.crm.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@SuppressWarnings("null")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleNames")
    UserResponseDto toDto(User user);

    @Named("rolesToRoleNames")
    default Set<String> rolesToRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
