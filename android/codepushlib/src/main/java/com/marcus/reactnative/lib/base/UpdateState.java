package com.marcus.reactnative.lib.base;

/*!
 * Copyright(c) 2009-2017 Marcus Ma
 * E-mail:maji1991@sina.com
 * GitHub : https://github.com/MarcusMa
 * MIT Licensed
 */

public enum UpdateState {
    NOT_START(0),
    UPDATING(1),
    UPDATED_FAILED(2),
    UPDATED_SUCCESS(3);

    private final int value;

    UpdateState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
