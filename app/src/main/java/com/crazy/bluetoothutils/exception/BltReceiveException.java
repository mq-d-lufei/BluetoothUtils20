package com.crazy.bluetoothutils.exception;


public class BltReceiveException extends BltSocketException {

    public BltReceiveException(String description) {
        super("[ Receive Exception: " + description + " ]");
    }
}
