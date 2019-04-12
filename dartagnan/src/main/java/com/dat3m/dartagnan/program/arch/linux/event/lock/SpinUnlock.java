package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.program.arch.linux.event.lock.utils.State;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.arch.linux.utils.Mo;

public class SpinUnlock extends LockBase {

    public SpinUnlock(IExpr address) {
        super(address, Mo.RELEASE, new IConst(State.FREE));
        addFilters(EType.ANY, EType.VISIBLE, EType.MEMORY, EType.WRITE, EType.UL);
    }

    private SpinUnlock(SpinUnlock other){
        super(other);
    }

    @Override
    public String toString() {
        return "spin_unlock(*" + address + ")";
    }

    @Override
    public String label(){
        return "Unlock_*" + address;
    }


    // Unrolling
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public SpinUnlock getCopy(){
        return new SpinUnlock(this);
    }
}
