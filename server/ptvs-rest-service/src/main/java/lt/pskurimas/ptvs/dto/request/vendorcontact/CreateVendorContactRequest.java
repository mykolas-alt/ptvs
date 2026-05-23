package lt.pskurimas.ptvs.dto.request.vendorcontact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateVendorContactRequest {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String vendorName;
    private String department;
}
