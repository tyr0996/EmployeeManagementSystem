package hu.martin.ems.core.service;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.model.EmailAttachment;
import hu.martin.ems.core.model.EmailProperties;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.eclipse.angus.mail.util.MailConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@NeedCleanCoding
public class EmailSendingService {

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

    private Logger logger = LoggerFactory.getLogger(EmailSendingService.class);


    public boolean send(EmailProperties emailProperties) {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.trust", sslTrust);
        props.put("mail.smtp.starttls.enable", startTlsEnable);

        Session session = Session.getDefaultInstance(props,
                new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(sendingAddress, sendingPassword);
                    }
                });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sendingAddress));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailProperties.getTo()));
            message.setSubject(emailProperties.getSubject());

            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart mainMessage = new MimeBodyPart();
            mainMessage.setText(emailProperties.getHtmlText(), "utf-8", "html");
            multipart.addBodyPart(mainMessage);

            try {
                if(emailProperties.getAttachments() != null){
                     for(EmailAttachment emailAttachment : emailProperties.getAttachments()){
                        MimeBodyPart attachment = new MimeBodyPart();
                        ByteArrayDataSource ds = new ByteArrayDataSource(emailAttachment.getData(), emailAttachment.getContentType());
                        attachment.setDataHandler(new DataHandler(ds));
                        attachment.setFileName(emailAttachment.getFileName());
                        multipart.addBodyPart(attachment);
                    };
                }
            } catch (MessagingException e) {
                logger.error("Error occured while sending email. " + e);
            }

            message.setContent(multipart);
            try{
                Transport.send(message);
                logger.info("Email sent to " + emailProperties.getTo() + " successfully!");
            }
            catch (MailConnectException e) {
                logger.error("Email connect refused.");
                return false;
            }

            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
