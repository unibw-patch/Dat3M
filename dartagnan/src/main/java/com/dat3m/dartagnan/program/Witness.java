package com.dat3m.dartagnan.program;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import com.dat3m.dartagnan.expression.BConst;
import com.dat3m.dartagnan.program.event.Event;
import com.dat3m.dartagnan.program.event.MemEvent;
import com.dat3m.dartagnan.program.memory.Address;
import com.dat3m.dartagnan.program.memory.Location;
import com.dat3m.dartagnan.program.utils.EType;
import com.dat3m.dartagnan.utils.options.DartagnanOptions;
import com.dat3m.dartagnan.wmm.filter.FilterBasic;
import com.dat3m.dartagnan.wmm.utils.Utils;
import com.microsoft.z3.Context;
import com.microsoft.z3.Model;

public class Witness {

    private Model model;
    private Program program;
    private Context context;
    private DartagnanOptions option ;

    public Witness (Model m, Program p, Context con, DartagnanOptions pn) {
        this.model = m;
        this.program = p;
        this.context = con;
        this.option = pn;

    }

    public void write () {
        File newTextFile = new File("./output/" + option.getbplName() + ".graphml");
        FileWriter fw;
        HashMap<Event, Integer> executed_event_position = new HashMap<>();

        int position;
        int count_threads = 0;

        for(Event e : program.getEvents()){

            System.out.println(e.toString() + " " + e.getCline());

            if (this.if_create_thread(e)) {
                count_threads++;
            }

            if(this.model.getConstInterp(e.exec()).isTrue() && e.getCline() > -1) {
                //executed_event_position.put(e, e.getCId()); only for singel program
                if (model.getConstInterp(Utils.intVar("hb", e, context)) != null) {
                    position = Integer.parseInt(String.valueOf(this.model.getConstInterp(Utils.intVar("hb", e, this.context))));
                    executed_event_position.put(e, position);
                }



            }
        }

        //if (executed_event_position.isEmpty()) {
        if (count_threads == 0){
            executed_event_position.clear();
            for (Event e : program.getEvents()) {
                if(this.model.getConstInterp(e.exec()).isTrue() && e.getCline() > -1) {
                    executed_event_position.put(e, e.getCId());
                }
            }
        }


        List<Map.Entry<Event, Integer>> list = new ArrayList<>(executed_event_position.entrySet());

        /*for (Map.Entry<Event, Integer> mapping : list) {
            System.out.println(mapping.getKey().toString() + " " + mapping.getKey().getCline() + " " + mapping.getValue() + " " + mapping.getKey().getOId());
        }*/
        //sort the startline events.
        Collections.sort(list, new Comparator<Map.Entry<Event, Integer>>() {
            public int compare(Map.Entry<Event, Integer> o1, Map.Entry<Event, Integer> o2) {
                return (o1.getValue() - o2.getValue());
                //return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });

        //for selecting the lines which have the same cline.
        int cforrepeat = -1;
        int oidforrepeat = Integer.MAX_VALUE;
        List<Map.Entry<Event, Integer>> secondlist = new ArrayList<>();
        for (Map.Entry<Event, Integer> mapping : list) {
            if (cforrepeat == mapping.getKey().getCline() && mapping.getKey().getOId() > oidforrepeat) {
                cforrepeat = mapping.getKey().getCline();
                oidforrepeat = mapping.getKey().getOId();
                //list.remove(mapping);
                continue;
            }
            secondlist.add(mapping);
            cforrepeat = mapping.getKey().getCline();
            oidforrepeat = mapping.getKey().getOId();
        }

        cforrepeat =Integer.MIN_VALUE;
        List<Map.Entry<Event, Integer>> delectrepeatlist = new ArrayList<>();
        for (Map.Entry<Event, Integer> mapping : list) {
            if (cforrepeat == mapping.getKey().getCline()) {
                cforrepeat = mapping.getKey().getCline();
                continue;
            }
            delectrepeatlist.add(mapping);
            cforrepeat = mapping.getKey().getCline();
        }

        for (Map.Entry<Event, Integer> mapping : list) {
            System.out.println(mapping.getKey().toString() + " " + mapping.getKey().getCline() + " " + mapping.getValue() + " " + mapping.getKey().getOId());
        }

        System.out.println(" ");

        //print the selected events for unrolling
        for (Map.Entry<Event, Integer> mapping : secondlist) {
            System.out.println(mapping.getKey().toString() + " " + mapping.getKey().getCline() + " " + mapping.getValue() + " " + mapping.getKey().getOId());
        }

        System.out.println(" ");

        for (Map.Entry<Event, Integer> mapping : delectrepeatlist) {
            System.out.println(mapping.getKey().toString() + " " + mapping.getKey().getCline() + " " + mapping.getValue() + " " + mapping.getKey().getOId());
        }


        try {
            fw = new FileWriter(newTextFile);
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            fw.write("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
            fw.write("  <graph edgedefault=\"directed\">\n");
            fw.write("    <data key=\"witness-type\">violation_witness</data>\n");
            fw.write("    <data key=\"producer\">Dat3M</data>\n");
            fw.write("    <data key=\"specification\">CHECK( init(main()), LTL(G ! call(__VERIFIER_error())) )</data>\n");
            fw.write("    <data key=\"programfile\">" + option.getProgramName() + "</data>\n");
            fw.write("    <data key=\"architecture\">32bit</data>\n");
            fw.write("    <data key=\"programhash\">" + checksum() + "</data>\n");
            fw.write("    <data key=\"sourcecodelang\">C</data>\n");
            fw.write("    <node id=\"N0\"> <data key=\"entry\">true</data> </node>\n");
            fw.write("    <edge source=\"N0\" target=\"N1\"><data key=\"threadId\">1</data> <data key=\"enterFunction\">main</data> </edge>\n");


            int time = 1;
            int cLine = -1;
            //int avoid = cLine;
            int count = 0;

            for (Map.Entry<Event, Integer> mapping : delectrepeatlist){
                Event e = mapping.getKey();
                cLine = e.getCline();

                //to check if the event refers to a return!
                if(e instanceof MemEvent && ((MemEvent)e).getMemValue() instanceof BConst && !((BConst)((MemEvent)e).getMemValue()).getValue()) {
                    continue;
                }


                fw.write("    <node id=\"N" + (time) + "\"> </node>\n");

                if (this.if_create_thread(e)) {
                    fw.write("    <edge source=\"N" + time + "\" target=\"N" + (time+1) + "\"><data key=\"threadId\">" + program.getThreadId(e) +
                            "</data> <data key=\"" + "createThread"  + "\">" + (count +2) + "</data> <data key=\"" + "startline" + "\">" +  cLine  + "</data> </edge>\n");
                    count++;
                }
                else {
                    fw.write("    <edge source=\"N" + time + "\" target=\"N" + (time+1) + "\"><data key=\"threadId\">" + program.getThreadId(e) +
                            "</data> <data key=\"startline\">" +  cLine  + "</data> </edge>\n");
                }



                time++;
                /*avoid = cLine;
                oid = e.getOId();*/
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
        String input = option.getProgramName();
        try {
            Process proc = Runtime.getRuntime().exec("sha256sum " + input);
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

    private boolean if_create_thread (Event e) {

        if (program.getCache().getEvents(FilterBasic.get(EType.MEMORY)).contains(e)) {
            MemEvent m = (MemEvent)e;
            //Here is only to select the event which has created a thread!
            if(m.getAddress() instanceof Address) {
                Location loc = program.getMemory().getLocationForAddress((Address)m.getAddress());
                if(loc != null && loc.getName()!= null)  {
                    if(loc.getName().contains("_active")) {
                        if (model.getConstInterp(Utils.intVar("hb", e, context)) != null) {
                            //System.out.println("haha");
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }

}
