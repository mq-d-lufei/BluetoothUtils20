package com.crazy.bluetoothutils.exception;


public class BltSendException extends BltSocketException {

    public BltSendException(String description) {
        super("[ Send Exception: " + description + " ]");
    }
}
