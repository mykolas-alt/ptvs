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
    private final EmailBuildingService emailBuildingService;

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

            helper.setTo(employeeEmail);

            if (additionalEmails != null && !additionalEmails.isEmpty()) {
                helper.setCc(additionalEmails.toArray(new String[0]));
            }

            helper.setSubject(emailBuildingService.buildSubject());

            helper.setText(
                    emailBuildingService.buildHtmlBody(
                            serviceName,
                            vendorName,
                            expirationDate,
                            daysRemaining
                    ),
                    true
            );

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

}