package com.dat3m.dartagnan.program.memory;

import com.dat3m.dartagnan.expression.IConst;
import com.dat3m.dartagnan.program.memory.utils.SecurityLevel;

public class LocationConfiguration {

	SecurityLevel security;
	IConst minValue;
	IConst maxValue;
	
	public LocationConfiguration(SecurityLevel security, IConst minValue, IConst maxValue) {
		this.security = security;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	public SecurityLevel getSecurity() {
		return security;
	}

	public IConst getMinBound() {
		return minValue;
	}

	public IConst getMaxBound() {
		return maxValue;
	}

}
