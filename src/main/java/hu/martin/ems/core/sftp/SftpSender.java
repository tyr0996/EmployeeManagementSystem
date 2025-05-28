package hu.martin.ems.core.sftp;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.JschConfig;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@AllArgsConstructor
@NeedCleanCoding
public class SftpSender {

    private JschConfig jschConfig;
    private final Logger logger = LoggerFactory.getLogger(SftpSender.class);

    public boolean send(byte[] data, String fileName){
        try {
            ByteArrayInputStream inputStream = createInputStreamFromByteArray(data);
            jschConfig.connect();
            jschConfig.getChannelSftp().put(inputStream, fileName);
            jschConfig.disconnect();
            return true;
        } catch (SftpException e){
//            jschConfig.getChannelSftp().disconnect();
            logger.error("EmsError occurred while uploading file!");
            jschConfig.disconnect();
            return false;
        } catch (JSchException e) {
            logger.error("The file upload failed because the connection to the SFTP server coulde not be established! Check the log for errors!");
            jschConfig.disconnect();
            return false;
        }
    }

    public ByteArrayInputStream createInputStreamFromByteArray(byte[] data) {
        return new ByteArrayInputStream(data);
    }
}

//        finally{
////            if(jschConfig.getChannelSftp() != null && jschConfig.getChannelSftp().isConnected()){
//            System.out.println("SFTP sender finally: " + (jschConfig.getChannelSftp() != null));
//            if(jschConfig.getChannelSftp() != null) {
//                jschConfig.getChannelSftp().disconnect();
//            }
//        }
