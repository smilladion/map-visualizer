package routefinding;

import bfst20.mapdrawer.drawing.Drawable;
import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMRelation;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.scene.canvas.GraphicsContext;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class OSMRoute implements Drawable {
    private static OSMMap model;
    private List<OSMWay> ways = new ArrayList<>();
    static ArrayList<OSMWay> highways = new ArrayList<>();
    private static List<Drawable> routeDrawables = new ArrayList<>();


    public OSMRoute(OSMMap model, OSMNode startPoint, OSMNode endPoint, ArrayList<OSMWay> highways) {
        //startpoint = det punkt som bliver skrevet i "fra"
        //endpoint = det punkt som bliver skrevet i "til"
        this.model = model;
        this.highways = highways;
    }

    public ArrayList<OSMWay> loadHighways(OSMMap model) throws FileNotFoundException, XMLStreamException, IllegalStateException {
        File file = new File("src/main/resources/maps/samsoe.osm");
        XMLStreamReader xmlReader = XMLInputFactory.newFactory().createXMLStreamReader(new FileReader(file));
        String tagName = xmlReader.getLocalName();
        String highway = "highway";
        ways = model.getWays();

        for (OSMWay way : ways) {
            if (tagName != highway) {
                continue;
            } else {
                highways.add(way);
            }
        }
        return highways;
    }

    public OSMNode getFirstNode(OSMWay osmWay){ return osmWay.first(); }

    public OSMNode getLastNode(OSMWay osmWay) { return osmWay.last(); }


    public static void printHighways() {
        for (OSMWay way : highways) {
            System.out.println(way);
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        OSMNode startNode = new OSMNode(341933769, 55.8157300f, 10.6365960f);
        OSMNode endNode = new OSMNode(341933760, 55.8162770f, 10.6369650f);

        OSMRoute route = new OSMRoute(model, startNode, endNode, highways);
        routeDrawables.add(route);
    }
}

/*
    public OSMRoute(List<OSMWay> ways, OSMNode startNode, OSMNode endNode) throws FileNotFoundException, XMLStreamException {
        // startNode = get adress
        // endnode = get adress

        List<OSMWay> OSMRoute = new ArrayList<>();
        //for(ArrayList<OSMWay> way; ways){
            /*if(way == highway){
                OSMRoute.add(way);
            }

             */