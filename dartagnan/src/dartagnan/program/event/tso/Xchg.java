package dartagnan.program.event.tso;

import dartagnan.program.Register;
import dartagnan.program.Thread;
import dartagnan.program.event.Local;
import dartagnan.program.event.MemEvent;
import dartagnan.program.event.rmw.RMWLoad;
import dartagnan.program.event.rmw.RMWStore;
import dartagnan.program.event.utils.RegReaderData;
import dartagnan.program.event.utils.RegWriter;
import dartagnan.program.memory.Address;
import dartagnan.program.utils.tso.EType;

import java.util.Set;

public class Xchg extends MemEvent implements RegWriter, RegReaderData {

    private Register resultRegister;

    public Xchg(Address address, Register register, String atomic) {
        this.address = address;
        this.resultRegister = register;
        this.atomic = atomic;
        this.condLevel = 0;
        addFilters(EType.ANY, EType.MEMORY, EType.READ, EType.WRITE, EType.ATOM);
    }

    @Override
    public Register getResultRegister(){
        return resultRegister;
    }

    @Override
    public Set<Register> getDataRegs(){
        return resultRegister.getRegs();
    }

    @Override
    public Thread compile(String target, boolean ctrl, boolean leading) {
        if(target.equals("tso") && atomic.equals("_rx")) {
            Register dummyReg = new Register(null);
            RMWLoad load = new RMWLoad(dummyReg, address, atomic);
            load.setHLId(hlId);
            load.setCondLevel(condLevel);
            load.addFilters(EType.ATOM);
            load.setMaxAddressSet(maxAddressSet);

            RMWStore store = new RMWStore(load, address, resultRegister, atomic);
            store.setHLId(hlId);
            store.setCondLevel(condLevel);
            store.addFilters(EType.ATOM);
            store.setMaxAddressSet(maxAddressSet);

            return Thread.fromArray(false, load, store, new Local(resultRegister, dummyReg));
        }
        throw new RuntimeException("xchg " + atomic + " is not implemented for " + target);
    }

    @Override
    public String toString() {
        return nTimesCondLevel() + "xchg(*" + address + ", " + resultRegister + ", " + "atomic)";
    }

    @Override
    public Xchg clone() {
        if(clone == null){
            clone= new Xchg((Address) address.clone(), resultRegister.clone(), atomic);
            afterClone();
        }
        return (Xchg)clone;
    }
}
