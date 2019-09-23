package com.crazy.bluetoothutils.connect.delegate;


import com.crazy.bluetoothutils.connect.bltclient.BltServerSocketClient;
import com.crazy.bluetoothutils.connect.bltserver.BltSocketServer;

/**
 * Created by feaoes on 2018/7/3.
 */


public interface BltSocketServerDelegate {

    void onServerBeginListen(BltSocketServer socketServer);


    void onServerStopListen(BltSocketServer socketServer, boolean isDisconnectAllServerClients);

    void onClientConnected(BltSocketServer socketServer, BltServerSocketClient socketServerClient);

    void onClientDisconnected(BltSocketServer socketServer, BltServerSocketClient socketServerClient);

    class SimpleBltSocketServerDelegate implements BltSocketServerDelegate {
        @Override
        public void onServerBeginListen(BltSocketServer socketServer) {

        }

        @Override
        public void onServerStopListen(BltSocketServer socketServer,boolean isDisconnectAllServerClients) {

        }

        @Override
        public void onClientConnected(BltSocketServer socketServer, BltServerSocketClient socketServerClient) {

        }

        @Override
        public void onClientDisconnected(BltSocketServer socketServer, BltServerSocketClient socketServerClient) {

        }
    }
}
