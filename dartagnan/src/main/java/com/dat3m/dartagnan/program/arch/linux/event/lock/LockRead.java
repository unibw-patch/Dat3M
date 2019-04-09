package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.ExprInterface;
import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.program.arch.linux.event.lock.utils.State;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.arch.linux.utils.Mo;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.dat3m.dartagnan.wmm.utils.Arch;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

public class LockRead extends MemEvent {

    LockRead(IExpr address) {
        super(address, Mo.ACQUIRE);
        addFilters(EType.ANY, EType.VISIBLE, EType.MEMORY, EType.READ, EType.RMW, EType.LKR);
    }

    @Override
    public String toString() {
        return "lock_read(*" + address + ")";
    }

    @Override
    public String label(){
        return "LKR_*" + address;
    }

    @Override
    public ExprInterface getMemValue(){
        return new IConst(State.FREE);
    }

    @Override
    public void initialise(Context ctx) {
        memValueExpr = new IConst(State.FREE).toZ3Int(this, ctx);
        memAddressExpr = address.toZ3Int(this, ctx);
    }


    // Unrolling
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public int unroll(int bound, int nextId, Event predecessor) {
        throw new RuntimeException("LockRead cannot be unrolled: event must be generated during compilation");
    }


    // Compilation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public int compile(Arch target, int nextId, Event predecessor) {
        throw new RuntimeException("LockRead cannot be compiled: event must be generated during compilation");
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
            cfEnc = ctx.mkAnd(cfEnc, successor.encodeCF(ctx, executes(ctx)));
        }
        return cfEnc;
    }
}
