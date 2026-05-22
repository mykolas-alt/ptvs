package lt.pskurimas.ptvs.util;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class DateProvider {

    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }
}
