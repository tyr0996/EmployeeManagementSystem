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

    @Value("${sftp.password}")
    private String sftpPassword;

    @Getter
    @Setter
    private ChannelSftp channelSftp;

    @Setter
    @Getter
    private Session session;

    public static JSch jsch = new JSch();


    public void init() throws JSchException {
        try {
            if (channelSftp == null) {
                session = createSession();
                log.info("Session started");

                session.setPassword(sftpPassword);
                session.setConfig("StrictHostKeyChecking", "no");

                connectToSession();
                log.info("Connected to session");
                openSftpChannel();
                log.info("SFTP channel opened");
                log.info("The SFTP connection to the host has been successfully established ("+sftpHost+")");
            }
            else{
                log.info("The SFTP connection already existed");
            }
        }
        catch (JSchException e){
            log.warn("The SFTP connection failed for some reason! Please check the log for errors.");
            throw e;
        }
    }

    private Session createSession() throws JSchException{
        try{
            Session session = jsch.getSession(sftpUser, sftpHost, sftpPort);
            return session;
        }
        catch(JSchException e){
            log.error("An unknown error occurred while opening the session!");
            throw new JSchException("An unknown error occurred while opening the session!");
        }
    }

    private void connectToSession() throws JSchException {
        try{
            session.connect();
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
    }

    private void openSftpChannel() throws JSchException {
        try{
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
        }
        catch (JSchException e){
            log.error("An error occurred while opening the channel! Please check that the session's isConnected() property is true.");
            throw new JSchException("An error occurred while opening the channel! Please check that the session's isConnected() property is true.");
        }
    }

    public void connect() throws JSchException {
        init();
    }

    public void disconnect(){
        log.info("Attempting to disconnect SFTP connection...");
        destroySftpChannel();
        destroyJSchSession();

        log.info("SFTP disconnection process completed.");
    }

    private void destroySftpChannel() {
        if (channelSftp != null) {
            channelSftp.disconnect();
            log.info("SFTP channel disconnected successfully.");
            channelSftp = null;
        }
        else {
            log.info("SFTP channel was already null, no action needed.");
        }
    }

    private void destroyJSchSession(){
        if (session != null) {
            session.disconnect();
            log.info("SFTP session disconnected successfully.");
            session = null;
        } else {
            log.info("SFTP session was already null, no action needed.");
        }
    }
}
