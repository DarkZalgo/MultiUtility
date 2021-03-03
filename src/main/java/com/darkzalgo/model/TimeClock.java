package com.darkzalgo.model;

public class TimeClock
{
    private String version;
    private String mac;
    private String host;
    private String model;
    private String image;
    private String username = "root";
    private String password = "synergy";
    private String kernelVersion;
    private String uptime;
    private String date;
    private String rebootCount;

    private String[] portsOpen;

    private boolean canConnect = false;

    private int port = 22;

    private long delay;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

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

    public String getKernelVersion() {
        return kernelVersion;
    }

    public void setKernelVersion(String kernelVersion) {
        this.kernelVersion = kernelVersion;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getDate() {
        return date;
    }

    public boolean canConnect() {
        return canConnect;
    }

    public void setCanConnect(boolean canConnect) {
        this.canConnect = canConnect;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRebootCount() {
        return rebootCount;
    }

    public void setRebootCount(String rebootCount) {
        this.rebootCount = rebootCount;
    }
}
