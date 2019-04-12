package com.dat3m.dartagnan.program.arch.linux.event.lock;

import com.dat3m.dartagnan.expression.ExprInterface;
import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.expression.IExpr;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.microsoft.z3.Context;

abstract class LockBase extends MemEvent {

    private final IConst memValue;

    LockBase(IExpr address, String mo, IConst memValue) {
        super(address, mo);
        this.memValue = memValue;
    }

    LockBase(LockBase other){
        super(other);
        this.memValue = other.memValue;
    }

    @Override
    public ExprInterface getMemValue(){
        return memValue;
    }

    @Override
    public void initialise(Context ctx) {
        memValueExpr = memValue.toZ3Int(this, ctx);
        memAddressExpr = address.toZ3Int(this, ctx);
    }
}
