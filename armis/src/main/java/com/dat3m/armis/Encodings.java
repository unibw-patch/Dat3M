package com.dat3m.armis;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;
import com.dat3m.dartagnan.program.Program;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.Init;
import com.dat3m.dartagnan.program.memory.Address;
import com.dat3m.dartagnan.program.memory.Configuration;
import com.dat3m.dartagnan.program.memory.Location;
import com.dat3m.dartagnan.program.utils.EType;
import com.dat3m.dartagnan.wmm.filter.FilterBasic;
import static com.dat3m.dartagnan.program.memory.utils.SecurityLevel.LOW;
import static com.dat3m.dartagnan.program.memory.utils.SecurityLevel.HIGH;

import java.util.*;
import java.util.stream.Collectors;

class Encodings {

	static BoolExpr encodeLowEquivalentInit(Program p1, Program p2, Configuration confs, Context ctx) {
        Iterator<Event> it1 = p1.getCache().getEvents(FilterBasic.get(EType.INIT)).iterator();
        Iterator<Event> it2 = p2.getCache().getEvents(FilterBasic.get(EType.INIT)).iterator();

		Set<Address> adds1 = p1.getLocations().stream().filter(e -> confs.get(((Location)e)).getSecurity().equals(LOW)).map(e -> ((Location)e).getAddress()).collect(Collectors.toSet());
		Set<Address> adds2 = p2.getLocations().stream().filter(e -> confs.get(((Location)e)).getSecurity().equals(LOW)).map(e -> ((Location)e).getAddress()).collect(Collectors.toSet());

        BoolExpr enc = ctx.mkTrue();
        BoolExpr orEnc = ctx.mkFalse();

        while(it1.hasNext() && it2.hasNext()) {
        	Init i1 = (Init)it1.next();
        	Init i2 = (Init)it2.next();
        	if(adds1.contains(i1.getAddress()) && adds2.contains(i2.getAddress())) {
            	enc = ctx.mkAnd(enc, ctx.mkEq(i1.getMemValueExpr(), i2.getMemValueExpr()));        		
        	} else {
        		orEnc = ctx.mkOr(orEnc, ctx.mkNot(ctx.mkEq(i1.getMemValueExpr(), i2.getMemValueExpr())));   
        	}
        }
        enc = ctx.mkAnd(enc, orEnc);
//        System.out.println("=====EQ=====");
//        System.out.println(enc.simplify());
//        System.out.println("==========");
        return enc;
	}
	
	static BoolExpr encodeLowDiffFinal(Program p1, Program p2, Configuration confs, Context ctx) {
		Iterator<Location> it1 = p1.getLocations().stream().filter(e -> confs.get(((Location)e)).getSecurity().equals(LOW)).iterator();
		Iterator<Location> it2 = p2.getLocations().stream().filter(e -> confs.get(((Location)e)).getSecurity().equals(LOW)).iterator();

        BoolExpr enc = ctx.mkFalse();
        
        while(it1.hasNext() && it2.hasNext()) {
        	Location loc1 = it1.next();
        	Location loc2 = it2.next();
        	enc = ctx.mkOr(enc, ctx.mkNot(ctx.mkEq(loc1.getLastValueExpr(ctx), loc2.getLastValueExpr(ctx))));
        }
//        System.out.println("=====DIFF=====");
//		System.out.println(enc.simplify());
//        System.out.println("==========");
        return enc;
	}
	
	static public BoolExpr encodeReachedLowState(Program p, Configuration confs, Model model, Context ctx) {
		BoolExpr reachedState = ctx.mkTrue();
		Iterator<Location> it = p.getLocations().stream().filter(e -> confs.get(((Location)e)).getSecurity().equals(LOW)).iterator();
        while(it.hasNext()) {
        	Location loc = it.next();
			reachedState = ctx.mkAnd(reachedState, ctx.mkEq(loc.getLastValueExpr(ctx), model.getConstInterp(loc.getLastValueExpr(ctx))));
        }
        System.out.println("=====REACH=====");
		System.out.println(reachedState.simplify());
		System.out.println("==========");
		return reachedState;
	}

	static public BoolExpr encodeInitStateFromModel(Program p, Configuration confs, Model model, Context ctx) {
		BoolExpr initState = ctx.mkTrue();
        Iterator<Event> it = p.getCache().getEvents(FilterBasic.get(EType.INIT)).iterator();
		Set<Address> adds = p.getLocations().stream().filter(e -> confs.get(((Location)e)).getSecurity().equals(HIGH)).map(e -> ((Location)e).getAddress()).collect(Collectors.toSet());

        while(it.hasNext()) {
        	Init i = (Init)it.next();
        	if(adds.contains(i.getAddress())) {
        		initState = ctx.mkAnd(initState, ctx.mkEq(i.getMemValueExpr(), model.getConstInterp(i.getMemValueExpr())));        		
        	}
        }
        System.out.println("=====INIT-MODEL=====");
		System.out.println(initState.simplify());
		System.out.println("==========");
		return initState;
	}

}