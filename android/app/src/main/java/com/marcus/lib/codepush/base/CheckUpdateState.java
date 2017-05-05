package com.marcus.lib.codepush.base;

/**
 * Created by marcus on 17/4/10.
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
