package com.crazy.bluetoothutils.exception;


public class BltSocketException extends BltException {

    public BltSocketException(String description) {
        super(ERROR_CODE_SOCKET, "Socket Exception: " + description);
    }
}
