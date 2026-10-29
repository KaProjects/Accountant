package org.kaleta.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@RegisterForReflection
public class CredentialsDto
{
    @NotNull
    private String username;
    @NotNull
    private String password;

    public static CredentialsDto from(String username, String password)
    {
        CredentialsDto dto = new CredentialsDto();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }
}
