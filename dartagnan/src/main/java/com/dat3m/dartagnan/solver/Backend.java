package com.dat3m.dartagnan.solver;

public enum Backend {
	Z3, CVC4;
	
	public static Backend fromString(String s) {
        if(s != null){
            s = s.trim();
            switch(s){
                case "z3":
                    return Z3;
                case "cvc4":
                    return CVC4;
            }
        }
        throw new UnsupportedOperationException("Unrecognized analysis " + s);
	}

	public String toString() {
        switch(this) {
            case Z3:
                return "z3";
            case CVC4:
                return "cvc4";
        }
        throw new UnsupportedOperationException("Unrecognized analysis " + this);
	}

}
