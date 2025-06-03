package hu.martin.ems.core.service;

import jakarta.mail.Authenticator;

public class EmsEmailAuthenticator extends Authenticator {
    private final String sendingAddress;
    private final String sendingPassword;

    public EmsEmailAuthenticator(String sendingAddress, String sendingPassword) {
        this.sendingAddress = sendingAddress;
        this.sendingPassword = sendingPassword;
    }
}
