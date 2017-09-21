package com.marcus.lib.codepush.base;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public enum CheckUpdateState {
    NOT_START(0),
    WAITING_FOR_RESPONSE(1),
    CHECK_UPDATE_FAILED(2),
    CHECK_UPDATE_SUCCESS(3);

    private final int value;

    CheckUpdateState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
