package dartagnan.wmm.relation.basic;

import dartagnan.program.Thread;
import dartagnan.program.event.Event;
import dartagnan.program.event.MemEvent;
import dartagnan.program.event.utils.RegWriter;
import dartagnan.program.utils.EventRepository;
import dartagnan.wmm.utils.Tuple;
import dartagnan.wmm.utils.TupleSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RelAddrDirect extends BasicRelation {

    public RelAddrDirect(){
        term = "addrDirect";
    }

    @Override
    public TupleSet getMaxTupleSet(){
        if(maxTupleSet == null){
            maxTupleSet = new TupleSet();
            for(Thread t : program.getThreads()){
                List<Event> events = t.getEventRepository().getEvents(EventRepository.ALL);
                Set<Event> regWriters = events.stream().filter(e -> e instanceof RegWriter).collect(Collectors.toSet());
                List<Event> regReaders = t.getEventRepository().getEvents(EventRepository.MEMORY);
                for(Event e1 : regWriters){
                    for(Event e2 : regReaders){
                        if(e1.getEId() < e2.getEId() && ((MemEvent)e2).getAddressReg() == ((RegWriter)e1).getModifiedReg()){
                            maxTupleSet.add(new Tuple(e1, e2));
                        }
                    }
                }
            }
        }
        return maxTupleSet;
    }
}
