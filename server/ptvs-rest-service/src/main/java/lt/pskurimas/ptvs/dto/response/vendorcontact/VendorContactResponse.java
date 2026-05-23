package lt.pskurimas.ptvs.dto.response.vendorcontact;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorContactResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String vendorName;
    private String department;
    private Long version;
}
