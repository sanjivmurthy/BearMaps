import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RasterTest {

    @Test
    public void testQueryDepth() {

        // test.html
        double ullon1 = -122.24163047377972;
        double lrlon1 = -122.24053369025242;
        int w1 = 892;
        int testLonDPP = Rasterer.getQueryDepth(lrlon1, ullon1, w1);
        assertEquals(7, testLonDPP);


//        double ullon2 = -122.3027284165759;
//        double lrlon2 = -122.20908713544797;
//        int w2 = 305;
//        int testLonDPP2 = Rasterer.getQueryDepth(lrlon2, ullon2, w2);
//        assertEquals(1, testLonDPP2);
//
//        double ullon3 = -122.30410170759153;
//        double lrlon3 = -122.2104604264636;
//        int w3 = 1085;
//        int testLonDPP3 = Rasterer.getQueryDepth(lrlon3, ullon3, w3);
//        assertEquals(2, testLonDPP3);
    }

    @Test
    public void testFileName() {
        String expected = "d2_x0_y3.png";
        int depth = 2;
        int xCoord = 0;
        int yCoord = 3;
        String actual = Rasterer.imageName(depth, xCoord, yCoord);
        assertEquals(expected, actual);
    }

    @Test
    public void testHTML() {
        Map<String, Double> params = new HashMap<>();
        params.put("lrlon", -122.24053369025242);
        params.put("ullon", -122.24163047377972);
        params.put("w", 892.0);
        params.put("h", 875.0);
        params.put("ullat", 37.87655856892288);
        params.put("lrlat", 37.87548268822065);


        Rasterer raster = new Rasterer();
        Map<String, Object> results = raster.getMapRaster(params);
        int depth = (int) results.get("depth");
        String[][] renderGrid = new String[3][3];

        renderGrid[0][0] = "d7_x84_y28.png";
        renderGrid[0][1] = "d7_x85_y28.png";
        renderGrid[0][2] = "d7_x86_y28.png";

        renderGrid[1][0] = "d7_x84_y29.png";
        renderGrid[1][1] = "d7_x85_y29.png";
        renderGrid[1][2] = "d7_x86_y29.png";

        renderGrid[2][0] = "d7_x84_y30.png";
        renderGrid[2][1] = "d7_x85_y30.png";
        renderGrid[2][2] = "d7_x86_y30.png";

        // System.out.println(Arrays.deepToString(renderGrid));
        String[][] renderAct = (String[][]) results.get("render_grid");
        System.out.println(Arrays.deepToString(renderAct));
        assertEquals(7, depth);
        assertEquals(renderGrid, renderAct);
    }

    @Test
    public void test1234() {
        Map<String, Double> params = new HashMap<>();
        params.put("lrlon", -122.20908713544797);
        params.put("ullon", -122.3027284165759);
        params.put("w", 305.0);
        params.put("h", 300.0);
        params.put("ullat", 37.88708748276975);
        params.put("lrlat", 37.848731523430196);

        Rasterer raster = new Rasterer();
        Map<String, Object> results = raster.getMapRaster(params);
        int depth = (int) results.get("depth");
        String[][] renderGrid = new String[2][2];

        renderGrid[0][0] = "d1_x0_y0.png";
        renderGrid[0][1] = "d1_x1_y0.png";

        renderGrid[1][0] = "d1_x0_y1.png";
        renderGrid[1][1] = "d1_x1_y1.png";

        String[][] renderAct = (String[][]) results.get("render_grid");
        assertEquals(1, depth);
        assertEquals(renderGrid, renderAct);

    }

    @Test

    public void test12() {
        Map<String, Double> params = new HashMap<>();
        params.put("lrlon", -122.2104604264636);
        params.put("ullon", -122.30410170759153);
        params.put("w", 1091.0);
        params.put("h", 566.0);
        params.put("ullat", 37.870213571328854);
        params.put("lrlat", 37.8318576119893);

        Rasterer raster = new Rasterer();
        Map<String, Object> results = raster.getMapRaster(params);
        int depth = (int) results.get("depth");
        String[][] renderGrid = new String[3][4];

        renderGrid[0][0] = "d2_x0_y1.png";
        renderGrid[0][1] = "d2_x1_y1.png";
        renderGrid[0][2] = "d2_x2_y1.png";
        renderGrid[0][3] = "d2_x3_y1.png";

        renderGrid[1][0] = "d2_x0_y2.png";
        renderGrid[1][1] = "d2_x1_y2.png";
        renderGrid[1][2] = "d2_x2_y2.png";
        renderGrid[1][3] = "d2_x3_y2.png";

        renderGrid[2][0] = "d2_x0_y3.png";
        renderGrid[2][1] = "d2_x1_y3.png";
        renderGrid[2][2] = "d2_x2_y3.png";
        renderGrid[2][3] = "d2_x3_y3.png";

        System.out.println(Arrays.deepToString(renderGrid));
        String[][] renderAct = (String[][]) results.get("render_grid");
        System.out.println(Arrays.deepToString(renderAct));
        assertEquals(2, depth);
        assertEquals(renderGrid, renderAct);
    }
}
