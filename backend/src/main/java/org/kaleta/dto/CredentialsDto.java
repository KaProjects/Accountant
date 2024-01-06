package org.kaleta.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Data
public class CredentialsDto
{
    private String username;
    private String password;

    public void validate(){
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username can't be null");
        }
        if (password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password can't be null");
        }
    }

    public static CredentialsDto from(String username, String password)
    {
        CredentialsDto dto = new CredentialsDto();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }
}
