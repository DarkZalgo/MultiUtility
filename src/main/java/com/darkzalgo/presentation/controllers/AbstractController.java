package com.darkzalgo.presentation.controllers;

import com.darkzalgo.model.TimeClock;
import com.jcraft.jsch.Identity;
import javafx.collections.ObservableList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class AbstractController {

    public abstract void setMsgLabelText(String msg);

    public abstract void setIpTextAreaIPs(List<String> ipAddresses);

    public abstract void setProgress(double progress);
    public abstract void appendErrorTextArea(String msg);

    public abstract void setSelectedIps(ObservableList<TimeClock> selectedItems);

    public abstract String getPassword(String... ip);
}
