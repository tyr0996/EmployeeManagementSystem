package hu.martin.ems.core.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmailAttachment;
import hu.martin.ems.core.model.EmailData;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@NeedCleanCoding
public class EmailSendingService {
    private static final Properties props = new Properties();

    @Autowired
    protected Environment env;

    @Autowired
    protected EmailData emailData;

    private Logger logger = LoggerFactory.getLogger(EmailSendingService.class);

    public boolean send(EmailProperties emailProperties) {
        props.put("mail.smtp.host", emailData.getHost());
        props.put("mail.smtp.auth", emailData.getAuth());
        props.put("mail.smtp.port", emailData.getPort());
        props.put("mail.smtp.ssl.trust", emailData.getSslTrust());
        props.put("mail.smtp.starttls.enable", emailData.getStartTlsEnable());

        String sendingAddress = emailData.getSendingAddress();
        String sendingPassword = emailData.getSendingPassword();

        Session session = Session.getDefaultInstance(props, new EmsEmailAuthenticator(sendingAddress, sendingPassword));
        try {
            MimeMessage message = createMimeMessage(session, emailProperties);

            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart mainMessage = new MimeBodyPart();
            mainMessage.setText(emailProperties.getHtmlText(), "utf-8", "html");
            multipart.addBodyPart(mainMessage);

            try {
                for (EmailAttachment emailAttachment : emailProperties.getAttachments()) {
                    multipart.addBodyPart(createAttachmentBodyPart(emailAttachment));
                }
            } catch (MessagingException e) {
                logger.error("EmsError occured while sending email. " + e);
                return false;
            }

            message.setContent(multipart);
            try {
                transportSend(message);
            } catch (MailConnectException e) {
                logger.error("Email connect refused.");
                return false;
            }
            logger.info("Email sent to " + emailProperties.getTo() + " successfully!");
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public BodyPart createAttachmentBodyPart(EmailAttachment emailAttachment) throws MessagingException {
        MimeBodyPart attachment = new MimeBodyPart();
        ByteArrayDataSource ds = new ByteArrayDataSource(emailAttachment.getData(), emailAttachment.getContentType());
        attachment.setDataHandler(new DataHandler(ds));
        attachment.setFileName(emailAttachment.getFileName());
        return attachment;
    }

    public MimeMessage createMimeMessage(Session session, EmailProperties emailProperties) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailData.getSendingAddress()));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailProperties.getTo()));
        message.setSubject(emailProperties.getSubject());
        return message;
    }

    public void transportSend(MimeMessage message) throws MessagingException {
        //TODO megcsinálni, hogy ne kelljen külön függvényt létrehozni ezért, hogy tudjam mockolni. MockStatic addig működik, ameddig
        // legfeljebb controller szinten hívom meg. Ha már az ApiClientből, akkor már nem jó. De ez csak a statikusnál van így, a sima az OK.
        Transport.send(message);
    }
}
