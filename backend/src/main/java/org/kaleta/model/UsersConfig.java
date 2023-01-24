package org.kaleta.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UsersConfig
{
    private List<User> users = new ArrayList<>();

    @Data
    public static class User
    {
        private String username;
        private String hash;
    }
}
