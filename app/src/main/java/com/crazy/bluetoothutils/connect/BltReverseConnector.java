package com.crazy.bluetoothutils.connect;

import android.support.annotation.NonNull;

import com.crazy.bluetoothutils.connect.bltserver.BltSocketServer;
import com.crazy.bluetoothutils.connect.delegate.BltSocketServerDelegate;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Created by Crazy on 2018/8/3.
 */
public class BltReverseConnector {

    private BltReverseConnector() {
    }

    private static class BltReverseConnectorHolder {
        private static final BltReverseConnector INSTANCE = new BltReverseConnector();
    }

    public static BltReverseConnector getInstance() {
        return BltReverseConnector.BltReverseConnectorHolder.INSTANCE;
    }

    public HashMap<String, BltSocketServer> bltSocketServerMap = new HashMap<>();

    public boolean beginListen(@NonNull BltConnectRuleConfig bltConnectRuleConfig, BltSocketServerDelegate bltSocketServerDelegate) {

        if (null == bltConnectRuleConfig.getName()) {
            throw new IllegalArgumentException("bltConnectRuleConfig.getName() can not be Null!");
        }

        if (null == bltConnectRuleConfig.getUuid()) {
            throw new IllegalArgumentException("bltConnectRuleConfig.getUuid() can not be Null!");
        }

        if (null == bltConnectRuleConfig.getSecureType()) {
            throw new IllegalArgumentException("bltConnectRuleConfig.getSecureType() can not be Null!");
        }

        if (isSocketServerExist(bltConnectRuleConfig)) {
            return bltSocketServerMap.get(bltConnectRuleConfig.getName()).beginListen();
        }

        BltSocketServer bltSocketServer = new BltSocketServer(bltConnectRuleConfig);
        bltSocketServer.registerBltSocketServerDelegate(bltSocketServerDelegate);
        bltSocketServerMap.put(bltConnectRuleConfig.getName(), bltSocketServer);

        return bltSocketServer.beginListen();
    }

    public void stopListen(@NonNull BltConnectRuleConfig bltConnectRuleConfig) {
        if (bltSocketServerMap.containsKey(bltConnectRuleConfig.getName())) {
            bltSocketServerMap.get(bltConnectRuleConfig.getName()).stopListen();
            bltSocketServerMap.remove(bltConnectRuleConfig.getName());
        }
    }

    public void stopListen(@NonNull BltConnectRuleConfig bltConnectRuleConfig, boolean isDisconnectAllServerClients) {
        if (bltSocketServerMap.containsKey(bltConnectRuleConfig.getName())) {
            bltSocketServerMap.get(bltConnectRuleConfig.getName()).stopListen(isDisconnectAllServerClients);
            bltSocketServerMap.remove(bltConnectRuleConfig.getName());
        }
    }

    public void stopAllListen() {
        for (Map.Entry<String, BltSocketServer> bltSocketServerEntry : bltSocketServerMap.entrySet()) {
            BltSocketServer bltSocketServer = bltSocketServerEntry.getValue();
            bltSocketServer.stopListen();
        }
        bltSocketServerMap.clear();
    }

    private boolean isSocketServerExist(@NonNull BltConnectRuleConfig bltConnectRuleConfig) {
        if (bltSocketServerMap.containsKey(bltConnectRuleConfig.getName())) {
            return true;
        }
        for (Map.Entry<String, BltSocketServer> bltSocketServerEntry : bltSocketServerMap.entrySet()) {
            BltSocketServer bltSocketServer = bltSocketServerEntry.getValue();
            if (bltConnectRuleConfig.equals(bltSocketServer.getBltConnectRuleConfig())) {
                return true;
            }
        }
        return false;
    }
}
