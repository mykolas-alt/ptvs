package lt.pskurimas.ptvs.dto.response.auth;

import java.util.List;
import java.util.UUID;

public record UserInfoResponse(String username, UUID userId, List<String> roles, Long version) {
}
