package bfst20.mapdrawer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.LongSupplier;

public class OSMWay extends ArrayList<OSMNode> implements LongSupplier {
    long id;

    public void OSMway(long id){ //skal være long return
        this.id = id;
    }

    public OSMNode first() {
        return get(0);
    }

    public OSMNode last() {
        return get(size()-1);
    }

    @Override
    public long getAsLong() {
        return id;
    }

    public static OSMWay connectOSM(OSMWay input, OSMWay output){
        if(input == null) return output;
        if(output == null) return input;

        OSMWay OSMWay = new OSMWay();

        if(input.first() == output.first()){
            OSMWay.addAll(input);
            Collections.reverse((OSMWay));                          //Hvad sker der her? Ænder jeg rækkefølgen på min arraylist?
            OSMWay.remove(OSMWay.size() - 1);
            OSMWay.addAll(output);
        } else if( input.first() == output.last()){
            OSMWay.addAll(output);
            OSMWay.remove(OSMWay.size() - 1);
            OSMWay.addAll(input);
        } else if(input.last() == output.first()){
            OSMWay.addAll(input);
            OSMWay.remove(OSMWay.size() - 1);
            OSMWay.addAll(output);
        } else if(input.last() == output.last()){
            ArrayList outputList = new ArrayList<>(output);
            Collections.reverse(outputList);                        //Hvad sker der her? Ænder jeg rækkefølgen på min arraylist?
            OSMWay.addAll(input);
            OSMWay.remove(OSMWay.size() - 1);
            OSMWay.addAll(outputList);
        } else {
            throw new IllegalArgumentException("Cannot connect unconnected OSMWays");
        }
        return OSMWay;
    }
}
