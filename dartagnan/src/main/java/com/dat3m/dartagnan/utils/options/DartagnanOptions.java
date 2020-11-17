package com.dat3m.dartagnan.utils.options;

import static com.dat3m.dartagnan.analysis.AnalysisTypes.RACES;
import static com.dat3m.dartagnan.analysis.AnalysisTypes.REACHABILITY;
import static com.dat3m.dartagnan.analysis.AnalysisTypes.TERMINATION;
import static com.dat3m.dartagnan.analysis.AnalysisTypes.fromString;
import static com.dat3m.dartagnan.solver.Backend.CVC4;
import static com.dat3m.dartagnan.solver.Backend.Z3;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.cli.*;

import com.dat3m.dartagnan.analysis.AnalysisTypes;
import com.dat3m.dartagnan.solver.Backend;
import com.google.common.collect.ImmutableSet;

public class DartagnanOptions extends BaseOptions {

    protected Set<String> supportedFormats = ImmutableSet.copyOf(Arrays.asList("litmus", "bpl"));
    protected String overApproxFilePath;
    protected boolean iSolver;
    protected String witness;
    private Set<AnalysisTypes> analyses = ImmutableSet.copyOf(Arrays.asList(REACHABILITY, RACES, TERMINATION));
    private AnalysisTypes analysis = REACHABILITY;
    private Set<Backend> solvers = ImmutableSet.copyOf(Arrays.asList(Z3, CVC4));
    private Backend solver = Z3;
	
    public DartagnanOptions(){
        super();
        Option catOption = new Option("cat", true,
                "Path to the CAT file");
        catOption.setRequired(true);
        addOption(catOption);

        addOption(new Option("incrementalSolver", false,
        		"Use an incremental solver"));
        
        addOption(new Option("w", "witness", true,
                "Creates a violation witness. The argument is the original *.c file from which the Boogie code was generated."));

        addOption(new Option("analysis", true,
        		"The analysis to be performed: reachability (default), data-race detection, termination"));

        addOption(new Option("solver", true,
        		"The backend SMT solver: z3 (default), cvc4"));
    }
    
    public void parse(String[] args) throws ParseException, RuntimeException {
    	super.parse(args);
        if(supportedFormats.stream().map(f -> programFilePath.endsWith(f)). allMatch(b -> b.equals(false))) {
            throw new RuntimeException("Unrecognized program format");
        }
        CommandLine cmd = new DefaultParser().parse(this, args);
        iSolver = cmd.hasOption("incrementalSolver");
        if(cmd.hasOption("analysis")) {
        	AnalysisTypes selectedAnalysis = fromString(cmd.getOptionValue("analysis"));
        	if(!analyses.contains(selectedAnalysis)) {
        		throw new RuntimeException("Unrecognized analysis");
        	}
        	analysis = selectedAnalysis;
        }
        if(cmd.hasOption("solver")) {
        	Backend selectedSolver = Backend.fromString(cmd.getOptionValue("solver"));
        	if(!solvers.contains(selectedSolver)) {
        		throw new RuntimeException("Unrecognized solver");
        	}
        	solver = selectedSolver;
        }
        if(cmd.hasOption("witness")) {
        	witness = cmd.getOptionValue("witness");
        }
    }
    
    public boolean useISolver(){
        return iSolver;
    }

    public AnalysisTypes getAnalysis(){
		return analysis;
    }

    public Backend getSolver(){
		return solver;
    }

    public String createWitness(){
        return witness;
    }
}
