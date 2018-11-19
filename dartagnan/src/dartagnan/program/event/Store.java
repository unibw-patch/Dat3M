package dartagnan.program.event;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import dartagnan.expression.ExprInterface;
import dartagnan.program.Location;
import dartagnan.program.Register;
import dartagnan.program.utils.EType;
import dartagnan.utils.MapSSA;
import dartagnan.utils.Pair;

import static dartagnan.utils.Utils.ssaLoc;

public class Store extends MemEvent {

	protected ExprInterface value;

	public Store(Location loc, ExprInterface value, String atomic) {
		this.value = value;
		this.loc = loc;
		this.atomic = atomic;
		this.condLevel = 0;
		addFilters(EType.ANY, EType.MEMORY, EType.WRITE);
	}

	@Override
	public Register getReg() {
		if(value instanceof Register){
			return (Register)value;
		}
		return null;
	}

	@Override
	public String toString() {
        return nTimesCondLevel() + loc + " := " + value;
	}

	@Override
	public String label(){
		return "W[" + atomic + "] " + loc;
	}

	@Override
	public ExprInterface getExpr(){
		return value;
	}

	@Override
	public Store clone() {
        Location newLoc = loc.clone();
        ExprInterface newVal = value.clone();
		Store newStore = new Store(newLoc, newVal, atomic);
		newStore.condLevel = condLevel;
		newStore.setHLId(getHLId());
		newStore.setUnfCopy(getUnfCopy());
		return newStore;
	}

	@Override
	public Pair<BoolExpr, MapSSA> encodeDF(MapSSA map, Context ctx) {
		if(mainThread != null){
			Expr z3Expr = value.toZ3(map, ctx);
			Expr z3Loc = ssaLoc(loc, mainThread.getTId(), map.getFresh(loc), ctx);
			this.ssaLoc = z3Loc;
			return new Pair<>(ctx.mkImplies(executes(ctx), value.encodeAssignment(map, ctx, z3Loc, z3Expr)), map);
		}
		throw new RuntimeException("Main thread is not set for " + toString());
	}
}