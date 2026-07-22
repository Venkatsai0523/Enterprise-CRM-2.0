package com.crm.identity.mapper;

import com.crm.identity.api.dto.UserResponseDto;
import com.crm.identity.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-22T19:30:59+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
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
        userResponseDto.id( user.getId() );
        userResponseDto.email( user.getEmail() );
        userResponseDto.firstName( user.getFirstName() );
        userResponseDto.lastName( user.getLastName() );
        userResponseDto.enabled( user.isEnabled() );
        userResponseDto.createdAt( user.getCreatedAt() );

        return userResponseDto.build();
    }
}
