package lt.pskurimas.ptvs.validation;

import java.time.LocalDate;

public record ServiceValidationContext(ServiceValidationOperation operation, LocalDate currentDate) {
}
