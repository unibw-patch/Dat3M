package com.dat3m.dartagnan.program.memory;

import java.util.HashMap;

import com.dat3m.dartagnan.expression.IConst;

import static com.dat3m.dartagnan.program.memory.utils.SecurityLevel.LOW;

public class DomainConfiguration extends HashMap<Location, LocationConfiguration> {

	public LocationConfiguration get(Location loc) {
		for(Location key : keySet()) {
			// Location (from the program and in the configuration) as objects can be different at this point
			// We compare them by name that it must be unique
			if(loc.getName().equals(key.getName())) {
				return super.get(key);
			}
		}
		// Currently we set the range in the UI as [0,0] even if the program may have initialized it with a different value
		// Set the [min,max] values from the initial value (if any) of the location in the program
		return new LocationConfiguration(LOW, new IConst(0), new IConst(0));
	}

	public LocationConfiguration put(Location loc, LocationConfiguration conf) {
		super.put(loc, conf);
		return conf;
	}
}
