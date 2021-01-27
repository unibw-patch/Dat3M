package com.dat3m.ui.options.utils;

public enum Task {
	
	REACHABILITY, 
	PORTABILITY,
	SECURITY; 
	
    @Override
    public String toString() {
        switch(this){
            case REACHABILITY:
                return "Reachability";
            case PORTABILITY:
                return "Portability";
            case SECURITY:
                return "Security";
        }
        return super.toString();
    }
}