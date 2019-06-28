package com.dat3m.armis.utils.options;

import com.dat3m.dartagnan.utils.options.BaseOptions;
import com.dat3m.dartagnan.wmm.utils.Arch;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

public class ArmisOptions extends BaseOptions {

    private Arch source;

    public ArmisOptions(){
        super();

        Option inputOption = new Option("i", "input", true,
                "Path to the file with input program");
        inputOption.setRequired(true);
        addOption(inputOption);

        Option targetCatOption = new Option("cat", true,
                "Path to the CAT file of the target memory model");
        targetCatOption.setRequired(true);
        addOption(targetCatOption);

//        Option sourceOption = new Option("s", "source", true,
//                "Source architecture {none|arm|arm8|power|tso}");
//        sourceOption.setRequired(true);
//        addOption(sourceOption);

        Option targetOption = new Option("t", "target", true,
                "Target architecture {none|arm|arm8|power|tso}");
        targetOption.setRequired(true);
        addOption(targetOption);

        addOption(new Option("m", "mode", true,
                "Encoding mode {knastertarski|idl|kleene}"));

        addOption(new Option("a", "alias", true,
                "Type of alias analysis {none|andersen|cfs}"));

        addOption(new Option("unroll", true,
                "Unrolling steps"));

        addOption(new Option("draw", true,
                "Path to save the execution graph if the state is reachable"));

        addOption(new Option("rels", true,
                "Relations to be drawn in the graph"));
    }

    public void parse(String[] args) throws ParseException, RuntimeException {
        CommandLine cmd = new DefaultParser().parse(this, args);
        parseSettings(cmd);
        parseGraphFilePath(cmd);

        String inputFilePath = cmd.getOptionValue("input");
        if(!inputFilePath.endsWith("pts")) {
            throw new RuntimeException("Portability analysis is allowed for .pts programs only");
        }

        programFilePath = cmd.getOptionValue("input");
        targetModelFilePath = cmd.getOptionValue("cat");
        source = Arch.get(cmd.getOptionValue("target"));
        target = Arch.get(cmd.getOptionValue("target"));
    }

    public Arch getSource(){
        return source;
    }
}
