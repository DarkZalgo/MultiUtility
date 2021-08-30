package com.darkzalgo.model;

import com.darkzalgo.presentation.gui.Context;
import javafx.beans.property.SimpleStringProperty;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeClock
{
    private String username = "root";
    private String password = "synergy";
    private String[] portsOpen;
    private String[] rawClockInfo;



    private boolean removeFlag = false;
    private boolean canConnect;

    private SimpleStringProperty model = new SimpleStringProperty("");
    private SimpleStringProperty image = new SimpleStringProperty("");
    private SimpleStringProperty ipAddress = new SimpleStringProperty("");
    private SimpleStringProperty macAddress = new SimpleStringProperty("");
    private SimpleStringProperty kernelVersion = new SimpleStringProperty("");
    private SimpleStringProperty uptime = new SimpleStringProperty("");
    private SimpleStringProperty version = new SimpleStringProperty("");
    private SimpleStringProperty rebootCount = new SimpleStringProperty("");
    private SimpleStringProperty reason = new SimpleStringProperty("");

    private int port = 22;

    private long delay;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String[] getPortsOpen() {
        return portsOpen;
    }

    public void setPortsOpen(String[] portsOpen) {
        this.portsOpen = portsOpen;
    }

    public boolean canConnect() {
        return canConnect;
    }

    public void setCanConnect(boolean canConnect) {
        this.canConnect = canConnect;
    }




    public TimeClock(String model, String image, String ipAddress, String macAddress, String kernelVersion, String uptime, String rebootCount, String version) {
        this.model = new SimpleStringProperty(model);
        this.image = new SimpleStringProperty(image);
        this.ipAddress = new SimpleStringProperty(ipAddress);
        this.macAddress = new SimpleStringProperty(macAddress);
        this.kernelVersion = new SimpleStringProperty(kernelVersion);
        this.uptime = new SimpleStringProperty(uptime);
        this.rebootCount = new SimpleStringProperty(rebootCount);
        this.version = new SimpleStringProperty(version);
    }

    public TimeClock(){}

    public String getModel() {

        return model.get();
    }

    public SimpleStringProperty modelProperty() {
        return model;
    }

    public void setModel(String model) {
        this.model.set(model);
    }

    public String getImage() {
        return image.get();
    }

    public SimpleStringProperty imageProperty() {
        return image;
    }

    public void setImage(String image) {
        this.image.set(image);
    }

    public String getIpAddress() {
        return ipAddress.get();
    }

    public SimpleStringProperty ipAddressProperty() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress.set(ipAddress);
    }

    public String getMacAddress() {
        return macAddress.get();
    }

    public SimpleStringProperty macAddressProperty() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress.set(macAddress);
    }

    public String getKernelVersion() {
        return kernelVersion.get();
    }

    public SimpleStringProperty kernelVersionProperty() {
        return kernelVersion;
    }

    public void setKernelVersion(String kernelVersion) {
        this.kernelVersion.set(kernelVersion);
    }

    public String getUptime() {
        return uptime.get();
    }

    public SimpleStringProperty uptimeProperty() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime.set(uptime);
    }

    public String getVersion() {
        return version.get();
    }

    public SimpleStringProperty versionProperty() {
        return version;
    }

    public void setVersion(String version) {
        this.version.set(version);
    }

    public String getRebootCount() {
        return rebootCount.get();
    }

    public SimpleStringProperty rebootCountProperty() {
        return rebootCount;
    }

    public void setRebootCount(String rebootCount) {
        this.rebootCount.set(rebootCount);
    }

    public boolean flaggedForRemoval() {
        return removeFlag;
    }

    public void setRemoveFlag(boolean removeFlag) {
        this.removeFlag = removeFlag;
    }

    public String getReason() {
        return reason.get();
    }

    public SimpleStringProperty reasonProperty() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = new SimpleStringProperty(reason);
    }

    public String[] getRawClockInfo() {
        return rawClockInfo;
    }

    public void setRawClockInfo(String[] rawClockInfo) {
        this.rawClockInfo = rawClockInfo;
    }

    public void updateClockInfo()
    {
        String[] clockInfo = this.rawClockInfo;
        if ( null != clockInfo && clockInfo.length >= 11) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd hh:mm");
            String date = formatter.format(new Date(System.currentTimeMillis()));
            int uptime = Math.round(Float.parseFloat(clockInfo[4]));
            int upDays = uptime / 86400;
            int upHours = (uptime % 86400) / 3600;
            int upMinutes = ((uptime % 86400) % 3600) / 60;
            String image = "Unknown";
            String version = clockInfo[2];
            this.setVersion(clockInfo[9]);
            if (version.toLowerCase().contains("ta"))
                image = "XactTime";
            if (version.toLowerCase().contains("frontline") || clockInfo[9].toLowerCase().contains("fl"))
                image = "Frontline";
            else if (version.toLowerCase().contains("kronos"))
                image = "Kronos";

            if (!image.equals("Kronos") || !image.equals("Frontline")) {
                if (version.contains("1.4.1")) {
                    this.setVersion("1.4.1");
                } else if (version.contains("1.4.2")) {
                    this.setVersion("1.4.2");
                } else if (version.contains("1.4.3")) {
                    this.setVersion("1.4.3");
                }

            }
            if (clockInfo[6].toLowerCase().contains("wbcs")) {
                if (version.contains("1.4.1") && version.contains("S")) {
                    this.setVersion("1.4.1 S");
                } else if (version.contains("1.4.2") && version.contains("S")) {
                    this.setVersion("1.4.2 S");
                } else if (version.contains("1.4.3") && version.contains("S")) {
                    this.setVersion("1.4.3 S");
                }
                if (clockInfo[8].toLowerCase().contains("nhc"))
                    image = "NHC";
                else if (clockInfo[8].toLowerCase().contains("compass"))
                    image = "Compass";
                else if (clockInfo[8].toLowerCase().contains("unc"))
                    image = "UNC";
                else if (clockInfo[8].toLowerCase().contains("hm"))
                    image = "H&M";
                else if (clockInfo[8].toLowerCase().contains("cvs"))
                    image = "CVS";
                else if (clockInfo[8].toLowerCase().contains("benchmark"))
                    image = "Benchmark";
                else if (clockInfo[8].toLowerCase().contains("ipaper"))
                    image = "iPaper";
                else if (clockInfo[8].toLowerCase().contains("speedway"))
                    image = "Speedway";
                else if (clockInfo[8].toLowerCase().contains("gpi"))
                    image = "GPI";
                else if (clockInfo[8].toLowerCase().contains("mctx"))
                    image = "Montgomery";
                else if (clockInfo[8].toLowerCase().contains("secinc"))
                    image = "Securitas";
                else if (clockInfo[8].toLowerCase().contains("nhrmc"))
                    image = "New Hanover";
                else if (clockInfo[8].toLowerCase().contains("cha"))
                    image = "COA";
                else if (clockInfo[8].toLowerCase().contains("chs"))
                    image = "CHS";
                else
                    image = "Infor";
            }

            if (clockInfo[6].toLowerCase().contains("menards"))
                image = "Menard's";
            if (clockInfo[7].contains("SynergyDemo"))
                image = "SGA";
            if (clockInfo[7].toLowerCase().contains("tress"))
                image = "Grupo";

            if (clockInfo[10].contains("3.")) {
                this.setVersion(clockInfo[10]);
            }

            this.setMacAddress(!clockInfo[1].equals("") ? clockInfo[1].trim() : "Default");
            this.setModel(clockInfo[0].contains("X") ? "SYnergy/A20" : "SYnergy/A 2416");
            this.setImage(image);

            this.setKernelVersion(clockInfo[3]);
            this.setUptime("Days: " + upDays + " Hrs: " + upHours + " Mins: " + upMinutes);
            this.setRebootCount(clockInfo[5]);
            this.setCanConnect(true);
            this.setRemoveFlag(false);
            Context.getInstance().getTableViewController().addToImageSet(image);
        }
    }

}
