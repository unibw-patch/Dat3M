package com.dat3m.armis;

import com.dat3m.dartagnan.Dartagnan;
import com.dat3m.porthos.utils.options.PorthosOptions;
import com.microsoft.z3.*;
import com.microsoft.z3.enumerations.Z3_ast_print_mode;
import com.dat3m.dartagnan.parsers.program.ProgramParser;
import com.dat3m.dartagnan.parsers.cat.ParserCat;
import com.dat3m.dartagnan.program.Program;
import com.dat3m.dartagnan.utils.Graph;
import com.dat3m.dartagnan.utils.Settings;
import com.dat3m.dartagnan.wmm.Wmm;
import com.dat3m.dartagnan.wmm.utils.Arch;
import org.apache.commons.cli.*;

import static com.dat3m.armis.Encodings.encodeLowEquivalentInit;
import static com.dat3m.armis.Encodings.encodeReachedLowState;
import static com.dat3m.armis.Encodings.encodeInitStateFromModel;
import static com.dat3m.armis.Encodings.encodeLowDiffFinal;

import java.io.File;
import java.io.IOException;

public class Armis {

    public static void main(String[] args) throws IOException {

        PorthosOptions options = new PorthosOptions();
        try {
            options.parse(args);
        }
        catch (Exception e){
            if(e instanceof UnsupportedOperationException){
                System.out.println(e.getMessage());
            }
            new HelpFormatter().printHelp("PORTHOS", options);
            System.exit(1);
            return;
        }

        Arch arch1 = options.getSource();
        Arch arch2 = options.getTarget();
        Settings settings = options.getSettings();
        System.out.println("Settings: " + options.getSettings());

        String inputFilePath = options.getProgramFilePath();
        if(!inputFilePath.endsWith("pts")) {
            System.out.println("Unrecognized program format");
            System.exit(0);
            return;
        }
        Wmm mcm1 = new ParserCat().parse(new File(options.getTargetModelFilePath()));
        Wmm mcm2 = new ParserCat().parse(new File(options.getTargetModelFilePath()));

        ProgramParser programParser = new ProgramParser();
        Program p1 = programParser.parse(new File(inputFilePath));
        Program p2 = programParser.parse(new File(inputFilePath));

        Context ctx = new Context();
        Solver s1 = ctx.mkSolver(ctx.mkTactic(Settings.TACTIC));
        Solver s2 = ctx.mkSolver(ctx.mkTactic(Settings.TACTIC));

        // null as a configuration set the initial value of locations to 0
        ArmisResult result = testProgram(s1, s2, ctx, p1, p2, arch1, arch2, mcm1, mcm2, settings);

        if(result.getIsNI()){
            System.out.println("The program is Non-Interferent");
            System.out.println("Iterations: " + result.getIterations());

        } else {
            System.out.println("The program is Interferent");
            System.out.println("Iterations: " + result.getIterations());
            if(settings.getDrawGraph()) {
                ctx.setPrintMode(Z3_ast_print_mode.Z3_PRINT_SMTLIB_FULL);
                Dartagnan.drawGraph(new Graph(s1.getModel(), ctx, p1, p2, settings.getGraphRelations()), options.getGraphFilePath());
                System.out.println("Execution graph is written to " + options.getGraphFilePath());
            }
        }

        ctx.close();
    }

    public static ArmisResult testProgram(Solver s1, Solver s2, Context ctx, Program p1, Program p2, Arch one, Arch two,
                                     Wmm wmmOne, Wmm wmmTwo, Settings settings){

        p1.unroll(settings.getBound(), 0);
        p2.unroll(settings.getBound(), 0);

        int nextId = p1.compile(one, 0);
        p2.compile(two, nextId);

        BoolExpr oneCF = p1.encodeCF(ctx);
        BoolExpr oneFV = p1.encodeFinalRegisterValues(ctx);
        BoolExpr oneMM = wmmOne.encode(p1, ctx, settings);
        BoolExpr oneDomain = p1.encodeDomain(ctx, settings.getConfiguration());

        s1.add(p2.encodeCF(ctx));
        s1.add(p2.encodeFinalRegisterValues(ctx));
        s1.add(wmmTwo.encode(p2, ctx, settings));
        s1.add(wmmTwo.consistent(p2, ctx));
        s1.add(p2.encodeDomain(ctx, settings.getConfiguration()));

        s1.add(oneCF);
        s1.add(oneFV);
        s1.add(oneMM);
        s1.add(wmmOne.consistent(p1, ctx));
        s1.add(oneDomain);

        s2.add(oneCF);
        s2.add(oneFV);
        s2.add(oneMM);
        s2.add(wmmOne.consistent(p1, ctx));
        s2.add(oneDomain);

        s1.add(encodeLowEquivalentInit(p2, p1, settings.getConfiguration(), ctx));
        s1.add(encodeLowDiffFinal(p1, p2, settings.getConfiguration(), ctx));

        boolean NI = true;
        int iterations = 1;

        Status lastCheck = s1.check();

        while(lastCheck == Status.SATISFIABLE) {
            Model model = s1.getModel();
            BoolExpr reachedState = encodeReachedLowState(p1, settings.getConfiguration(), model, ctx);
            BoolExpr initState = encodeInitStateFromModel(p1, settings.getConfiguration(), model, ctx);
            s2.push();
            s2.add(ctx.mkNot(initState));
            s2.add(reachedState);
            if(s2.check() == Status.UNSATISFIABLE) {
                NI = false;
                break;
            }
            encodeInitStateFromModel(p1, settings.getConfiguration(), s2.getModel(), ctx);
            s2.pop();
            s1.add(ctx.mkNot(reachedState));
            iterations++;
            lastCheck = s1.check();
        }
        return new ArmisResult(NI, iterations, p1, p2);
    }
}