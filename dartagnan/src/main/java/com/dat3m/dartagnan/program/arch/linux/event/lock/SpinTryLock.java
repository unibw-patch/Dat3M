package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.Atom;
import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.expression.op.COpBin;
import com.dat3m.dartagnan.program.Register;
import com.dat3m.dartagnan.program.arch.linux.event.lock.utils.State;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.arch.linux.utils.Mo;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.Local;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.dat3m.dartagnan.program.event.utils.RegWriter;
import com.dat3m.dartagnan.wmm.utils.Arch;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import java.util.LinkedList;

public class SpinTryLock extends MemEvent implements RegWriter {

    private final Register resultRegister;

    public SpinTryLock(Register register, IExpr address) {
        super(address, Mo.RELAXED);
        this.resultRegister = register;
        addFilters(EType.ANY, EType.MEMORY, EType.READ, EType.WRITE, EType.RMW,
                EType.LF, EType.LKR, EType.LKW, EType.REG_WRITER);
    }

    private SpinTryLock(SpinTryLock other) {
        super(other);
        this.resultRegister = other.resultRegister;
    }

    @Override
    public Register getResultRegister(){
        return resultRegister;
    }

    @Override
    public String toString() {
        return resultRegister + " = spin_try_lock(*" + address + ")";
    }

    @Override
    public String label(){
        return "TryLock_*" + address;
    }


    // Unrolling
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public SpinTryLock getCopy(){
        return new SpinTryLock(this);
    }


    // Compilation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public int compile(Arch target, int nextId, Event predecessor) {
        if(target == Arch.NONE) {
            LockReadFailed lockReadFailed = new LockReadFailed(resultRegister, address);
            LockRead lockRead = new LockRead(lockReadFailed, resultRegister, address);
            LinkedList<Event> events = new LinkedList<>();
            events.add(lockReadFailed);
            events.add(lockRead);
            events.add(new Local(resultRegister, new Atom(resultRegister, COpBin.EQ, new IConst(State.FREE))));
            events.add(new LockWrite(lockRead, address));
            return compileSequence(target, nextId, predecessor, events);
        }
        throw new RuntimeException("Compilation to " + target + " is not supported for " + getClass().getName());
    }


    // Encoding
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public BoolExpr encodeCF(Context ctx, BoolExpr cond) {
        throw new RuntimeException("SpinTryLock cannot be encoded: event must be compiled into" +
                " a triple of LockReadFailed, LockRead and LockWrite");
    }
}
