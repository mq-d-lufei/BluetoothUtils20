package com.crazy.bluetoothutils.exception;

import java.io.Serializable;


public abstract class BltException implements Serializable {

    private static final long serialVersionUID = 8004414918500865564L;

    public static final int ERROR_CODE_SOCKET = 100;
    public static final int ERROR_CODE_TIMEOUT = 101;
    public static final int ERROR_CODE_OTHER = 102;

    private int code;
    private String description;

    public BltException(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public BltException setCode(int code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public BltException setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "BltException { " +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
