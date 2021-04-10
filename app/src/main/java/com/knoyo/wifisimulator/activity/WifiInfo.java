package com.knoyo.wifisimulator.activity;

/**
 * @author lanxiaobin
 * @date 4/10/21
 */
public class WifiInfo {
    public String Ssid = "null";
    public String BSSID = "";
    public String Password = "null";

    @Override
    public String toString() {
        return "WifiInfo{" +
                "Ssid='" + Ssid + '\'' +
                ", BSSID='" + BSSID + '\'' +
                ", Password='" + Password + '\'' +
                '}';
    }
}