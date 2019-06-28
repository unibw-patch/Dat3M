package com.dat3m.ui.options.utils;

public enum Task {
	
	REACHABILITY, 
	NONINTERFERENCE, 
	PORTABILITY; 
	
    @Override
    public String toString() {
        switch(this){
        	case REACHABILITY:
        		return "Reachability";
        	case NONINTERFERENCE:
        		return "Non-Interference";
        	case PORTABILITY:
        		return "Portability";
        }
        return super.toString();
    }
}