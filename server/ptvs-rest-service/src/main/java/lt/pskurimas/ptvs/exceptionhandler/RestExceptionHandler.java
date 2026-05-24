package lt.pskurimas.ptvs.exceptionhandler;

import lt.pskurimas.ptvs.dto.response.ErrorResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
        String message = ex.getMessage() == null ? "Optimistic lock conflict" : ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(message));
    }
}