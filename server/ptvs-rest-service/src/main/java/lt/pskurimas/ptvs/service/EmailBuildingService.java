package lt.pskurimas.ptvs.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class EmailBuildingService {
    
    public String buildSubject() {
        return "Service Expiration Reminder";
    }

    public String buildHtmlBody(
            String serviceName,
            String vendorName,
            LocalDate expirationDate,
            Integer daysRemaining
    ) {

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Service Expiration Reminder</h2>

                    <p>
                        Your service contract for %s is approaching expiration.
                    </p>

                    <table style="border-collapse: collapse;">
                        <tr>
                            <td><strong>Vendor:</strong></td>
                            <td>%s</td>
                        </tr>

                        <tr>
                            <td><strong>Expiration Date:</strong></td>
                            <td>%s</td>
                        </tr>

                        <tr>
                            <td><strong>Days Remaining:</strong></td>
                            <td>%d</td>
                        </tr>
                    </table>

                    <br>

                    <p>
                        Please review and renew the service if necessary.
                    </p>

                    <br>

                    <p>
                        Regards,<br>
                        PTVS System
                    </p>
                </body>
                </html>
                """
                .formatted(
                        serviceName,
                        vendorName,
                        expirationDate,
                        daysRemaining
                );
    }

}
