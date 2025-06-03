package hu.martin.ems.core.config;

import hu.martin.ems.core.model.EmailData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Value("${mail.smtp.host}")
    private String host;

    @Value("${mail.smtp.starttls.enable}")
    private Boolean startTlsEnable;

    @Value("${mail.smtp.auth}")
    private Boolean auth;

    @Value("${mail.smtp.port}")
    private Integer port;

    @Value("${mail.smtp.ssl.trust}")
    private String sslTrust;

    @Value("${mail.smtp.sending.address}")
    private String sendingAddress;

    @Value("${mail.smtp.sending.password}")
    private String sendingPassword;

    @Bean
    public EmailData getEmailData() {
        return new EmailData(host, startTlsEnable, auth, port, sslTrust, sendingAddress, sendingPassword);
    }
}
