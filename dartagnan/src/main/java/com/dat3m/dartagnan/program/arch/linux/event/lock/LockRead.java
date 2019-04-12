package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.ExprInterface;
import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.program.Register;
import com.dat3m.dartagnan.program.arch.linux.event.lock.utils.State;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.arch.linux.utils.Mo;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.dat3m.dartagnan.program.event.utils.EventWithPartner;
import com.dat3m.dartagnan.program.event.utils.RegWriter;
import com.dat3m.dartagnan.wmm.utils.Arch;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;

public class LockRead extends MemEvent implements RegWriter, EventWithPartner {

    private final LockReadFailed failedEvent;
    private final Register resultRegister;

    LockRead(LockReadFailed failedEvent, Register register, IExpr address) {
        super(address, Mo.ACQUIRE);
        this.failedEvent = failedEvent;
        this.resultRegister = register;
        addFilters(EType.ANY, EType.VISIBLE, EType.MEMORY, EType.READ, EType.RMW, EType.LKR, EType.REG_WRITER);
    }

    @Override
    public Event getPartner(){
        return failedEvent;
    }

    @Override
    public Register getResultRegister(){
        return resultRegister;
    }

    @Override
    public String toString() {
        if(failedEvent != null){
            return resultRegister + " = lock_read(*" + address + ")";
        }
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

    @Override
    public IntExpr getResultRegisterExpr(){
        return memValueExpr;
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
            if(failedEvent != null){
                cfEnc = ctx.mkAnd(cfEnc, ctx.mkImplies(ctx.mkBoolConst(cfVar()),
                        ctx.mkEq(executes(ctx), ctx.mkNot(failedEvent.executes(ctx)))
                ));
                cfEnc = ctx.mkAnd(cfEnc, successor.encodeCF(ctx, ctx.mkBoolConst(cfVar())));
            } else {
                cfEnc = ctx.mkAnd(cfEnc, successor.encodeCF(ctx, executes(ctx)));
            }
        }
        return cfEnc;
    }
}
