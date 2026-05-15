package lt.pskurimas.ptvs.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service 
@RequiredArgsConstructor
public class EmailDispatchService {

    private final JavaMailSender mailSender;

    public void sendExpirationNotification(
            String employeeEmail,
            String serviceName,
            String vendorName,
            LocalDate expirationDate,
            Integer daysRemaining,
            List<String> additionalEmails
    ) {

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Main recipient
            helper.setTo(employeeEmail);

            if (additionalEmails != null && !additionalEmails.isEmpty()) {
                helper.setCc(additionalEmails.toArray(new String[0]));
            }

            helper.setSubject("Service Expiration Reminder");

            String htmlBody = buildHtmlTemplate(
                    serviceName,
                    vendorName,
                    expirationDate,
                    daysRemaining
            );

            helper.setText(htmlBody, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildHtmlTemplate(
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