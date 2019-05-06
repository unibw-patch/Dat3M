package com.dat3m.dartagnan.program.arch.linux.event.lock.utils;

import com.dat3m.dartagnan.program.event.Event;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

public class Utils {

    public static BoolExpr isLockObtainedVar(Event e, Context ctx){
        return ctx.mkBoolConst("LOCK_OBTAINED(E" + e.getCId() + ")");
    }

    public static BoolExpr isLockConsumedVar(Event e, Context ctx){
        return ctx.mkBoolConst("LOCK_CONSUMED(E" + e.getCId() + ")");
    }
}
