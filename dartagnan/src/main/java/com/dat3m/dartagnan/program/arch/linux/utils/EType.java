package com.dat3m.dartagnan.program.arch.linux.utils;

public class EType extends com.dat3m.dartagnan.program.utils.EType {

    public static final String NORETURN     = "Noreturn";
    public static final String RCU_SYNC     = "Sync-rcu";
    public static final String RCU_LOCK     = "Rcu-lock";
    public static final String RCU_UNLOCK   = "Rcu-unlock";

    public static final String LKR          = "LKR";    // Lock acquire read partner (blocking and non-blocking)
    public static final String LKW          = "LKW";    // Lock acquire write partner
    public static final String UL           = "UL";     // Lock release
    public static final String LF           = "LF";     // Lock read failed (read taken state)
    public static final String RU           = "RU";     // lock read success (read free state, do not take lock)
    public static final String BL           = "BL";     // Lock read blocking (subset of LKR)
}
