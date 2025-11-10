package com.egfs.biometrictest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zkteco")
public class ZktecoProperties {
    private String ip;
    private int port = 4370;
    private String password;
    private int machineNumber = 1;

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getMachineNumber() { return machineNumber; }
    public void setMachineNumber(int machineNumber) { this.machineNumber = machineNumber; }
}

