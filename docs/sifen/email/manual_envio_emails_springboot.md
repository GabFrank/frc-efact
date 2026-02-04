# Manual de Implementación de Envío de Emails en Spring Boot  
### Usando Gmail (SMTP) + Adjuntos XML/PDF para Factura Electrónica

## Objetivo
Implementar en un servidor Spring Boot la capacidad de enviar correos electrónicos utilizando Gmail como servidor SMTP y permitir el envío de XML/PDF de facturas electrónicas.

## 1. Dependencia
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

## 2. Configuración SMTP Gmail (`application.yml`)
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: frcsistemasinformaticos@gmail.com
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
    default-encoding: UTF-8
```

## 3. Servicio Base: EmailService
```java
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String defaultFrom = "frcsistemasinformaticos@gmail.com";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailSimple(String to, String subject, String body, boolean isHtml) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);
            helper.setFrom(defaultFrom);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Error enviando email simple", e);
        }
    }

    public void enviarEmailConAdjuntos(String to, String subject, String body, boolean isHtml, Map<String, byte[]> attachments) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);
            helper.setFrom(defaultFrom);

            if (attachments != null) {
                for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
                    helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
                }
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Error enviando email con adjuntos", e);
        }
    }
}
```

## 4. Async Config
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-exec-");
        executor.initialize();
        return executor;
    }
}
```

## 5. Servicio de Factura Electrónica
```java
@Service
public class EmailFacturaElectronicaService {

    private final EmailService emailService;

    public EmailFacturaElectronicaService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async("emailExecutor")
    public void enviarFacturaAlClienteAsync(FacturaElectronica factura, byte[] xmlBytes, byte[] pdfBytesOpcional) {
        String emailCliente = factura.getCliente().getEmail();
        if (emailCliente == null || emailCliente.isBlank()) return;

        String subject = "Factura electrónica N° " + factura.getNumero();

        String bodyHtml = String.format(
            "<p>Hola %s,</p><p>Adjuntamos tu factura electrónica N° <strong>%s</strong>.</p><p>Gracias por tu preferencia.</p>",
            factura.getCliente().getNombre(), factura.getNumero()
        );

        Map<String, byte[]> attachments = new HashMap<>();
        attachments.put("factura-" + factura.getNumero() + ".xml", xmlBytes);

        if (pdfBytesOpcional != null) {
            attachments.put("factura-" + factura.getNumero() + ".pdf", pdfBytesOpcional);
        }

        emailService.enviarEmailConAdjuntos(emailCliente, subject, bodyHtml, true, attachments);
    }
}
```

## 6. Uso en tu flujo de facturación
```java
emailFacturaElectronicaService.enviarFacturaAlClienteAsync(factura, xmlBytes, pdfBytesOpcional);
```

## 7. Requisitos Gmail
- Activar verificación en dos pasos.
- Crear App Password.
- Configurar variable MAIL_PASSWORD.
