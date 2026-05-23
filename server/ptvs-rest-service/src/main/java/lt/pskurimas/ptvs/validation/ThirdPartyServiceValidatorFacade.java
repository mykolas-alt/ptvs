package lt.pskurimas.ptvs.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import lt.pskurimas.ptvs.model.ThirdPartyService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThirdPartyServiceValidatorFacade {

    private final List<ThirdPartyServiceValidator> validators;

    public void validate(ThirdPartyService service, ServiceValidationContext context) {
        for (ThirdPartyServiceValidator validator : validators) {
            validator.validate(service, context);
        }
    }
}
