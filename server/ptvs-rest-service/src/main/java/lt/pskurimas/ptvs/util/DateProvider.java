package lt.pskurimas.ptvs.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class DateProvider {

    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
}
