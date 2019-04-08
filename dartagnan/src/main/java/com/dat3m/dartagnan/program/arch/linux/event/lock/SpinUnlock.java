package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.ExprInterface;
import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.arch.linux.utils.Mo;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.microsoft.z3.Context;

public class SpinUnlock extends MemEvent {

    public SpinUnlock(IExpr address) {
        super(address, Mo.RELEASE);
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

    @Override
    public ExprInterface getMemValue(){
        return new IConst(0);
    }

    @Override
    public void initialise(Context ctx) {
        memValueExpr = new IConst(0).toZ3Int(this, ctx);
        memAddressExpr = address.toZ3Int(this, ctx);
    }


    // Unrolling
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public SpinUnlock getCopy(){
        return new SpinUnlock(this);
    }
}
