package com.knoyo.wifisimulator.activity;

/**
 * @author lanxiaobin
 * @date 4/10/21
 */
import android.os.Build;
import android.util.Log;
import android.util.Xml;


import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WifiPasswordUtil {

    /**
     * 8.0之前
     * /**
     * #Android8.0（Oreo）之前版本
     * /data/misc/wifi/wpa_supplicant.conf
     * #Android8.0（Oreo）之后版本
     * /data/misc/wifi/WifiConfigStore.xml
     */
    public static List<WifiInfo> readWifiPassworld() {
        List<WifiInfo> wifiInfos = new ArrayList<>();
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            wifiInfos = ReadWifiPasswordOnAndroidO();
        } else {
            wifiInfos = ReadWifiPasswordBelowAndroidO();
        }

        return wifiInfos;
    }


    private static List<WifiInfo> ReadWifiPasswordBelowAndroidO(){
        List<WifiInfo> wifiInfos = new ArrayList<WifiInfo>();
        Process process = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        StringBuffer wifiConf = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream
                    .writeBytes("cat /data/misc/wifi/wpa_supplicant.conf\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(
                    dataInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(
                    inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                wifiConf.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            return wifiInfos;
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL);
        Matcher networkMatcher = network.matcher(wifiConf.toString());
        while (networkMatcher.find()) {
            String networkBlock = networkMatcher.group();
            Pattern ssid = Pattern.compile("ssid=\"([^\"]+)\"");
            Matcher ssidMatcher = ssid.matcher(networkBlock);

            if (ssidMatcher.find()) {
                WifiInfo wifiInfo = new WifiInfo();
                wifiInfo.Ssid = ssidMatcher.group(1);
                Pattern psk = Pattern.compile("psk=\"([^\"]+)\"");
                Matcher pskMatcher = psk.matcher(networkBlock);
                if (pskMatcher.find()) {
                    wifiInfo.Password = pskMatcher.group(1);
                } else {
                    wifiInfo.Password = "无密码";
                }
                wifiInfos.add(wifiInfo);
            }

        }

        return wifiInfos;
    }


    /**
     * 8.0 之后读取密码文件
     *
     * @return
     */
    private static List<WifiInfo> ReadWifiPasswordOnAndroidO() {
        List<WifiInfo> wifiInfos = new ArrayList<WifiInfo>();

        Process process = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream.writeBytes("cat /data/misc/wifi/WifiConfigStore.xml\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            return xmlParse(dataInputStream);

        } catch (Exception e) {
            e.printStackTrace();
            return wifiInfos;
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    /**
     *
     <WifiConfiguration>
     <string name="ConfigKey">&quot;lizhi_test&quot;WPA_PSK</string>
     <string name="SSID">&quot;lizhi_test&quot;</string>
     <string name="OriSsid">6c697a68695f74657374</string>
     <null name="BSSID" />
     <string name="PreSharedKey">&quot;88888888&quot;</string>
     <null name="WEPKeys" />
     <int name="WEPTxKeyIndex" value="0" />
     <int name="Priority" value="0" />
     <boolean name="HiddenSSID" value="false" />
     <boolean name="RequirePMF" value="false" />
     <byte-array name="AllowedKeyMgmt" num="1">02</byte-array>
     <byte-array name="AllowedProtocols" num="1">03</byte-array>
     <byte-array name="AllowedAuthAlgos" num="1">01</byte-array>
     <byte-array name="AllowedGroupCiphers" num="1">0f</byte-array>
     <byte-array name="AllowedPairwiseCiphers" num="1">06</byte-array>
     <int name="WapiPskKeyType" value="-1" />
     <null name="WapiAsCertPath" />
     <null name="WapiUserCertPath" />
     <boolean name="Shared" value="true" />
     <int name="Status" value="2" />
     <null name="FQDN" />
     <null name="ProviderFriendlyName" />
     <map name="LinkedNetworksList" />
     <null name="DefaultGwMacAddress" />
     <boolean name="ValidatedInternetAccess" value="true" />
     <boolean name="NoInternetAccessExpected" value="false" />
     <int name="ConnectToCellularAndWLAN" value="0" />
     <int name="WifiApType" value="0" />
     <int name="NumNoInternetAccessReports" value="0" />
     <string name="INTERNET_HISTORY">1/1/1/1/1/1/1/1/1/1</string>
     <boolean name="PORTAL_NETWORK" value="false" />
     <long name="LAST_HAS_INTERNET_TS" value="1618061130068" />
     <long name="LAST_TRY_SWTICH_WIFI_TS" value="0" />
     <boolean name="WIFI_PRO_TEMP_CREATED" value="false" />
     <string name="LAST_DHCP_RESULTS">-1||192.168.43.101|24|0|0|192.168.43.241|192.168.43.241|</string>
     <string name="INET_SELF_CURE_HISTORY"></string>
     <boolean name="PORTAL_CONNECT" value="false" />
     <int name="UserApproved" value="0" />
     <boolean name="MeteredHint" value="false" />
     <int name="MeteredOverride" value="0" />
     <boolean name="UseExternalScores" value="false" />
     <int name="NumAssociation" value="12" />
     <int name="CreatorUid" value="1000" />
     <string name="CreatorName">android.uid.system:1000</string>
     <string name="CreationTime">time=03-31 11:13:13.599</string>
     <int name="LastUpdateUid" value="1000" />
     <string name="LastUpdateName">android.uid.system:1000</string>
     <int name="LastConnectUid" value="1000" />
     <boolean name="IsLegacyPasspointConfig" value="false" />
     <long-array name="RoamingConsortiumOIs" num="0" />
     <string name="RandomizedMacAddress">02:00:00:00:00:00</string>
     </WifiConfiguration>


     */
    private static List<WifiInfo> xmlParse(InputStream inStream)
            throws Exception {
        WifiInfo wifiInfo = null;
        List<WifiInfo> wifiInfos = null;
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inStream, "UTF-8");
        int event = pullParser.getEventType();// 觸發第一個事件
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_DOCUMENT:
                    wifiInfos = new ArrayList<WifiInfo>();
                    break;
                case XmlPullParser.START_TAG:
                    Log.i("TAG", "xmlParse: pullParser.getName()=" + pullParser.getName() + ",getAttributeCount=" + pullParser.getAttributeCount());
                    if ("WifiConfiguration".equals(pullParser.getName())) {
                        wifiInfo = new WifiInfo();
                    }

                    if ("string".equals(pullParser.getName()) && pullParser.getAttributeCount() > 0) {
                        String attributeName = pullParser.getAttributeName(0);
                        String attributeValue = pullParser.getAttributeValue(0);
                        Log.i("TAG", "xmlParse: string, attributeName="+attributeName + ",attributeValue=" +attributeValue);
                        if (attributeValue.equals("SSID")) {
                            wifiInfo.Ssid = pullParser.nextText();
                        } else if (attributeName.equals("PreSharedKey")) {
                            wifiInfo.Password = pullParser.nextText();
                        }else if (attributeName.equals("PreSharedKey")) {
                            wifiInfo.Password = pullParser.nextText();
                        }
                    }


                    break;
                case XmlPullParser.END_TAG:
                    if ("WifiConfiguration".equals(pullParser.getName())) {
                        if (wifiInfo != null){
                            wifiInfos.add(wifiInfo);
                        }
                        wifiInfo = null;
                    }
                    break;
            }
            event = pullParser.next();
        }
        return wifiInfos;
    }

}
