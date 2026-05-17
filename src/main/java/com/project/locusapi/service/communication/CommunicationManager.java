package com.project.locusapi.service.communication;

public interface CommunicationManager {

    void sendMsg(String to, String subject, String text);

}
