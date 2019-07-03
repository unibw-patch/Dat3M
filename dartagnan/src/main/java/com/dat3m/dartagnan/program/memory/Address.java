package com.dat3m.dartagnan.program.memory;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;

import static java.util.UUID.randomUUID;

import java.util.UUID;

import com.dat3m.dartagnan.expression.ExprInterface;
import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.program.Register;
import com.dat3m.dartagnan.program.event.Event;

public class Address extends IConst implements ExprInterface {

    private final int index;
	private final UUID uuid = randomUUID();

    Address(int index){
        super(index);
        this.index = index;
    }

    public UUID getUUID() {
    	return uuid;
    }
    
    @Override
    public ImmutableSet<Register> getRegs(){
        return ImmutableSet.of();
    }

    @Override
    public IntExpr toZ3Int(Event e, Context ctx){
        return toZ3Int(ctx);
    }

    @Override
    public IntExpr getLastValueExpr(Context ctx){
        return toZ3Int(ctx);
    }

    public IntExpr getLastMemValueExpr(Context ctx){
//        return ctx.mkIntConst("last_val_at_memory_" + uuid + "_" + index);
        return ctx.mkIntConst("last_val_at_memory_" + index);
    }

    @Override
    public BoolExpr toZ3Bool(Event e, Context ctx){
        return ctx.mkTrue();
    }

    @Override
    public String toString(){
        return "&mem" + index;
    }

    @Override
    public int hashCode(){
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        return index == ((Address)obj).index;
    }

    @Override
    public IntExpr toZ3Int(Context ctx){
//        return ctx.mkIntConst("memory_" + uuid + "_" + index);
        return ctx.mkIntConst("memory_" + index);
    }

    @Override
    public int getIntValue(Event e, Context ctx, Model model){
        return Integer.parseInt(model.getConstInterp(toZ3Int(ctx)).toString());
    }    
}
