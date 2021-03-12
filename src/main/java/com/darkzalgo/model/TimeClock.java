package com.darkzalgo.model;

import javafx.beans.property.SimpleStringProperty;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TimeClock
{
    private String username = "root";
    private String password = "synergy";
    private String[] portsOpen;

    private boolean removeFlag = false;
    private boolean canConnect;

    private SimpleStringProperty model = new SimpleStringProperty();
    private SimpleStringProperty image = new SimpleStringProperty();
    private SimpleStringProperty ipAddress = new SimpleStringProperty();
    private SimpleStringProperty macAddress = new SimpleStringProperty();
    private SimpleStringProperty kernelVersion = new SimpleStringProperty();
    private SimpleStringProperty uptime = new SimpleStringProperty();
    private SimpleStringProperty version = new SimpleStringProperty();
    private SimpleStringProperty rebootCount = new SimpleStringProperty();

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

}
