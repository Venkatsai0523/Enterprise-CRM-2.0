package com.crm.identity.mapper;

import com.crm.identity.api.dto.UserResponseDto;
import com.crm.identity.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T00:13:00+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponseDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponseDto.UserResponseDtoBuilder userResponseDto = UserResponseDto.builder();

        userResponseDto.roles( rolesToRoleNames( user.getRoles() ) );
        userResponseDto.createdAt( user.getCreatedAt() );
        userResponseDto.email( user.getEmail() );
        userResponseDto.enabled( user.isEnabled() );
        userResponseDto.firstName( user.getFirstName() );
        userResponseDto.id( user.getId() );
        userResponseDto.lastName( user.getLastName() );

        return userResponseDto.build();
    }
}
