package lt.pskurimas.ptvs.validation;

import lt.pskurimas.ptvs.model.ThirdPartyService;
import org.springframework.stereotype.Component;

@Component
public class ServiceCreatorValidator implements ThirdPartyServiceValidator {

    @Override
    public void validate(ThirdPartyService service, ServiceValidationContext context) {
        if (context.operation() == ServiceValidationOperation.UPDATE) {
            return;
        }
        if (service.getCreatedBy() == null) {
            throw new ServiceValidationException(ServiceValidationError.SERVICE_CREATOR_MISSING);
        }
    }
}
