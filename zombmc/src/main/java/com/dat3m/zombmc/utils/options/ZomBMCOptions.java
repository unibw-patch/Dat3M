package com.dat3m.zombmc.utils.options;

import static com.dat3m.dartagnan.compiler.Mitigation.LFENCE;
import static com.dat3m.dartagnan.compiler.Mitigation.NOBRANCHSPECULATION;
import static com.dat3m.dartagnan.compiler.Mitigation.SLH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.*;

import com.dat3m.dartagnan.compiler.Mitigation;
import com.dat3m.dartagnan.utils.Settings;
import com.dat3m.dartagnan.utils.options.BaseOptions;
import com.google.common.collect.ImmutableSet;

public class ZomBMCOptions extends BaseOptions {

	public static String ENTRYSTRING = "entry";
	public static String SECRETSTRING = "secret";
	public static String SECRETEVENTSTRING = "read_from";
	public static String BRANCHSPECULATIONSTRING = "branch_speculation";
	public static String ONLYSPECULATIVESTRING = "branch_speculation_error";
	public static String ALIASSPECULATIONSTRING = "alias_speculation";
	public static String LFENCESTRING = "lfence";
	public static String SLHSTRING = "slh";
	public static String TIMEOUT = "timeout";
	
    protected Set<String> supportedFormats = ImmutableSet.copyOf(Arrays.asList("bpl", "s"));
    
    private String secret = "spectre_secret";
    private int read_from = -1;
    private boolean branchSpeculation;
    private boolean onlySpeculative;
    private boolean aliasSpeculation;
    private boolean lfence;
    private boolean slh;
    private String entry = "main";
    private int timoeut = -1;
    
    public ZomBMCOptions(){
        super();
        Option catOption = new Option("cat", true,
                "Path to the CAT file");
        catOption.setRequired(true);
        addOption(catOption);

        Option entryOption = new Option(ENTRYSTRING, true,
                "Name of the entry procedure (default: main)");
        addOption(entryOption);

        Option secretOption = new Option(SECRETSTRING, true,
                "Name of the secret variable (default: secret)");
        addOption(secretOption);

        Option secretEventOption = new Option(SECRETEVENTSTRING, true,
                "ID of the event to read from");
        addOption(secretEventOption);

        Option noSpecOption = new Option(BRANCHSPECULATIONSTRING, false,
                "Allow branch speculation");
        addOption(noSpecOption);

        Option onlySpeculativeOption = new Option(ONLYSPECULATIVESTRING, false,
                "Check for safety violation only due to branch misprediction (requires " + BRANCHSPECULATIONSTRING + " option)");
        addOption(onlySpeculativeOption);

        Option lfenceOption = new Option(LFENCESTRING, false,
                "Fence after every branch mitigation");
        addOption(lfenceOption);

        Option slhOption = new Option(SLHSTRING, false,
                "Speculative Load Hardening mitigation");
        addOption(slhOption);
    }

    public ZomBMCOptions(String secret, boolean onlySpeculative, List<Mitigation> mitigations, Settings settings, int timeout){
        this.secret = secret;
        this.branchSpeculation = !mitigations.contains(NOBRANCHSPECULATION);
        this.onlySpeculative = onlySpeculative;
        this.lfence = mitigations.contains(LFENCE);
        this.slh = mitigations.contains(SLH);
        this.settings = settings;
        this.timoeut= timeout;
        this.aliasSpeculation = false;
    }
	
    public ZomBMCOptions(String secret, boolean onlySpeculative, boolean aliasSpeculation, List<Mitigation> mitigations, Settings settings, int timeout){
    	this(secret, onlySpeculative, mitigations, settings, timeout);
    	this.aliasSpeculation = aliasSpeculation; 
    }
    
    public ZomBMCOptions(int read_from, boolean onlySpeculative, List<Mitigation> mitigations, Settings settings, int timeout){
        this.read_from = read_from;
        this.branchSpeculation = !mitigations.contains(NOBRANCHSPECULATION);
        this.onlySpeculative = onlySpeculative;
        this.lfence = mitigations.contains(LFENCE);
        this.slh = mitigations.contains(SLH);
        this.settings = settings;
        this.timoeut= timeout;
        this.aliasSpeculation = false;
    }
	
    public void parse(String[] args) throws ParseException, RuntimeException {
    	super.parse(args);
        if(supportedFormats.stream().map(f -> programFilePath.endsWith(f)). allMatch(b -> b.equals(false))) {
            throw new RuntimeException("Unrecognized program format");
        }
    	CommandLine cmd = new DefaultParser().parse(this, args);
    	if(cmd.hasOption(ENTRYSTRING)) {    		
        	entry = cmd.getOptionValue(ENTRYSTRING);
    	}
    	secret = cmd.getOptionValue(SECRETSTRING);
    	if(cmd.hasOption(SECRETEVENTSTRING)) {
        	read_from = Integer.parseInt(cmd.getOptionValue(SECRETEVENTSTRING));    		
    	}
    	branchSpeculation = cmd.hasOption(BRANCHSPECULATIONSTRING);
    	onlySpeculative = cmd.hasOption(ONLYSPECULATIVESTRING);
    	aliasSpeculation = cmd.hasOption(ALIASSPECULATIONSTRING);
    	lfence = cmd.hasOption(LFENCESTRING);
    	slh = cmd.hasOption(SLHSTRING);
    }
    
    public String getEntry(){
        return entry;
    }

    public String getSecretOption(){
        return secret;
    }

    public int getReadFrom(){
        return read_from;
    }

    public boolean getBranchSpeculativeOption(){
        return branchSpeculation;
    }

    public boolean getAliasSpeculativeOption(){
        return aliasSpeculation;
    }

    public boolean getOnlySpeculativeOption(){
        return onlySpeculative;
    }

    public boolean getLfenceOption(){
        return lfence;
    }

    public boolean getSLHOption(){
        return slh;
    }
    
    public int getTimeout(){
        return timoeut;
    }
    
    public List<Mitigation> getMitigations() {
        List<Mitigation> mitigations = new ArrayList<Mitigation>();
        if(!branchSpeculation) {
            mitigations.add(NOBRANCHSPECULATION);
        }
        if(lfence) {
            mitigations.add(LFENCE);
        }
        if(slh) {
            mitigations.add(SLH);
        }
        return mitigations;
    }
    
    @Override
    public String toString() {
		return 
				"bound: " + getSettings().getBound() + ", " +
				SECRETSTRING + ": " + secret + ", " + 
				ONLYSPECULATIVESTRING + ": " + onlySpeculative + 
				", mitigations: " + getMitigations() + ", " +
				ALIASSPECULATIONSTRING + ": " + aliasSpeculation + ", " +
				TIMEOUT + ": " + timoeut;
    }
}