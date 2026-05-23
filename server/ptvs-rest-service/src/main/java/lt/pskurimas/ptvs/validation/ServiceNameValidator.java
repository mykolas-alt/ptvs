package lt.pskurimas.ptvs.validation;

import org.springframework.stereotype.Component;

import lt.pskurimas.ptvs.model.ThirdPartyService;

@Component
public class ServiceNameValidator implements ThirdPartyServiceValidator {

    @Override
    public void validate(ThirdPartyService service, ServiceValidationContext context) {
        String name = service.getServiceName();
        if (name == null || name.trim().isEmpty()) {
            throw new ServiceValidationException(ServiceValidationError.SERVICE_NAME_REQUIRED);
        }
    }
}
