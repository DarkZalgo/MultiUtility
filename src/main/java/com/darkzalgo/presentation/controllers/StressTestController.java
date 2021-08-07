package com.darkzalgo.presentation.controllers;

import com.darkzalgo.presentation.gui.Context;
import com.darkzalgo.utility.SSHHandler;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class StressTestController extends MainController implements Initializable {

    private SSHHandler sshHandler = new SSHHandler(this);

    private static final Logger logger = LoggerFactory.getLogger(StressTestController.class);



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Context.getInstance().setStressTestController(this);
    }
}
