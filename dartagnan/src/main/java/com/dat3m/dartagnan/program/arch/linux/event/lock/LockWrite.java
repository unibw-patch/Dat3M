package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.ExprInterface;
import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.program.arch.linux.event.lock.utils.State;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.arch.linux.utils.Mo;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.dat3m.dartagnan.program.event.utils.EventWithPartner;
import com.dat3m.dartagnan.wmm.utils.Arch;
import com.microsoft.z3.Context;

public class LockWrite extends MemEvent implements EventWithPartner {

    private final LockRead lockRead;

    LockWrite(LockRead lockRead, IExpr address) {
        super(address, Mo.RELAXED);
        this.lockRead = lockRead;
        addFilters(EType.ANY, EType.VISIBLE, EType.MEMORY, EType.WRITE, EType.RMW, EType.LKW);
    }

    @Override
    public LockRead getPartner(){
        return lockRead;
    }

    @Override
    public String toString() {
        return "lock_write(*" + address + ")";
    }

    @Override
    public String label(){
        return "LKW_*" + address;
    }

    @Override
    public ExprInterface getMemValue(){
        return new IConst(State.TAKEN);
    }

    @Override
    public void initialise(Context ctx) {
        memValueExpr = new IConst(State.TAKEN).toZ3Int(this, ctx);
        memAddressExpr = address.toZ3Int(this, ctx);
    }


    // Unrolling
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public int unroll(int bound, int nextId, Event predecessor) {
        throw new RuntimeException("LockWrite cannot be unrolled: event must be generated during compilation");
    }


    // Compilation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public int compile(Arch target, int nextId, Event predecessor) {
        throw new RuntimeException("LockWrite cannot be compiled: event must be generated during compilation");
    }
}
