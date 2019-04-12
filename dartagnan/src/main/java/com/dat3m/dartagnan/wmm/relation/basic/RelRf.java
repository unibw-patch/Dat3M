package com.dat3m.dartagnan.wmm.relation.basic;

import com.dat3m.dartagnan.program.arch.linux.event.lock.utils.State;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.event.Init;
import com.dat3m.dartagnan.wmm.filter.FilterBasic;
import com.dat3m.dartagnan.wmm.filter.FilterMinus;
import com.microsoft.z3.BoolExpr;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.dat3m.dartagnan.wmm.relation.Relation;
import com.dat3m.dartagnan.wmm.utils.Tuple;
import com.dat3m.dartagnan.wmm.utils.TupleSet;

import java.util.*;

import static com.dat3m.dartagnan.utils.Utils.edge;

public class RelRf extends Relation {

    public RelRf(){
        term = "rf";
        forceDoEncode = true;
    }

    @Override
    public TupleSet getMaxTupleSet(){
        if(maxTupleSet == null){
            maxTupleSet = new TupleSet();

            List<Event> eventsLoad = program.getCache().getEvents(FilterBasic.get(EType.READ));
            List<Event> eventsInit = program.getCache().getEvents(FilterBasic.get(EType.INIT));
            List<Event> eventsStore = program.getCache().getEvents(FilterMinus.get(
                    FilterBasic.get(EType.WRITE),
                    FilterBasic.get(EType.INIT)
            ));

            for(Event e1 : eventsInit){
                for(Event e2 : eventsLoad){
                    if(MemEvent.canAddressTheSameLocation((MemEvent) e1, (MemEvent) e2)){
                        maxTupleSet.add(new Tuple(e1, e2));
                    }
                }
            }

            for(Event e1 : eventsStore){
                for(Event e2 : eventsLoad){
                    if(MemEvent.canAddressTheSameLocation((MemEvent) e1, (MemEvent) e2)){
                        maxTupleSet.add(new Tuple(e1, e2));
                    }
                }
            }

            maxTupleSet.removeAll(getIllegalLockTuples(maxTupleSet));
        }
        return maxTupleSet;
    }

    @Override
    protected BoolExpr encodeApprox() {
        BoolExpr enc = ctx.mkTrue();
        Map<MemEvent, List<BoolExpr>> rfMap = new HashMap<>();

        for(Tuple tuple : maxTupleSet){
            MemEvent w = (MemEvent) tuple.getFirst();
            MemEvent r = (MemEvent) tuple.getSecond();
            BoolExpr rel = edge("rf", w, r, ctx);
            rfMap.putIfAbsent(r, new ArrayList<>());
            rfMap.get(r).add(rel);

            enc = ctx.mkAnd(enc, ctx.mkImplies(rel, ctx.mkAnd(
                    ctx.mkAnd(w.executes(ctx), r.executes(ctx)),
                    ctx.mkAnd(
                            ctx.mkEq(w.getMemAddressExpr(), r.getMemAddressExpr()),
                            ctx.mkEq(w.getMemValueExpr(), r.getMemValueExpr())
                    )
            )));
        }

        for(MemEvent r : rfMap.keySet()){
            enc = ctx.mkAnd(enc, ctx.mkImplies(r.executes(ctx), encodeEO(r.getCId(), rfMap.get(r))));
        }

        return ctx.mkAnd(enc, encodeLockConstraints());
    }

    private BoolExpr encodeEO(int readId, List<BoolExpr> set){
        int num = set.size();

        BoolExpr enc = ctx.mkEq(mkL(readId, 0), ctx.mkFalse());
        BoolExpr atLeastOne = set.get(0);

        for(int i = 1; i < num; i++){
            enc = ctx.mkAnd(enc, ctx.mkEq(mkL(readId, i), ctx.mkOr(mkL(readId, i - 1), set.get(i - 1))));
            enc = ctx.mkAnd(enc, ctx.mkNot(ctx.mkAnd(set.get(i), mkL(readId, i))));
            atLeastOne = ctx.mkOr(atLeastOne, set.get(i));
        }
        return ctx.mkAnd(enc, atLeastOne);
    }

    private BoolExpr mkL(int readId, int i) {
        return (BoolExpr) ctx.mkConst("l(" + readId + "," + i + ")", ctx.mkBoolSort());
    }

