package lt.pskurimas.ptvs.validation;

import lt.pskurimas.ptvs.model.ThirdPartyService;
import org.springframework.stereotype.Component;

@Component
public class ServiceMustHaveVendorValidator implements ThirdPartyServiceValidator {

    @Override
    public void validate(ThirdPartyService service, ServiceValidationContext context) {
        if (service.getVendorContact() == null) {
            throw new ServiceValidationException(ServiceValidationError.SERVICE_MUST_HAVE_VALID_VENDOR_CONTACT);
        }
    }
}
