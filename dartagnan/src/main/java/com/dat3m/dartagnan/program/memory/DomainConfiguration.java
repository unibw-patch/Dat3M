package com.dat3m.dartagnan.program.memory;

import java.util.HashMap;

public class DomainConfiguration extends HashMap<Location, LocationConfiguration> {

	public LocationConfiguration get(Location loc) {
		for(Location key : keySet()) {
			// Location as objects can be different at this point
			// We compare them by name that it must be unique
			if(loc.getName().equals(key.getName())) {
				return super.get(key);
			}
		}
		throw new RuntimeException("The domain of " + loc.getName() + " has not been defined");
	}

	public LocationConfiguration put(Location loc, LocationConfiguration conf) {
		super.put(loc, conf);
		return conf;
	}
}
