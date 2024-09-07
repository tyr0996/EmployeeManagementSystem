package hu.martin.ems.core.config;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.sftp.SftpSender;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;

@Configuration
@NeedCleanCoding
public class JschConfig {

    private static Logger log = LoggerFactory.getLogger(SftpSender.class);

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.port}")
    private int sftpPort;

    @Getter
    @Value("${sftp.user.tester}")
    private String sftpUser;

    @Value("${sftp.privateKey}")
    private String sftpPrivateKey;

    @Value("${sftp.privateKeyPassphrase}")
    private String sftpPrivateKeyPassphrase;

    @Value("${sftp.password}")
    private String sftpPassword;

    public Boolean successfullyStarted = false;

    private static ChannelSftp channelSftp;

    private static Session session;

    public Session getSession(){
        return session;
    }

    public ChannelSftp getChannelSftp(){
        return channelSftp;
    }


    public void init() {
        boolean succ = true;
        if (channelSftp == null || !channelSftp.isConnected() || !session.getUserName().equals(sftpUser)) {
            JSch jsch = new JSch();

            if (sftpPrivateKey != null && !sftpPrivateKey.equals("")) {
                try{
                    Resource keyResource = new PathMatchingResourcePatternResolver().getResources(sftpPrivateKey)[0];
                    InputStream in = keyResource.getInputStream();
                    byte[] ba = IOUtils.toByteArray(in);

                    if(sftpPrivateKeyPassphrase != null && sftpPrivateKeyPassphrase.trim().length() > 0){
                        jsch.addIdentity("private.ppk", ba, null, sftpPrivateKeyPassphrase.getBytes());
                    }
                    else{
                        jsch.addIdentity("private.ppk", ba, null, (byte[]) null);
                    }
                }
                catch (IOException e){
                    succ = false;
                    log.error("The specified private key does not exist at the given location! (" + sftpPrivateKey + ")");
                }
                catch(JSchException e){
                    succ = false;
                    log.error("The private key is invalid, or the password for the private key is incorrect!");
                }
            }

            Session jschSession = null;
            try{
                jschSession = jsch.getSession(sftpUser, sftpHost, sftpPort);
                session = jschSession;
            }
            catch(JSchException e){
                log.error("An unknown error occurred while opening the session!");
                succ = false;
            }
            jschSession.setPassword(jschSession != null && sftpPassword != null && !sftpPassword.equals("") ? sftpPassword : null);
            jschSession.setConfig("StrictHostKeyChecking", "no");
            try{
                jschSession.connect();
            }
            catch(JSchException e){
                succ = false;
                if(e.getCause() instanceof ConnectException){
                    log.error("Failed to connect to the host! It may not be running or it could be outside the domain. ("+sftpHost+")");
                }
                else if(e.getMessage().equals("Auth fail")){
                    log.error("Connection to the host failed because the login credentials are incorrect! (" + sftpHost + ")");
                }
                else{
                    log.error("An unknown error occurred while connecting to the host! " + e.getMessage());
                }
            }
            try{
                channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
            }
            catch (Exception e){
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
        if(!successfullyStarted || !this.getSession().getUserName().equals("tester")){
            init();
        }
        if(channelSftp == null || !channelSftp.isConnected()){
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
        }
        if(!session.isConnected()){
            session.connect();
        }
    }
}
