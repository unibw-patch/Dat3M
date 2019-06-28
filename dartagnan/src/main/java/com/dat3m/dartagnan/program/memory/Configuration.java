package com.dat3m.dartagnan.program.memory;

import static com.dat3m.dartagnan.program.memory.utils.SecurityLevel.LOW;

import java.util.HashMap;

import com.dat3m.dartagnan.expression.IConst;

public class Configuration extends HashMap<Location, LocationConfiguration> {

	public LocationConfiguration get(Location loc) {
		for(Location key : keySet()) {
			if(loc.getName().equals(key.getName())) {
				return super.get(key);
			}
		}
		return new LocationConfiguration(LOW, new IConst(0), new IConst(0));
	}

	public LocationConfiguration put(Location loc, LocationConfiguration conf) {
		super.put(loc, conf);
		return conf;
	}
}
