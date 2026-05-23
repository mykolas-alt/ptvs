package lt.pskurimas.ptvs.validation;

import lt.pskurimas.ptvs.model.ThirdPartyService;

public interface ThirdPartyServiceValidator {
    void validate(ThirdPartyService service, ServiceValidationContext context);
}
