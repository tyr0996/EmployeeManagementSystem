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
import java.io.ByteArrayOutputStream;

@Service
@AllArgsConstructor
@NeedCleanCoding
public class SftpSender {

    private JschConfig jschConfig;
    private final Logger logger = LoggerFactory.getLogger(SftpSender.class);

    public boolean send(byte[] data, String destination){
        try {
//            jschConfig.connect();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(data);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

//            jschConfig.getSession().connect();
//            jschConfig.getChannelSftp().connect();
            jschConfig.connect();
            jschConfig.getChannelSftp().put(inputStream, destination);
            jschConfig.getChannelSftp().disconnect();
            System.out.println("SFTP sender success");
            return true;
        } catch (SftpException e){
            System.out.println("SFTP sender fail");
            jschConfig.getChannelSftp().disconnect();
            logger.error("EmsError occurred while uploading file!");
            return false;
        } catch (JSchException e){
            logger.error("The file upload failed because the connection to the SFTP server could not be established! Check the log for errors!");
            return false;
        } catch (Exception e) {
            logger.error("Failed to create the output stream");
            return false;
        }
//        finally{
////            if(jschConfig.getChannelSftp() != null && jschConfig.getChannelSftp().isConnected()){
//            System.out.println("SFTP sender finally: " + (jschConfig.getChannelSftp() != null));
//            if(jschConfig.getChannelSftp() != null) {
//                jschConfig.getChannelSftp().disconnect();
//            }
//        }
    }
}
