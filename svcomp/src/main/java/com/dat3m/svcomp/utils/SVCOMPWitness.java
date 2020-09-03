package com.dat3m.svcomp.utils;

import static com.dat3m.dartagnan.program.utils.EType.INIT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.IntStream;

import com.dat3m.dartagnan.program.Program;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.utils.EType;
import com.dat3m.dartagnan.wmm.filter.FilterBasic;
import com.dat3m.dartagnan.wmm.utils.Utils;
import com.dat3m.svcomp.options.SVCOMPOptions;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;

public class SVCOMPWitness {
	
	private Program program;
	//private SVCOMPOptions options;
	private Model model;
	private Context context;

	public SVCOMPWitness(Program p, SVCOMPOptions o) {
		this.program = p;
		//this.options = o;
	}

	public SVCOMPWitness(Program p, SVCOMPOptions o, Model m, Context ctx) {
		this.program = p;
		//this.options = o;
		this.model = m;
		this.context = ctx;
	}


	public void writetest() {
		//String programFilePath = options.getProgramFilePath().substring(0, options.getProgramFilePath().lastIndexOf('.')) + ".c";
		//String programName = programFilePath.substring(programFilePath.lastIndexOf('/'), programFilePath.lastIndexOf('.'));






		File newTextFile = new File("./output/" + "testtemp.graphml");
		FileWriter fw;
		HashMap<Event, Integer> executed_event_position = new HashMap<>();
		int position;
		ArrayList<Integer> list= new ArrayList<>(executed_event_position.values());
		for(Event e : program.getCache().getEvents(FilterBasic.get(EType.MEMORY))){
//                if(m.getConstInterp(e.exec()).isTrue() && e.getCline() > -1) {
//                    System.out.println("Is executed");
//                    System.out.println(e);
//                    System.out.println(e.getCline());
//                    System.out.println();
//                }
			if(this.model.getConstInterp(e.exec()).isTrue() && e.getCline() > -1) {

				position = Integer.parseInt(String.valueOf(this.model.getConstInterp(Utils.intVar("hb", e, this.context))));
				executed_event_position.put(e, position);


				Collections.sort(list, new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return o1>o2?1:-1;
					}
				});
			}
		}

		try {
			fw = new FileWriter(newTextFile);
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
			fw.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
			fw.write("  <graph edgedefault=\"directed\">\n");
			fw.write("    <data key=\"witness-type\">violation_witness</data>\n");
			fw.write("    <data key=\"producer\">Dat3M</data>\n");
			fw.write("    <data key=\"specification\">CHECK( init(main()), LTL(G ! call(__VERIFIER_error())) )</data>\n");
			fw.write("    <data key=\"programfile\">" + "testtemp" + "</data>\n");
			fw.write("    <data key=\"architecture\">32bit</data>\n");
			fw.write("    <data key=\"programhash\">" + checksum() + "</data>\n");
			fw.write("    <data key=\"sourcecodelang\">C</data>\n");
			fw.write("    <node id=\"N0\"> <data key=\"entry\">true</data> </node>\n");
			fw.write("    <edge source=\"N0\" target=\"N1\"> <data key=\"createThread\">0</data> <data key=\"enterFunction\">main</data> </edge>\n");

			Iterator<Integer> iterator = list.iterator();
			int time = 1;
			int cLine;
			while ((iterator.hasNext())){
				Integer key = iterator.next();
				//Event value = executed_event_position.get(key);

				if(executed_event_position.containsValue(key)) {

				}

				//cLine = value.getCline();

				fw.write("    <node id=\"N" + (time) + "\"> </node>\n");
				//fw.write("    <edge source=\"N" + time + "\" target=\"N" + (time+1) + "\"> <data key=\"" + value.toString() + "\">" + "line = \"" + cLine + "\"" + time + "</data> </edge>\n");
				time++;

			}

			fw.write("    <node id=\"N" + time + "\"> <data key=\"violation\">true</data> </node>\n");
			fw.write("  </graph>\n");
			fw.write("</graphml>\n");
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void write() {
		//String programFilePath = options.getProgramFilePath().substring(0, options.getProgramFilePath().lastIndexOf('.')) + ".c";
		//String programName = programFilePath.substring(programFilePath.lastIndexOf('/'), programFilePath.lastIndexOf('.'));
        File newTextFile = new File("./output/" + "testtemp" + ".graphml");
        FileWriter fw;
		try {
			fw = new FileWriter(newTextFile);
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
			fw.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
			fw.write("  <graph edgedefault=\"directed\">\n");
			fw.write("    <data key=\"witness-type\">violation_witness</data>\n");
			fw.write("    <data key=\"producer\">Dat3M</data>\n");
			fw.write("    <data key=\"specification\">CHECK( init(main()), LTL(G ! call(__VERIFIER_error())) )</data>\n");
			fw.write("    <data key=\"programfile\">" + "testtemp" + "</data>\n");
			fw.write("    <data key=\"architecture\">32bit</data>\n");
			fw.write("    <data key=\"programhash\">" + "</data>\n");
			//fw.write("    <data key=\"programhash\">" + checksum() + "</data>\n");
			fw.write("    <data key=\"sourcecodelang\">C</data>\n");
			fw.write("    <node id=\"N0\"> <data key=\"entry\">true</data> </node>\n");
			fw.write("    <edge source=\"N0\" target=\"N1\"> <data key=\"createThread\">0</data> <data key=\"enterFunction\">main</data> </edge>\n");
			fw.write("    <node id=\"N1\"> </node>\n");
			int threadCount = program.getThreads().size() - program.getCache().getEvents(FilterBasic.get(INIT)).size();
			for(int i : IntStream.range(1, threadCount-1).toArray()) {
				fw.write("    <edge source=\"N" + i + "\" target=\"N" + (i+1) + "\"> <data key=\"createThread\">" + i + "</data> </edge>\n");
				fw.write("    <node id=\"N" + (i+1) + "\"> </node>\n");
			}
			fw.write("    <edge source=\"N" + (threadCount-1) + "\" target=\"N" + threadCount + "\"> <data key=\"createThread\">" + (threadCount-1) + "</data> </edge>\n");
			fw.write("    <node id=\"N" + threadCount + "\"> <data key=\"violation\">true</data> </node>\n");
			fw.write("  </graph>\n");
			fw.write("</graphml>\n");
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private String checksum() {
		String output = null;
		//String input = options.getProgramFilePath();
		try {
			Process proc = Runtime.getRuntime().exec("sha256sum " + "testtemp");
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
		output = output.substring(0, output.lastIndexOf(' '));
		return output;
	}

}
