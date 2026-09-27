package com.shaker.entity.user;

import com.shaker.entity.common.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Document("users")
public class User extends BaseEntity {

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private String displayName;

    private String bio;

    private Set<Role> roles = new LinkedHashSet<>();

    private UserPreferences preferences = new UserPreferences();
}
