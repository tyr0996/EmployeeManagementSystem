package hu.martin.ems.core.config;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.sftp.SftpSender;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.ConnectException;

@Configuration
@NeedCleanCoding
public class JschConfig {

    private static Logger log = LoggerFactory.getLogger(SftpSender.class);

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.port}")
    private int sftpPort;

    @Value("${sftp.user.tester}")
    private String sftpUser;

    @Value("${sftp.privateKey}")
    private String sftpPrivateKey;

    @Value("${sftp.privateKeyPassphrase}")
    private String sftpPrivateKeyPassphrase;

    @Value("${sftp.password}")
    private String sftpPassword;

    public Boolean successfullyStarted = false;

    @Getter
    @Setter
    private ChannelSftp channelSftp;

    @Setter
    @Getter
    private Session session;

    public static JSch jsch = new JSch();


    public void init() throws JSchException {
        boolean succ = true;
        if (channelSftp == null || !channelSftp.isConnected() || !session.getUserName().equals(sftpUser)) {
            Session jschSession = null;
            try{
                jschSession = jsch.getSession(sftpUser, sftpHost, sftpPort);
                session = jschSession;
            }
            catch(JSchException e){
                log.error("An unknown error occurred while opening the session!");
                throw new JSchException("An unknown error occurred while opening the session!");
            }
            String password = jschSession != null && sftpPassword != null && !sftpPassword.equals("") ? sftpPassword : null;
            jschSession.setPassword(password);
            jschSession.setConfig("StrictHostKeyChecking", "no");
            try{
                jschSession.connect();
            }
            catch(JSchException e){
                if(e.getCause() instanceof ConnectException){
                    log.error("Failed to connect to the host! It may not be running or it could be outside the domain. ("+sftpHost+")");
                    throw new JSchException("Failed to connect to the host! It may not be running or it could be outside the domain. ("+sftpHost+")");
                }
                else if(e.getMessage().equals("Auth fail")){
                    log.error("Connection to the host failed because the login credentials are incorrect! (" + sftpHost + ")");
                    throw new JSchException("Connection to the host failed because the login credentials are incorrect! (" + sftpHost + ")");
                }
                else{
                    log.error("An unknown error occurred while connecting to the host! " + e.getMessage());
                    throw new JSchException("An unknown error occurred while connecting to the host! " + e.getMessage());
                }
            }
            try{
                channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
            }
            catch (JSchException e){
                succ = false;
                log.error("An error occurred while opening the channel! Please check that the session's isConnected() property is true.");
            }
        }
        else{
            succ = false;
        }

        if(succ){
            successfullyStarted = true;
            log.info("The SFTP connection to the host has been successfully established ("+sftpHost+")");
        }
        else{
            log.warn("The SFTP connection failed for some reason! Please check the log for errors.");
        }
    }

    public void connect() throws JSchException {
        init();
        if(channelSftp == null){
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
        }
        session.connect();
    }
}
