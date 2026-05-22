package lt.pskurimas.ptvs.validation;

import lombok.Getter;

@Getter
public enum ServiceValidationError {
    SERVICE_NAME_REQUIRED("Service name is required"),
    SERVICE_NAME_NOT_UNIQUE("Service name must be unique"),
    MONTHLY_COST_REQUIRED("Monthly cost must be greater than 0"),
    CONTRACT_START_DATE_REQUIRED("Contract start date is required"),
    CONTRACT_END_DATE_REQUIRED("Contract end date is required"),
    CONTRACT_END_BEFORE_START("Contract end date must be on or after start date"),
    MANUAL_DEACTIVATION_AFTER_CONTRACT_END("Manual deactivation date cannot be after contract end date"),
    MANUALLY_DEACTIVATED_SERVICE_MAY_NOT_BE_UPDATED("A service that has been manually deactivated may not be updated"),
    SERVICE_MUST_HAVE_VALID_RESPONSIBLE_PERSONNEL("A service must have at least one valid employee assigned"),
    SERVICE_CREATOR_MISSING("Service creator is not present"),
    SERVICE_MUST_HAVE_VALID_VENDOR_CONTACT("A service must have a valid vendor contact assigned"),
    SERVICE_MUST_BE_ACTIVE_OR_PENDING_FOR_DEACTIVATION("Service must be ACTIVE or PENDING to deactivate");

    private final String message;

    ServiceValidationError(String message) {
        this.message = message;
    }
}
