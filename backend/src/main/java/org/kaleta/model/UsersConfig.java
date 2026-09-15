package org.kaleta.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@RegisterForReflection
public class UsersConfig
{
    private List<User> users = new ArrayList<>();

    @Data
    @RegisterForReflection
    public static class User
    {
        private String username;
        private String hash;
    }
}
