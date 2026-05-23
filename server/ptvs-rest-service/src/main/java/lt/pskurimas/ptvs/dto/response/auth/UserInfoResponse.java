package lt.pskurimas.ptvs.dto.response.auth;

import java.util.List;

public record UserInfoResponse(String username, List<String> roles, Long version) {
}
