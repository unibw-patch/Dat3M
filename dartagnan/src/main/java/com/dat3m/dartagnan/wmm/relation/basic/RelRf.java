package com.dat3m.dartagnan.wmm.relation.basic;

import com.dat3m.dartagnan.program.Program;
import com.dat3m.dartagnan.program.arch.linux.event.lock.utils.State;
import com.dat3m.dartagnan.program.arch.linux.event.lock.utils.Utils;
import com.dat3m.dartagnan.program.arch.linux.utils.EType;
import com.dat3m.dartagnan.program.event.Init;
import com.dat3m.dartagnan.wmm.filter.FilterBasic;
import com.dat3m.dartagnan.wmm.filter.FilterMinus;
import com.dat3m.dartagnan.wmm.utils.Mode;
import com.microsoft.z3.BoolExpr;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.dat3m.dartagnan.wmm.relation.Relation;
import com.dat3m.dartagnan.wmm.utils.Tuple;
import com.dat3m.dartagnan.wmm.utils.TupleSet;
import com.microsoft.z3.Context;

import java.util.*;

import static com.dat3m.dartagnan.wmm.utils.Utils.edge;

public class RelRf extends Relation {

    private Set<MemEvent> reads;

    public RelRf(){
        term = "rf";
        forceDoEncode = true;
    }

    @Override
    public void initialise(Program program, Context ctx, Mode mode){
        super.initialise(program, ctx, mode);
        reads = null;
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
        BoolExpr enc = atMostOneEdgeToRead();

        for(Tuple tuple : maxTupleSet){
            MemEvent write = (MemEvent) tuple.getFirst();
            MemEvent read = (MemEvent) tuple.getSecond();
            BoolExpr edge = edge("rf", write, read, ctx);
            enc = ctx.mkAnd(enc, ctx.mkImplies(edge, ctx.mkAnd(
                    write.executes(ctx),
                    read.executes(ctx),
                    ctx.mkEq(write.getMemAddressExpr(), read.getMemAddressExpr()),
                    ctx.mkEq(write.getMemValueExpr(), read.getMemValueExpr())
            )));
        }

        return ctx.mkAnd(enc, atMostOneEdgeFromUnlock());
    }

    private BoolExpr atMostOneEdgeToRead(){
        BoolExpr enc = ctx.mkTrue();
        for(MemEvent read : getReads()){
            int i = 0;
            int cId = read.getCId();
            BoolExpr clause = ctx.mkEq(mkL(cId, 0), ctx.mkFalse());

            for(Tuple tuple : maxTupleSet.getBySecond(read)){
                BoolExpr prev = mkL(cId, ++i - 1);
                BoolExpr edge = edge("rf", tuple.getFirst(), read, ctx);
                clause = ctx.mkAnd(clause, ctx.mkNot(ctx.mkAnd(prev, edge)));
                clause = ctx.mkAnd(clause, ctx.mkEq(mkL(cId, i), ctx.mkOr(prev, edge)));
            }

            enc = ctx.mkAnd(enc, ctx.mkImplies(read.executes(ctx), clause));

            BoolExpr isSatisfied = mkL(cId, i);
            if(read.is(EType.LKR)){
                isSatisfied = ctx.mkOr(encodeCannotExecLock(read), isSatisfied);
                enc = ctx.mkAnd(enc, ctx.mkEq(Utils.isLockObtainedVar(read, ctx), mkL(cId, i)));
            }
            enc = ctx.mkAnd(enc, ctx.mkImplies(read.executes(ctx), isSatisfied));
        }
        return enc;
    }

    private BoolExpr atMostOneEdgeFromUnlock(){
        BoolExpr enc = ctx.mkTrue();
        TupleSet blockingReadTuples = new TupleSet();
        Set<MemEvent> unlocks = new HashSet<>();

        for(Tuple tuple : getMaxTupleSet()){
            if((tuple.getFirst().is(EType.UL) || tuple.getFirst().is(EType.INIT)) && tuple.getSecond().is(EType.LKR)){
                blockingReadTuples.add(tuple);
                unlocks.add((MemEvent) tuple.getFirst());
            }
        }

        for(MemEvent unlock : unlocks){
            int i = 0;
            int cId = unlock.getCId();
            BoolExpr clause = ctx.mkEq(mkM(cId, 0), ctx.mkFalse());
            for(Tuple tuple : blockingReadTuples.getByFirst(unlock)){
                BoolExpr edge = edge("rf", unlock, tuple.getSecond(), ctx);
                clause = ctx.mkAnd(clause, ctx.mkNot(ctx.mkAnd(mkM(cId, i++ - 1), edge)));
                clause = ctx.mkAnd(clause, ctx.mkEq(mkM(cId, i), ctx.mkOr(mkM(cId, i - 1), edge)));
            }
            enc = ctx.mkAnd(enc, ctx.mkAnd(clause, ctx.mkEq(Utils.isLockConsumedVar(unlock, ctx), mkM(cId, i))));
        }
        return enc;
    }

    private BoolExpr mkL(int readId, int i) {
        return (BoolExpr) ctx.mkConst("l(" + readId + "," + i + ")", ctx.mkBoolSort());
    }

    private BoolExpr mkM(int unlockId, int i) {
        return (BoolExpr) ctx.mkConst("m(" + unlockId + "," + i + ")", ctx.mkBoolSort());
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
                        isLegal = value == (e2.is(EType.LF) ? State.TAKEN : State.FREE);
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

    private BoolExpr encodeCannotExecLock(MemEvent lock){
        BoolExpr enc = ctx.mkTrue();
        for(Tuple tuple : maxTupleSet.getBySecond(lock)){
            enc = ctx.mkAnd(enc, ctx.mkOr(
                    Utils.isLockConsumedVar(tuple.getFirst(), ctx),
                    ctx.mkNot(tuple.getFirst().executes(ctx)),
                    ctx.mkNot(ctx.mkEq(((MemEvent)tuple.getFirst()).getMemAddressExpr(), lock.getMemAddressExpr()))
            ));
        }
        return enc;
    }

    private Set<MemEvent> getReads(){
        if(reads == null){
            reads = new HashSet<>();
            for(Tuple tuple : getMaxTupleSet()){
                reads.add((MemEvent) tuple.getSecond());
            }
        }
        return reads;
    }
}
