package lt.pskurimas.ptvs.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.pskurimas.ptvs.config.AuditProperties;
import lt.pskurimas.ptvs.model.AuditLogEntry;
import lt.pskurimas.ptvs.model.UserRole;
import lt.pskurimas.ptvs.resolver.SecurityContextUserResolver;
import lt.pskurimas.ptvs.service.AuditService;
import lt.pskurimas.ptvs.util.DateProvider;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEntryAspect {

    private final AuditService auditService;
    private final DateProvider dateProvider;
    private final AuditProperties auditProperties;
    private final SecurityContextUserResolver userResolver;
    private final ObjectMapper objectMapper;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Auditable auditable, Object result) {
        audit(joinPoint, auditable);
    }

    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Auditable auditable, Exception ex) {
        audit(joinPoint, auditable);
    }

    private void audit(JoinPoint joinPoint, Auditable auditable) {
        if (auditProperties.isAuditEnabled()) {
            var event = buildAuditLogEntry(joinPoint, auditable);
            try {
                auditService.createAuditLogEntry(event);
            } catch (Exception e) {
                log.error("Error while persisting audit entry: {}", e.getMessage());
            }
        }
    }

    private AuditLogEntry buildAuditLogEntry(JoinPoint joinPoint, Auditable auditable) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        AuditLogEntry.AuditLogEntryBuilder auditLogEntryBuilder = AuditLogEntry.builder()
                .className(className)
                .methodName(methodName)
                .actionName(resolveActionName(auditable))
                .payloadJson(resolvePayloadJson(joinPoint, auditable))
                .createdAt(dateProvider.getCurrentDateTime());

        userResolver.resolveUser().ifPresent(user -> {
            String roles = user.getRoles().stream()
                    .map(UserRole::name)
                    .sorted()
                    .collect(Collectors.joining(","));

            auditLogEntryBuilder.userId(user.getId())
                    .username(user.getUsername())
                    .roles(roles);
        });

        return auditLogEntryBuilder.build();
    }

    private String resolvePayloadJson(JoinPoint joinPoint, Auditable auditable) {
        Class<?> payloadType = auditable.payloadType();
        if (payloadType == null || payloadType == Void.class) {
            return null;
        }
        return Optional.ofNullable(joinPoint.getArgs())
                .flatMap(args -> Arrays.stream(args)
                        .filter(Objects::nonNull)
                        .filter(arg -> payloadType.isAssignableFrom(arg.getClass()))
                        .map(arg -> serializePayload(arg, payloadType))
                        .findFirst())
                .orElse(null);
    }

    private String serializePayload(Object payload, Class<?> payloadType) {
        try {
            return objectMapper.writerFor(payloadType).writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize audit payload for " + payloadType.getSimpleName(), ex);
        }
    }

    private String resolveActionName(Auditable auditable) {
        AuditAction action = auditable.action();
        if (action == null || action == AuditAction.UNSPECIFIED) {
            return null;
        }
        return action.getDisplayName();
    }
}
