package dartagnan.program.event.linux.rmw;

import com.google.common.collect.ImmutableSet;
import dartagnan.expression.Atom;
import dartagnan.expression.ExprInterface;
import dartagnan.expression.IExpr;
import dartagnan.expression.IExprBin;
import dartagnan.expression.op.COpBin;
import dartagnan.expression.op.IOpBin;
import dartagnan.program.Register;
import dartagnan.program.Seq;
import dartagnan.program.Thread;
import dartagnan.program.event.Local;
import dartagnan.program.event.rmw.cond.RMWReadCondUnless;
import dartagnan.program.event.rmw.cond.RMWStoreCond;
import dartagnan.program.event.utils.RegReaderData;
import dartagnan.program.event.utils.RegWriter;

public class RMWAddUnless extends RMWAbstract implements RegWriter, RegReaderData {

    private ExprInterface cmp;

    public RMWAddUnless(IExpr address, Register register, ExprInterface cmp, ExprInterface value) {
        super(address, register, value, "Mb");
        this.dataRegs = new ImmutableSet.Builder<Register>().addAll(value.getRegs()).addAll(cmp.getRegs()).build();
        this.cmp = cmp;
    }

    @Override
    public Thread compile(String target, boolean ctrl, boolean leading) {
        if(target.equals("sc")) {
            Register dummy = new Register(null);
            RMWReadCondUnless load = new RMWReadCondUnless(dummy, cmp, address, "Relaxed");
            RMWStoreCond store = new RMWStoreCond(load, address, new IExprBin(dummy, IOpBin.PLUS, value), "Relaxed");
            Local local = new Local(resultRegister, new Atom(dummy, COpBin.NEQ, cmp));

            compileBasic(load);
            compileBasic(store);

            Thread result = new Seq(load, new Seq(store, local));
            return insertCondFencesOnMb(result, load);
        }
        return super.compile(target, ctrl, leading);
    }

    @Override
    public String toString() {
        return nTimesCondLevel() + resultRegister + " := atomic_add_unless" + "(" + address + ", " + value + ", " + cmp + ")";
    }

    @Override
    public RMWAddUnless clone() {
        if(clone == null){
            Register newReg = resultRegister.clone();
            ExprInterface newValue = resultRegister == value ? newReg : value.clone();
            ExprInterface newCmp = resultRegister == cmp ? newReg : ((value == cmp) ? newValue : cmp.clone());
            clone = new RMWAddUnless(address.clone(), newReg, newCmp, newValue);
            afterClone();
        }
        return (RMWAddUnless)clone;
    }
}