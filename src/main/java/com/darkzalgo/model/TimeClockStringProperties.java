package com.darkzalgo.model;

import javafx.beans.property.SimpleStringProperty;

public class TimeClockStringProperties
{
    private SimpleStringProperty model;
    private SimpleStringProperty image;
    private SimpleStringProperty ipAddress;
    private SimpleStringProperty macAddress;
    private SimpleStringProperty kernelVersion;
    private SimpleStringProperty uptime;
    private SimpleStringProperty date;
    private SimpleStringProperty rebootCount;

    public TimeClockStringProperties(String model, String image, String ipAddress, String macAddress, String kernelVersion, String uptime, String rebootCount, String date) {
        this.model = new SimpleStringProperty(model);
        this.image = new SimpleStringProperty(image);
        this.ipAddress = new SimpleStringProperty(ipAddress);
        this.macAddress = new SimpleStringProperty(macAddress);
        this.kernelVersion = new SimpleStringProperty(kernelVersion);
        this.uptime = new SimpleStringProperty(uptime);
        this.rebootCount = new SimpleStringProperty(rebootCount);
        this.date = new SimpleStringProperty(date);
    }

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

    public String getDate() {
        return date.get();
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public void setDate(String date) {
        this.date.set(date);
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
}
