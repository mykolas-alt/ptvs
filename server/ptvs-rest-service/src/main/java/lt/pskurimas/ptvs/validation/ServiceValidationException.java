package lt.pskurimas.ptvs.validation;

import lombok.Getter;

@Getter
public class ServiceValidationException extends IllegalArgumentException {

    private final ServiceValidationError error;

    public ServiceValidationException(ServiceValidationError error) {
        super(error.getMessage());
        this.error = error;
    }
}
