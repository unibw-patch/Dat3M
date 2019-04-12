package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.program.Register;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.arch.linux.utils.Mo;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.dat3m.dartagnan.program.event.utils.RegWriter;
import com.dat3m.dartagnan.wmm.utils.Arch;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import java.util.LinkedList;

public class SpinLock extends MemEvent implements RegWriter {

    private final Register resultRegister;

    public SpinLock(Register register, IExpr address) {
        super(address, Mo.ACQUIRE);
        this.resultRegister = register;
        addFilters(EType.ANY, EType.MEMORY, EType.RMW, EType.READ, EType.WRITE, EType.LKR, EType.LKW, EType.REG_WRITER);
    }

    private SpinLock(SpinLock other) {
        super(other);
        this.resultRegister = other.resultRegister;
    }

    @Override
    public Register getResultRegister(){
        return resultRegister;
    }

    @Override
    public String toString() {
        return "spin_lock(*" + address + ")";
    }

    @Override
    public String label(){
        return "Lock_*" + address;
    }


    // Unrolling
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public SpinLock getCopy(){
        return new SpinLock(this);
    }


    // Compilation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public int compile(Arch target, int nextId, Event predecessor) {
        if(target == Arch.NONE) {
            LockRead lockRead = new LockRead(null, resultRegister, address);
            LinkedList<Event> events = new LinkedList<>();
            events.add(lockRead);
            events.add(new LockWrite(lockRead, address));
            return compileSequence(target, nextId, predecessor, events);
        }
        throw new RuntimeException("Compilation to " + target + " is not supported for " + getClass().getName());
    }


    // Encoding
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public BoolExpr encodeCF(Context ctx, BoolExpr cond) {
        throw new RuntimeException("SpinIsLocked cannot be encoded: event must be compiled into"
                + " a pair of LockRead and LockWrite");
    }
}
