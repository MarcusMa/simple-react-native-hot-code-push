package com.marcus.lib.codepush.base;

/**
 * Created by marcus on 17/4/29.
 */

public class MMErrorCode {
    public static final int SUCCESS = 1000;

    /*
    Local Error
     */
    public static final int PARAMS_ERROR = 2000;
    public static final int NEED_INIT_FIRST_ERROR = 2001;
    public static final int IO_ERROR = 2002;
    /*
    Remote Error
     */
    public static final int NETWORK_ERROR = 3001;
    public static final int NETWORK_RESPONSE_ERROR = 3002;
    public static final int URL_ERROR = 3003;
    public static final int CHECK_HASH_ERROR = 3004;


}
