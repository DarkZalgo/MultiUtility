package com.darkzalgo.presentation.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class AbstractController {

    public abstract void setMsgLabelText(String msg);

    public abstract void setIpTextAreaIPs(List<String> ipAddresses);

    public abstract void setProgress(double progress);
    public abstract void appendErrorTextArea(String msg);

    public abstract void setSelectedIps(String ip);

    public abstract String getPassword(String... ip);
}
