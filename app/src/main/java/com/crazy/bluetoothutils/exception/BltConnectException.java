package com.crazy.bluetoothutils.exception;


public class BltConnectException extends BltSocketException {

    public BltConnectException() {
        super("Connection Failure !!!");
    }

    public BltConnectException(String description) {
        super(description);
    }
}
