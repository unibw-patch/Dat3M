package com.dat3m.svcomp;

import static com.dat3m.dartagnan.utils.Compilation.compile;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.commons.cli.HelpFormatter;

import com.dat3m.dartagnan.parsers.program.ProgramParser;
import com.dat3m.dartagnan.program.Program;
import com.dat3m.svcomp.options.SVCOMPOptions;
import com.dat3m.svcomp.utils.SVCOMPSanitizer;
import com.dat3m.svcomp.utils.SVCOMPWitness;

public class SVCOMPRunner {

    public static void main(String[] args) {
    	SVCOMPOptions options = new SVCOMPOptions();
        try {
            options.parse(args);
        }
        catch (Exception e){
            if(e instanceof UnsupportedOperationException){
                System.out.println(e.getMessage());
            }
            new HelpFormatter().printHelp("SVCOMP Runner", options);
            System.exit(1);
            return;
        }

        File file = new SVCOMPSanitizer(options.getProgramFilePath()).run(1);
		String path = file.getAbsolutePath();
		// File name contains "_tmp.c"
		String name = path.substring(path.lastIndexOf('/'), path.lastIndexOf('_'));
		int bound = 2;

		String output = "UNKNOWN";
		while(output.equals("UNKNOWN")) {
			try {
				compile(file, options.getOptimization(), options.useBP());
			} catch (IOException e) {
				System.out.println(e.getMessage());
				System.exit(0);
			}
	        // If not removed here, file is not removed when we reach the timeout
	        // File can be safely deleted since it was created by the SVCOMPSanitizer 
	        // (it not the original C file) and we already created the Boogie file
	        file.delete();

	    	ArrayList<String> cmd = new ArrayList<String>();
	    	cmd.add("java");
	    	cmd.add("-jar");
	    	cmd.add("dartagnan/target/dartagnan-2.0.6-jar-with-dependencies.jar");
	    	cmd.add("-i");
	    	cmd.add("./output/" + name + "-" + options.getOptimization() + ".bpl");
	    	cmd.add("-cat");
	    	cmd.add(options.getTargetModelFilePath());
	    	cmd.add("-t");
	    	cmd.add("none");
	    	cmd.add("-unroll");
	    	cmd.add(String.valueOf(bound));
	    	if(options.getOverApproxPath() != null) {
	    		cmd.add("-cegar");
	    		cmd.add(String.valueOf(options.getOverApproxPath()));
	    	}
	    	if(options.useISolver()) {
	    		cmd.add("-incrementalSolver");
	    	}
	    	ProcessBuilder processBuilder = new ProcessBuilder(cmd); 

	        try {
	        	Process proc = processBuilder.start();
				BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				try {
					proc.waitFor();
				} catch(InterruptedException e) {
					System.out.println(e.getMessage());
					System.exit(0);
				}
				while(read.ready()) {
					output = read.readLine();
				}
				if(proc.exitValue() == 1) {
					BufferedReader error = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					while(error.ready()) {
						System.out.println(error.readLine());
					}
					System.exit(0);
				}
			} catch(IOException e) {
				System.out.println(e.getMessage());
				System.exit(0);
			}
			bound++;
	        file = new SVCOMPSanitizer(options.getProgramFilePath()).run(bound);
		}
		output = output.contains("PASS") ? "PASS" : "FAIL";
		System.out.println(output);
		
        if(options.getGenerateWitness() && output.contains("FAIL")) {
			try {
				Program p = new ProgramParser().parse(new File("./output/" + name + "-" + options.getOptimization() + ".bpl"));
	            new SVCOMPWitness(p, options).write();;
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        file.delete();
        return;        	
    }
}
