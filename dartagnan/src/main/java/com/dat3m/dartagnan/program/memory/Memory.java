package com.dat3m.dartagnan.program.memory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntExpr;
import com.dat3m.dartagnan.program.memory.utils.IllegalMemoryAccessException;

import static com.dat3m.dartagnan.parsers.program.visitors.VisitorAsmX86.PRECISION;

import java.util.*;

public class Memory {

    private BiMap<Location, Address> map;
    private Map<String, Location> locationIndex;
    private Map<String, List<Address>> arrays;

    private int nextIndex = 0;

    public Memory(){
        map = HashBiMap.create();
        locationIndex = new HashMap<>();
        arrays = new HashMap<>();
    }

    public Location getLocationForAddress(Address address){
        return map.inverse().get(address);
    }

    public BoolExpr encode(Context ctx){
        BoolExpr enc = ctx.mkTrue();
        for(List<Address> array : arrays.values()){
            Expr e1 = array.get(0).toZ3Int(ctx);
            for(int i = 1; i < array.size(); i++){
                Expr e2 = array.get(i).toZ3Int(ctx);
                Expr newAddress = e1.isBV() ? ctx.mkBVAdd((BitVecExpr)e1, ctx.mkBV(1, array.get(0).getPrecision())) : ctx.mkAdd((IntExpr)e1, ctx.mkInt(1));
				enc = ctx.mkAnd(enc, ctx.mkEq(e2, newAddress));
                e1 = e2;
            }
        }
        enc = ctx.mkAnd(enc, ctx.mkAnd(getAllAddresses().stream().map(a -> ctx.mkBVSGT((BitVecExpr) a.toZ3Int(ctx), ctx.mkBV(0, PRECISION))).toArray(BoolExpr[]::new)));
        return ctx.mkAnd(enc, ctx.mkDistinct(getAllAddresses().stream().map(a -> a.toZ3Int(ctx)).toArray(Expr[]::new)));
    }

    public List<Address> malloc(String name, int size, int precision){
        if(!arrays.containsKey(name) && size > 0){
            List<Address> addresses = new ArrayList<>();
            for(int i = 0; i < size; i++){
                addresses.add(new Address(nextIndex++, precision));
            }
            arrays.put(name, addresses);
            return addresses;
        }
        throw new IllegalMemoryAccessException("Illegal malloc for " + name);
    }

    public Location getOrCreateLocation(String name, int precision){
        if(!locationIndex.containsKey(name)){
            Location location = new Location(name, new Address(nextIndex++, precision));
            map.put(location, location.getAddress());
            locationIndex.put(name, location);
            return location;
        }
        return locationIndex.get(name);
    }

    public Location getLocation(String name){
        if(!locationIndex.containsKey(name)){
        	throw new RuntimeException("No Location named " + name);
        }
        return locationIndex.get(name);
    }

    public Address getPointer(String name){
        if(!arrays.containsKey(name)){
        	throw new RuntimeException("No array named " + name);
        }
        return arrays.get(name).get(0);
    }

    public List<Address> getArrayAddresses(String name){
        if(!arrays.containsKey(name)){
        	throw new RuntimeException("No Array named " + name);
        }
        return arrays.get(name);
    }

    public ImmutableSet<Address> getAllAddresses(){
        Set<Address> result = new HashSet<>(map.values());
        for(List<Address> array : arrays.values()){
            result.addAll(array);
        }
        return ImmutableSet.copyOf(result);
    }
}