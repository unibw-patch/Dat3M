package com.dat3m.dartagnan.program;

import com.dat3m.dartagnan.program.arch.aarch64.event.RMWStoreExclusiveStatus;
import com.dat3m.dartagnan.program.event.CondJump;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.Load;
import com.dat3m.dartagnan.program.event.utils.RegReaderData;
import com.dat3m.dartagnan.program.utils.EType;
import com.dat3m.dartagnan.program.utils.ThreadCache;
import com.dat3m.dartagnan.wmm.filter.FilterBasic;
import com.dat3m.dartagnan.wmm.utils.Arch;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

public class Thread {

    private final int id;
    private final Event entry;
    private Event exit;

    private Map<String, Register> registers;
    private ThreadCache cache;

    public Thread(int id, Event entry){
        if(id < 0){
            throw new IllegalArgumentException("Invalid thread ID");
        }
        if(entry == null){
            throw new IllegalArgumentException("Thread entry event must be not null");
        }
        this.id = id;
        this.entry = entry;
        this.exit = this.entry;
        this.registers = new HashMap<>();
    }

    public int getId(){
        return id;
    }

    public ThreadCache getCache(){
        if(cache == null){
            List<Event> events = new ArrayList<>(entry.getSuccessors());
            cache = new ThreadCache(events);
        }
        return cache;
    }

    public Register getRegister(String name){
        return registers.get(name);
    }

    public Register addRegister(String name){
        if(registers.containsKey(name)){
            throw new RuntimeException("Register " + id + ":" + name + " already exists");
        }
        cache = null;
        Register register = new Register(name, id);
        registers.put(register.getName(), register);
        return register;
    }

    public Event getEntry(){
        return entry;
    }

    public Event getExit(){
        return exit;
    }

    public void append(Event event){
        exit.setSuccessor(event);
        updateExit(event);
        cache = null;
    }

    private void updateExit(Event event){
        exit = event;
        Event next = exit.getSuccessor();
        while(next != null){
            exit = next;
            next = next.getSuccessor();
        }
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        return id == ((Thread) obj).id;
    }

    public Set<Event> getSlice() {
		List<Event> processing = new ArrayList<Event>();
		processing.addAll(getCache().getEvents(FilterBasic.get(EType.ASSERTION)));
		HashSet<Event> slice = new HashSet<Event>();
		while(!processing.isEmpty()) {
			Event next = processing.remove(0);
			slice.add(next);
			processing.addAll(condDependsOn(next));
			// Every RegWriter has one of the following types and thus every case is covered
			if(next instanceof RegReaderData) {
				RegReaderData reader = (RegReaderData)next;
				Set<Event> newEvents = reader.getDataRegs().stream().map(e -> e.getModifiedBy()).flatMap(Collection::stream).filter(e -> !slice.contains(e)).collect(Collectors.toSet());
				processing.addAll(newEvents);
			}
			if(next instanceof Load) {
				Load load = (Load)next;
				Set<Event> newEvents = load.getAddress().getModifiedBy().stream().filter(e -> !slice.contains(e)).collect(Collectors.toSet());
				processing.addAll(newEvents);
			}
			if(next instanceof RMWStoreExclusiveStatus) {
				//TODO
			}
		}
		return slice;
    }
    
    private Set<Event> condDependsOn(Event e) {
		HashSet<Event> set = new HashSet<Event>();
		//TODO: normal jumps missing
		set.addAll(getCache().getEvents(FilterBasic.get(EType.COND_JUMP)).stream().
				filter(cj -> cj.getUId() < e.getUId() && ((CondJump)cj).getLabel().getUId() > e.getUId()).
				collect(Collectors.toSet()));
		return set;
    }
    
    // Unrolling
    // -----------------------------------------------------------------------------------------------------------------

    public int unroll(int bound, int nextId){
        nextId = entry.unroll(bound, nextId, null);
        updateExit(entry);
        cache = null;
        return nextId;
    }


    // Compilation
    // -----------------------------------------------------------------------------------------------------------------

    public int compile(Arch target, int nextId) {
        nextId = entry.compile(target, nextId, null);
        updateExit(entry);
        cache = null;
        return nextId;
    }


    // Encoding
    // -----------------------------------------------------------------------------------------------------------------

    public BoolExpr encodeCF(Context ctx){
        return entry.encodeCF(ctx, ctx.mkTrue());
    }
}
