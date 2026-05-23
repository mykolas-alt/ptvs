package lt.pskurimas.ptvs.dto.request.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lt.pskurimas.ptvs.dto.request.VersionedUpdateRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest implements VersionedUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String department;
    private String jobTitle;
    private Long version;
    private boolean forceUpdate;
}
