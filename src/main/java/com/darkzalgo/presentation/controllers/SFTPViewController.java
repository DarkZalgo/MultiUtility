package com.darkzalgo.presentation.controllers;

import com.jcraft.jsch.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

public class SFTPViewController implements Initializable
{
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

    }

    private void getstuff() throws JSchException, SftpException {



    }

    @FXML
    private void sendFile(ActionEvent event) throws SftpException, JSchException {
        Session session = new JSch().getSession("root","192.168.1.152",22);
        session.setPassword("synergy");
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "password");
        session.setConfig("MaxAuthTries", "3");
        session.connect();
        Channel channel = session.openChannel("sftp");
        ChannelSftp secureChannel = (ChannelSftp) channel;
        secureChannel.connect();
        Vector filelist = secureChannel.ls("/tmp");
        for(int i=0; i<filelist.size();i++){
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
            System.out.println(entry.getFilename());
        }

        /*FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory();
        chooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

        String filePath = "/";
        List<ChannelSftp.LsEntry> response = new ArrayList<>();

        secureChannel.ls(filePath, new ChannelSftp.LsEntrySelector() {
            @Override
            public int select(ChannelSftp.LsEntry record) {
                if (response.size() <= top) {
                    response.add(record);
                    return ChannelSftp.LsEntrySelector.CONTINUE;
                } else {
                    return ChannelSftp.LsEntrySelector.BREAK;
                }
            }
        });
        return response;*/
    }

    @FXML
    private void downloadFile()
    {

    }

}
