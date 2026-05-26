package lt.pskurimas.ptvs.service.handler;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.model.EmployeeNotificationAdditionalEmail;
import lt.pskurimas.ptvs.model.EmployeeNotificationConfig;
import lt.pskurimas.ptvs.model.ThirdPartyService;
import lt.pskurimas.ptvs.service.EmailTemplateProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;
    private final EmailTemplateProvider emailTemplateProvider;

    @Override
    public void sendNotification(EmployeeNotificationConfig config) {
        ThirdPartyService service = config.getServiceNotificationConfig().getService();

        List<String> additionalEmails = config.getAdditionalEmails().stream()
                .map(EmployeeNotificationAdditionalEmail::getEmail)
                .toList();

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(config.getEmployee().getEmail());

            if (!additionalEmails.isEmpty()) {
                helper.setCc(additionalEmails.toArray(new String[0]));
            }

            helper.setSubject(emailTemplateProvider.getSubject());

            helper.setText(
                    emailTemplateProvider.buildHtmlBody(
                            service.getServiceName(),
                            service.getVendorContact().getVendorName(),
                            service.getContractEndDate(),
                            config.getDaysBeforeExpiry()
                    ),
                    true
            );

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }

        log.info("Email notification sent: employee={}, service={}", config.getEmployee().getId(), service.getId());
    }
}
