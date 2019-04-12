package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.program.arch.linux.event.lock.utils.State;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.arch.linux.utils.Mo;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.utils.EventWithPartner;
import com.dat3m.dartagnan.wmm.utils.Arch;

// The write partner of spin_lock() and spin_trylock()
public class LockWrite extends LockBase implements EventWithPartner {

    private final Event lockRead;

    LockWrite(Event lockRead, IExpr address) {
        super(address, Mo.RELAXED, new IConst(State.TAKEN));
        this.lockRead = lockRead;
        addFilters(EType.ANY, EType.VISIBLE, EType.MEMORY, EType.WRITE, EType.RMW, EType.LKW);
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
    public Event getPartner(){
        return lockRead;
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