    private TupleSet getIllegalLockTuples(TupleSet tupleSet){
        TupleSet result = new TupleSet();
        for(Tuple tuple : tupleSet){
            Event e1 = tuple.getFirst();
            Event e2 = tuple.getSecond();
            boolean isLegal = true;

            if(e1.is(EType.LKW)){
                isLegal = e2.is(EType.LF);
            } else if(e1.is(EType.UL)){
                isLegal = e2.is(EType.LKR) || e2.is(EType.RU);
            } else if(e1.is(EType.INIT)){
                if(e2.is(EType.LF) || e2.is(EType.LKR) || e2.is(EType.RU)){
                    try {
                        int value = Integer.parseInt(((Init)tuple.getFirst()).getValue().toString());
                        isLegal = (!e2.is(EType.LF) || value == State.TAKEN)
                                && ((!e2.is(EType.LKR) && !e2.is(EType.RU)) || value == State.FREE);
                    } catch (NumberFormatException e){
                        throw new RuntimeException("Initial value of a lock must be an integer");
                    }
                }
            } else {
                isLegal = !e2.is(EType.LKR) && !e2.is(EType.LF) && !e2.is(EType.RU);
            }

            if(!isLegal){
                result.add(tuple);
            }
        }
        return result;
    }

    private BoolExpr encodeLockConstraints(){
        BoolExpr enc = ctx.mkTrue();
        TupleSet unlockTuples = getUnlockToLockTuples();

        if(!unlockTuples.isEmpty()){
            Set<MemEvent> unlocks = new HashSet<>();
            for(Tuple tuple : unlockTuples){
                unlocks.add((MemEvent) tuple.getFirst());
            }

            for(MemEvent unlock : unlocks){
                List<Tuple> tuples = new ArrayList<>(unlockTuples.getByFirst(unlock));
                if(!tuples.isEmpty()){
                    int num = tuples.size();
                    int unlockId = unlock.getCId();
                    MemEvent lock = (MemEvent)tuples.get(0).getSecond();
                    BoolExpr lastEdge = edge("rf", unlock, lock, ctx);

                    // At most one lock can read from an unlock event
                    BoolExpr atMostOne = ctx.mkEq(mkM(unlockId, 0), ctx.mkFalse());

                    // Exists a lock reading from this unlock
                    BoolExpr atLeastOne = lastEdge;

                    // A (blocking) read is already satisfied or addresses does not match this unlock
                    BoolExpr none = ctx.mkTrue();
                    if(lock.is(EType.BL)) {
                        none = ctx.mkAnd(none, noReadUnlockLock(unlock, lock));
                    }

                    for(int i = 1; i < num; i++){
                        lock = (MemEvent)tuples.get(i).getSecond();
                        BoolExpr edge = edge("rf", unlock, lock, ctx);

                        // At most one lock can read from an unlock event
                        atMostOne = ctx.mkAnd(atMostOne, ctx.mkEq(mkM(unlockId, i), ctx.mkOr(mkM(unlockId, i - 1), lastEdge)));
                        atMostOne = ctx.mkAnd(atMostOne, ctx.mkNot(ctx.mkAnd(edge, mkM(unlockId, i))));

                        // Exists a lock reading from this unlock
                        atLeastOne = ctx.mkOr(atLeastOne, edge);

                        // A (blocking) read is already satisfied or addresses does not match this unlock
                        if(lock.is(EType.BL)) {
                            none = ctx.mkAnd(none, noReadUnlockLock(unlock, lock));
                        }

                        lastEdge = edge;
                    }

                    enc = ctx.mkAnd(enc, atMostOne);
                    enc = ctx.mkAnd(enc, ctx.mkImplies(unlock.executes(ctx), ctx.mkOr(atLeastOne, none)));
                }
            }
        }
        return enc;
    }

    private BoolExpr noReadUnlockLock(MemEvent unlock, MemEvent lock){
        return ctx.mkOr(
                ctx.mkOr(ctx.mkNot(ctx.mkBoolConst(lock.cfVar())), lock.executes(ctx)),
                ctx.mkNot(ctx.mkEq(unlock.getMemAddressExpr(), lock.getMemAddressExpr()))
        );
    }

    private TupleSet getUnlockToLockTuples(){
        TupleSet result = new TupleSet();
        for(Tuple tuple : getMaxTupleSet()){
            if((tuple.getFirst().is(EType.UL) || tuple.getFirst().is(EType.INIT))
                    && tuple.getSecond().is(EType.LKR)){
                result.add(tuple);
            }
        }
        return result;
    }

    private BoolExpr mkM(int unlockId, int i) {
        return (BoolExpr) ctx.mkConst("m(" + unlockId + "," + i + ")", ctx.mkBoolSort());
    }
}
