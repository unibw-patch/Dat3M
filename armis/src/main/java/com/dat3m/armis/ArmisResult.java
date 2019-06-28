package com.dat3m.armis;

import com.dat3m.dartagnan.program.Program;

public class ArmisResult {

    private boolean isNI;
    private int iterations;
    private Program p1;
    private Program p2;

    ArmisResult(boolean isNI, int iterations, Program p1, Program p2){
        this.isNI = isNI;
        this.iterations = iterations;
        this.p1 = p1;
        this.p2 = p2;
    }

    public boolean getIsNI(){
        return isNI;
    }

    public int getIterations(){
        return iterations;
    }

    public Program getOneProgram(){
        return p1;
    }

    public Program getTwoProgram(){
        return p2;
    }
}
