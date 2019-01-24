package dartagnan.wmm.filter;

import dartagnan.program.event.Event;

public class FilterUnion extends FilterAbstract {

    private FilterAbstract filter1;
    private FilterAbstract filter2;

    public FilterUnion(FilterAbstract filter1, FilterAbstract filter2){
        this.filter1 = filter1;
        this.filter2 = filter2;
    }

    @Override
    public boolean filter(Event e){
        return filter1.filter(e) || filter2.filter(e);
    }

    @Override
    public String toString(){
        return (filter1 instanceof FilterBasic ? filter1.toString() : "( " + filter1.toString() + " )")
                + " | " + (filter2 instanceof FilterBasic ? filter2.toString() : "( " + filter2.toString() + " )");
    }
}