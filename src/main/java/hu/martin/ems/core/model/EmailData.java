package hu.martin.ems.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

@AllArgsConstructor
@Getter
@ToString
public class EmailData {
    private String host;
    private Boolean startTlsEnable;
    private Boolean auth;
    private Integer port;
    private String sslTrust;
    private String sendingAddress;
    private String sendingPassword;
}
