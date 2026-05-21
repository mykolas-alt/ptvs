package lt.pskurimas.ptvs.validation;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import lt.pskurimas.ptvs.model.ThirdPartyService;

@Component
public class ServiceContractDatesValidator implements ThirdPartyServiceValidator {

    @Override
    public void validate(ThirdPartyService service, ServiceValidationContext context) {
        LocalDate startDate = service.getContractStartDate();
        LocalDate endDate = service.getContractEndDate();

        if (startDate == null) {
            throw new ServiceValidationException(ServiceValidationError.CONTRACT_START_DATE_REQUIRED);
        }
        if (endDate == null) {
            throw new ServiceValidationException(ServiceValidationError.CONTRACT_END_DATE_REQUIRED);
        }
        if (endDate.isBefore(startDate)) {
            throw new ServiceValidationException(ServiceValidationError.CONTRACT_END_BEFORE_START);
        }
    }
}
