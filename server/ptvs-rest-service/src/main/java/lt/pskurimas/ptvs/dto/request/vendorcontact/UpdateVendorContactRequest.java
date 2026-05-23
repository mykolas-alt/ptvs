package lt.pskurimas.ptvs.dto.request.vendorcontact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lt.pskurimas.ptvs.dto.request.VersionedUpdateRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVendorContactRequest implements VersionedUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String vendorName;
    private String department;
    private Long version;
    private boolean forceUpdate;
}
