package lt.pskurimas.ptvs.validation;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import lt.pskurimas.ptvs.model.ThirdPartyService;

@Component
public class ServiceMonthlyCostValidator implements ThirdPartyServiceValidator {

    @Override
    public void validate(ThirdPartyService service, ServiceValidationContext context) {
        BigDecimal cost = service.getMonthlyCost();
        if (cost == null || cost.signum() <= 0) {
            throw new ServiceValidationException(ServiceValidationError.MONTHLY_COST_REQUIRED);
        }
    }
}
