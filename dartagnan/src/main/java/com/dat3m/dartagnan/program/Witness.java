package com.dat3m.dartagnan.program;

import static com.dat3m.dartagnan.program.utils.EType.INIT;
import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.dat3m.dartagnan.program.Program;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.Skip;
import com.dat3m.dartagnan.program.utils.EType;
import com.dat3m.dartagnan.wmm.filter.FilterBasic;
import com.dat3m.dartagnan.wmm.utils.Utils;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;

public class Witness {

    private Model model;
    private Program program;
    private Context context;

    public Witness (Model m, Program p, Context con) {
        this.model = m;
        this.program = p;
        this.context = con;
    }

    public void write ()  {
        File newTextFile = new File("./output/" + "testtemp.graphml");
        FileWriter fw;
        HashMap<Event, Integer> executed_event_position = new HashMap<Event, Integer>();
        int position;


        for(Event e : program.getCache().getEvents(FilterBasic.get(EType.MEMORY))){
            if(this.model.getConstInterp(e.exec()).isTrue() && e.getCline() > -1) {
                position = Integer.parseInt(model.getConstInterp(Utils.intVar("hb", e, context)).toString());
                executed_event_position.put(e, position);
            }
        }
        System.out.println(executed_event_position);

        List<Map.Entry<Event, Integer>> list = new ArrayList<Map.Entry<Event, Integer>>(executed_event_position.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Event, Integer>>() {
            public int compare(Map.Entry<Event, Integer> o1, Map.Entry<Event, Integer> o2) {
                return (o1.getValue() - o2.getValue());
                //return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });

        try {
            fw = new FileWriter(newTextFile);
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            fw.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
            fw.write("  <graph edgedefault=\"directed\">\n");
            fw.write("    <data key=\"witness-type\">vio2lation_witness</data>\n");
            fw.write("    <data key=\"producer\">Dat3M</data>\n");
            fw.write("    <data key=\"specification\">CHECK( init(main()), LTL(G ! call(__VERIFIER_error())) )</data>\n");
            fw.write("    <data key=\"programfile\">" + "testtemp" + "</data>\n");
            fw.write("    <data key=\"architecture\">32bit</data>\n");
            fw.write("    <data key=\"programhash\">" + "checksum()" + "</data>\n");
            fw.write("    <data key=\"sourcecodelang\">C</data>\n");
            fw.write("    <node id=\"N0\"> <data key=\"entry\">true</data> </node>\n");
            fw.write("    <edge source=\"N0\" target=\"N1\"> <data key=\"createThread\">0</data> <data key=\"enterFunction\">main</data> </edge>\n");


            int time = 1;
            int cLine = -1;
            for (Map.Entry<Event, Integer> mapping : list){
                Event e = mapping.getKey();
                cLine = e.getCline();
                fw.write("    <node id=\"N" + (time) + "\"> </node>\n");
                fw.write("    <edge source=\"N" + time + "\" target=\"N" + (time+1) + "\"> <data key=\"" + "startline" + "\">" +  cLine  + "</data> </edge>\n");
                time++;
                //System.out.println(mapping.getKey()+": "+mapping.getValue());
            }


            fw.write("    <node id=\"N" + time + "\"> <data key=\"violation\">true</data> </node>\n");
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
