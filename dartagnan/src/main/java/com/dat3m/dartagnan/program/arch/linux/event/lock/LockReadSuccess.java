package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.program.Register;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.event.utils.RegWriter;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;

// The successful (read from a free lock) partner of spin_trylock()
public class LockReadSuccess extends LockRead implements RegWriter {

    private final LockReadFailed failedEvent;
    private final Register resultRegister;

    LockReadSuccess(LockReadFailed failedEvent, Register register, IExpr address) {
        super(address);
        this.failedEvent = failedEvent;
        this.resultRegister = register;
        addFilters(EType.REG_WRITER);
    }

    @Override
    public String toString() {
        return resultRegister + " = lock_read(*" + address + ")";
    }

    @Override
    public Register getResultRegister(){
        return resultRegister;
    }

    @Override
    public IntExpr getResultRegisterExpr(){
        return memValueExpr;
    }


    // Encoding
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public BoolExpr encodeCF(Context ctx, BoolExpr cond) {
        if(cfEnc == null){
            if(successor == null){
                throw new RuntimeException("Malformed spinlock");
            }
            cfCond = (cfCond == null) ? cond : ctx.mkOr(cfCond, cond);
            cfEnc = ctx.mkEq(ctx.mkBoolConst(cfVar()), cfCond);
            cfEnc = ctx.mkAnd(cfEnc, ctx.mkImplies(executes(ctx), ctx.mkBoolConst(cfVar())));
            cfEnc = ctx.mkAnd(cfEnc, ctx.mkImplies(ctx.mkBoolConst(cfVar()),
                    ctx.mkEq(executes(ctx), ctx.mkNot(failedEvent.executes(ctx)))
            ));
            cfEnc = ctx.mkAnd(cfEnc, successor.encodeCF(ctx, ctx.mkBoolConst(cfVar())));
        }
        return cfEnc;
    }
}
