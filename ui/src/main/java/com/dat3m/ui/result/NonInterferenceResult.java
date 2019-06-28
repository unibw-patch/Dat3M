package com.dat3m.ui.result;

import com.dat3m.armis.Armis;
import com.dat3m.armis.ArmisResult;
import com.dat3m.dartagnan.program.Program;
import com.dat3m.dartagnan.utils.Graph;
import com.dat3m.dartagnan.wmm.Wmm;
import com.dat3m.dartagnan.wmm.utils.Arch;
import com.dat3m.ui.options.utils.Options;
import com.dat3m.ui.utils.UiOptions;
import com.dat3m.ui.utils.Utils;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;

public class NonInterferenceResult implements Dat3mResult {

    private final Program p1;
    private final Program p2;
    private final Wmm wmm1;
    private final Wmm wmm2;
    private final UiOptions options;

    private Graph graph;
    private String verdict;

    public NonInterferenceResult(Program sourceProgram, Program targetProgram, Wmm sourceWmm, Wmm targetWmm, UiOptions options){
        this.p1 = sourceProgram;
        this.p2 = targetProgram;
        this.wmm1 = sourceWmm;
        this.wmm2 = targetWmm;
        this.options = options;
        run();
    }
    
    public Graph getGraph(){
        return graph;
    }

    public String getVerdict(){
        return verdict;
    }

    private void run(){
        if(validate()){
            Context ctx = new Context();
            Solver s1 = ctx.mkSolver();
            Solver s2 = ctx.mkSolver();

            ArmisResult result = Armis.testProgram(s1, s2, ctx, p1, p2, p1.getArch(), p2.getArch(), wmm1, wmm2, options.getSettings());

            verdict = "The program is" + (result.getIsNI() ? " non " : " ") + "interferent\n"
                    + "Iterations: " + result.getIterations();

            if(!result.getIsNI()){
                graph = new Graph(s1.getModel(), ctx, p1, p2, options.getSettings().getGraphRelations());
            }
            ctx.close();
        }
    }

    private boolean validate(){
        Arch sourceArch = p1.getArch() == null ? options.getSource() : p1.getArch();
        if(sourceArch == null) {
            Utils.showError("Missing source architecture.");
            return false;
        }
        Arch targetArch = p2.getArch() == null ? options.getTarget() : p2.getArch();
        if(targetArch == null) {
            Utils.showError("Missing target architecture.");
            return false;
        }
        p1.setArch(sourceArch);
        p2.setArch(targetArch);
        return true;
    }
}
