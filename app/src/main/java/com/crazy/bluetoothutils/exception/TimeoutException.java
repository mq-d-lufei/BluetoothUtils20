package com.crazy.bluetoothutils.exception;


public class TimeoutException extends BltException {

    public TimeoutException() {
        super(ERROR_CODE_TIMEOUT, "Timeout Exception Occurred!");
    }

}
