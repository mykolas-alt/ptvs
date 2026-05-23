package lt.pskurimas.ptvs.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterRequest(String username,
                              @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password) {
}
