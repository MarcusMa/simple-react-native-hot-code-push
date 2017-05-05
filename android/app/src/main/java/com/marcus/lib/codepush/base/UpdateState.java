package com.marcus.lib.codepush.base;

/**
 * Created by marcus on 17/3/23.
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
