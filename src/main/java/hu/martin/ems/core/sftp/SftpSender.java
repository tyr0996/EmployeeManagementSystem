package hu.martin.ems.core.sftp;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.JschConfig;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
@AllArgsConstructor
@NeedCleanCoding
public class SftpSender {

    private final JschConfig jschConfig;
    private final Logger logger = LoggerFactory.getLogger(SftpSender.class);

    public JschConfig getConfig(){
        return this.jschConfig;
    }

    public boolean send(byte[] data, String destination){
        try {
            jschConfig.connect();

            if(!jschConfig.successfullyStarted){
                logger.error("The file upload failed because the connection to the SFTP server could not be established! Check the log for errors!");
                return false;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(data);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            if(!jschConfig.getSession().isConnected()){
                jschConfig.getSession().connect();
            }
            if(!jschConfig.getChannelSftp().isConnected()){
                jschConfig.getChannelSftp().connect();
            }

            jschConfig.getChannelSftp().put(inputStream, destination);
            return true;
        } catch (Exception e) {
            logger.error("Error occurred while uploading file!");
            return false;
        }
        finally{
            if(jschConfig.getChannelSftp() != null && jschConfig.getChannelSftp().isConnected()){
                jschConfig.getChannelSftp().disconnect();
            }
        }
    }
}
