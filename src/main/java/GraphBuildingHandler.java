import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;



/**
 * Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for
 * pathfinding, under some constraints.
 * See OSM documentation on
 * <a href="http://wiki.openstreetmap.org/wiki/Key:highway">the highway tag</a>,
 * <a href="http://wiki.openstreetmap.org/wiki/Way">the way XML element</a>,
 * <a href="http://wiki.openstreetmap.org/wiki/Node">the node XML element</a>,
 * and the java
 * <a href="https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html">SAX parser tutorial</a>.
 * <p>
 * You may find the CSCourseGraphDB and CSCourseGraphDBHandler examples useful.
 * <p>
 * The idea here is that some external library is going to walk through the XML
 * file, and your override method tells Java what to do every time it gets to the next
 * element in the file. This is a very common but strange-when-you-first-see it pattern.
 * It is similar to the Visitor pattern we discussed for graphs.
 *
 * @author Alan Yao, Maurice Lee
 */
public class GraphBuildingHandler extends DefaultHandler {
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));
    boolean firstID;
    boolean validWay;
    boolean addFirst = true;
    private String activeState = "";
    private GraphDB g;
    private Long oldID;
    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equals("node")) {
            activeState = "node";
            Long justSeenID = Long.parseLong(attributes.getValue("id"));
            double nodeLon = Double.parseDouble(attributes.getValue("lon"));
            double nodeLat = Double.parseDouble(attributes.getValue("lat"));
            double[] lonLat = new double[]{nodeLon, nodeLat};
            g.coords.put(justSeenID, lonLat);
            g.routed.put(justSeenID, null);
        } else if (qName.equals("way")) {
            activeState = "way";
            firstID = true;
            g.potentials.clear();
        } else if (activeState.equals("way") && qName.equals("nd")) {
            if (firstID) {
                oldID = Long.parseLong(attributes.getValue("ref"));
                firstID = false;
            } else {
                Long justSeen = Long.parseLong(attributes.getValue("ref"));
                if (g.potentials.get(justSeen) == null) {
                    List<Long> newEdges = new ArrayList<>();
                    newEdges.add(oldID);
                    g.potentials.put(justSeen, newEdges);
                } else {
                    List<Long> newEdges = g.potentials.get(justSeen);
                    newEdges.add(oldID);
                    g.potentials.put(justSeen, newEdges);
                }
                if (g.potentials.get(oldID) == null) {
                    List<Long> oldEdges = new ArrayList<>();
                    oldEdges.add(justSeen);
                    g.potentials.put(oldID, oldEdges);
                } else {
                    List<Long> oldEdges = g.potentials.get(oldID);
                    oldEdges.add(justSeen);
                    g.potentials.put(oldID, oldEdges);
                }
                oldID = justSeen;
            }
        }
        if (activeState.equals("way") && qName.equals("tag")) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("highway")) {
                validWay = true;
                if (!ALLOWED_HIGHWAY_TYPES.contains(v)) {
                    validWay = false;
                    g.count++;
                }
            }
        } else if (activeState.equals("node") && qName.equals("tag") && attributes.getValue("k")
                .equals("name")) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            if (k.equals("highway")) {
                validWay = true;
                if (!ALLOWED_HIGHWAY_TYPES.contains(v)) {
                    validWay = false;
                    g.count++;
                }
            }
        }
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            if (validWay) {
                for (Long i : g.potentials.keySet()) {
                    if (g.routed.get(i) == null) {
                        List<Long> temp = g.potentials.get(i);
                        g.routed.put(i, temp);
                    } else {
                        List<Long> existingEdges = g.routed.get(i);
                        for (Long e : g.potentials.get(i)) {
                            if (!existingEdges.contains(e)) {
                                existingEdges.add(e);
                            }
                        }
                        g.routed.put(i, existingEdges);
                    }
                }
            }
            validWay = false;
            addFirst = true;
        }
    }
}
