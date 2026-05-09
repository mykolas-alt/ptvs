package lt.pskurimas.ptvs.dto.response;

import java.util.List;

public record UserInfoResponse(String username, List<String> roles) {
}
