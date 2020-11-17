package com.dat3m.dartagnan.analysis;

import static com.dat3m.dartagnan.solver.Backend.CVC4;
import static com.dat3m.dartagnan.solver.Backend.Z3;
import static com.dat3m.dartagnan.utils.Result.FAIL;
import static com.dat3m.dartagnan.utils.Result.PASS;
import static com.dat3m.dartagnan.utils.Result.UNKNOWN;
import static com.microsoft.z3.Status.SATISFIABLE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.dat3m.dartagnan.asserts.AssertTrue;
import com.dat3m.dartagnan.program.Program;
import com.dat3m.dartagnan.solver.Backend;
import com.dat3m.dartagnan.utils.Result;
import com.dat3m.dartagnan.utils.Settings;
import com.dat3m.dartagnan.wmm.Wmm;
import com.dat3m.dartagnan.wmm.utils.Arch;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;

public class Base {

    public static Result runAnalysis(Solver s1, Context ctx, Program program, Wmm wmm, Arch target, Settings settings) {
    	program.unroll(settings.getBound(), 0);
        program.compile(target, 0);
        // AssertionInline depends on compiled events (copies)
        // Thus we need to set the assertion after compilation
        program.updateAssertion();
       	if(program.getAss() instanceof AssertTrue) {
       		return PASS;
       	}
       	
        // Using two solvers is much faster than using
        // an incremental solver or check-sat-assuming
        Solver s2 = ctx.mkSolver();
        
        BoolExpr encodeCF = program.encodeCF(ctx);
		s1.add(encodeCF);
        s2.add(encodeCF);
        
        BoolExpr encodeFinalRegisterValues = program.encodeFinalRegisterValues(ctx);
		s1.add(encodeFinalRegisterValues);
        s2.add(encodeFinalRegisterValues);
        
        BoolExpr encodeWmm = wmm.encode(program, ctx, settings);
		s1.add(encodeWmm);
        s2.add(encodeWmm);
        
        BoolExpr encodeConsistency = wmm.consistent(program, ctx);
		s1.add(encodeConsistency);
        s2.add(encodeConsistency);
       	
        s1.add(program.getAss().encode(ctx));
        if(program.getAssFilter() != null){
            BoolExpr encodeFilter = program.getAssFilter().encode(ctx);
			s1.add(encodeFilter);
            s2.add(encodeFilter);
        }

        BoolExpr encodeNoBoundEventExec = program.encodeNoBoundEventExec(ctx);

        Result res;
		if(s1.check() == SATISFIABLE) {
			s1.add(encodeNoBoundEventExec);
			res = s1.check() == SATISFIABLE ? FAIL : UNKNOWN;	
		} else {
			s2.add(ctx.mkNot(encodeNoBoundEventExec));
			res = s2.check() == SATISFIABLE ? UNKNOWN : PASS;	
		}
        
		if(program.getAss().getInvert()) {
			res = res.invert();
		}
		return res;
    }
	
    public static Result runAnalysisIncrementalSolver(Solver solver, Context ctx, Program program, Wmm wmm, Arch target, Settings settings) {
    	return runAnalysisIncrementalSolver(solver, ctx, program, wmm, target, settings, Z3);
    }
    
    public static Result runAnalysisIncrementalSolver(Solver solver, Context ctx, Program program, Wmm wmm, Arch target, Settings settings, Backend smtSolver) {
    	program.unroll(settings.getBound(), 0);
        program.compile(target, 0);
        // AssertionInline depends on compiled events (copies)
        // Thus we need to update the assertion after compilation
        program.updateAssertion();
       	if(program.getAss() instanceof AssertTrue) {
       		return PASS;
       	}

        solver.add(program.encodeCF(ctx));
        solver.add(program.encodeFinalRegisterValues(ctx));
        solver.add(wmm.encode(program, ctx, settings));
        solver.add(wmm.consistent(program, ctx));  
        solver.push();
        solver.add(program.getAss().encode(ctx));
        if(program.getAssFilter() != null){
            solver.add(program.getAssFilter().encode(ctx));
        }

        if(smtSolver.equals(CVC4)) {
            try {
            	return runCVC4(solver, ctx, program);
            } catch (Exception e) {
            	// Nothing to be done
            }        	
        }

        Result res = UNKNOWN;        
		if(solver.check() == SATISFIABLE) {
        	solver.add(program.encodeNoBoundEventExec(ctx));
			res = solver.check() == SATISFIABLE ? FAIL : UNKNOWN;
        } else {
        	solver.pop();
			solver.add(ctx.mkNot(program.encodeNoBoundEventExec(ctx)));
        	res = solver.check() == SATISFIABLE ? UNKNOWN : PASS;
        }
		return program.getAss().getInvert() ? res.invert() : res;
    }
    
    private static Result runCVC4(Solver solver, Context ctx, Program program) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter("./output/smt2/" + program.getName() + ".smt2"));
        writer.write(ctx.benchmarkToSMTString(program.getName(), "ALL", "unknown", "", solver.getAssertions(), ctx.mkTrue()));
        writer.close();
        
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("cvc4");
        cmd.add("./output/smt2/" + program.getName() + ".smt2");
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
    	Process proc = processBuilder.start();
		BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		proc.waitFor();
		Result res = UNKNOWN;
		String output = "unknown";
		while(read.ready()) {
			output = read.readLine();
		}
		if(!output.contains("sat")) {
			throw new RuntimeException("Problem with cvc4");
		}
		if(output.equals("sat")) {
            writer = new BufferedWriter(new FileWriter("./output/smt2/" + program.getName() + ".final.smt2"));
            writer.write(ctx.benchmarkToSMTString(program.getName(), "ALL", "unknown", "", solver.getAssertions(), program.encodeNoBoundEventExec(ctx)));
            writer.close();
            
            cmd = new ArrayList<String>();
            cmd.add("cvc4");
            cmd.add("./output/smt2/" + program.getName() + ".final.smt2");

            processBuilder = new ProcessBuilder(cmd);
        	proc = processBuilder.start();
    		read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    		proc.waitFor();
    		while(read.ready()) {
    			output = read.readLine();
    		}
    		if(!output.contains("sat")) {
    			throw new RuntimeException("Problem with cvc4");
    		}
			res = output.equals("sat") ? FAIL : UNKNOWN;
        } else {
        	solver.pop();
            writer = new BufferedWriter(new FileWriter("./output/smt2/" + program.getName() + ".final.smt2"));
            writer.write(ctx.benchmarkToSMTString(program.getName(), "ALL", "unknown", "", solver.getAssertions(), ctx.mkNot(program.encodeNoBoundEventExec(ctx))));
            writer.close();
            
            cmd = new ArrayList<String>();
            cmd.add("cvc4");
            cmd.add("./output/smt2/" + program.getName() + ".final.smt2");
            
            processBuilder = new ProcessBuilder(cmd);
        	proc = processBuilder.start();
    		read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    		proc.waitFor();
    		while(read.ready()) {
    			output = read.readLine();
    		}
    		if(!output.contains("sat")) {
    			throw new RuntimeException("Problem with cvc4");
    		}
    		res = output.equals("sat") ? UNKNOWN : PASS;
        }
		return program.getAss().getInvert() ? res.invert() : res;

    }
}
