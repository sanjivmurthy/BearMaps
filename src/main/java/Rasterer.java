import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    int leftUnderEstimate;
    int rightOverEstimate;
    int topUnderEstimate;
    int bottomOverEstimate;
    double ullon;
    double lrlon;
    double width;
    double ullat;
    double lrlat;
    private double[] upperCoords = new double[2];
    private double[] lowerCoords = new double[2];

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * </p>
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * <<<<<<< HEAD
     * "depth"         : Number, the depth of the nodes of the rastered image;
     * can also be interpreted as the length of the numbers in the image
     * string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     * forget to set this to true! <br>
     * =======
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     * forget to set this to true on success! <br>
     * >>>>>>> 0ff9935f3e4eef382e47c567733a55bb2bd2bcab
     * @params params Map of the HTTP GET request's query parameters - the query box and
     * the user viewport width and height.
     */

    // params: query lrlat, lrlon, width , returns: query depth
    public static int getQueryDepth(double lrlon, double ullon, double width) {

        double lonDPP = Math.abs((lrlon - ullon) / width);

        if (lonDPP >= 0.00034332275390625) {
            return 0;
        } else if (lonDPP >= 0.000171661376953125) {
            return 1;
        } else if (lonDPP >= 0.0000858306884765625) {
            return 2;
        } else if (lonDPP >= 0.00004291534423828125) {
            return 3;
        } else if (lonDPP >= 0.000021457672119140625) {
            return 4;
        } else if (lonDPP >= 0.000010728836059570312) {
            return 5;
        } else if (lonDPP >= 0.000005364418029785156) {
            return 6;
        } else {
            return 7;
        }
    }

    // given depth, xCoord, and yCoord formats a file string
    public static String imageName(int depth, int xCoord, int yCoord) {
        String depthString = String.valueOf(depth);
        String xString = String.valueOf(xCoord);
        String yString = String.valueOf(yCoord);

        return "d" + depthString + "_x" + xString + "_y" + yString + ".png";
    }

    // returns dimensions of tiles at a given depth
    public double[] tileDims(int depth) {
        double length = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / (Math.pow(2, depth));
        double height = (MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / (Math.pow(2, depth));
        return new double[]{length, height};
    }

    public String[][] bullshitMethod1() {
        int depth = getQueryDepth(lrlon, ullon, width);
        double tileLength = tileDims(depth)[0];
        double tileHeight = tileDims(depth)[1];
        int totalHeightTiles = (int) ((MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / tileHeight);
        int totalWidthTiles = (int) ((MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / tileLength);
        int boxWidth;
        if ((ullon - MapServer.ROOT_ULLON) < 0 && (MapServer.ROOT_LRLON - lrlon) < 0) {
            boxWidth = totalWidthTiles;
        } else {
            leftUnderEstimate = (int) ((ullon - MapServer.ROOT_ULLON) / tileLength);
            rightOverEstimate = (int) Math.ceil((lrlon - MapServer.ROOT_ULLON) / tileLength);
            boxWidth = rightOverEstimate - leftUnderEstimate;
        }
        int boxHeight;
        if ((ullat - MapServer.ROOT_ULLAT) > 0 && (lrlat - MapServer.ROOT_LRLAT) < 0) {
            boxHeight = totalHeightTiles;
        } else {
            topUnderEstimate = (int) ((MapServer.ROOT_ULLAT - ullat) / tileHeight);
            bottomOverEstimate = (int) Math.ceil((MapServer.ROOT_ULLAT - lrlat) / tileHeight);
            boxHeight = bottomOverEstimate - topUnderEstimate;
        }
        String[][] renderGrid1 = new String[boxHeight][boxWidth];
        int cols = 0;
        int rows = 0;

        for (int y = 0; y < Math.pow(2, depth); y++) {
            for (int x = 0; x < Math.pow(2, depth); x++) {
                double tileUllon = MapServer.ROOT_ULLON + (x * tileLength);
                double tileLrlon = MapServer.ROOT_ULLON + ((x + 1) * tileLength);
                double tileUllat = MapServer.ROOT_ULLAT - (y * tileHeight);
                double tileLrlat = MapServer.ROOT_ULLAT - ((y + 1) * tileHeight);
                if (tileLrlon > ullon && tileUllon < lrlon) {
                    if (tileUllat > lrlat && tileLrlat < ullat) {
                        String filePath = imageName(depth, x, y);
                        int rowNum = rows / boxWidth;
                        int colNum = cols % boxWidth;
                        if (rowNum == 0 && colNum == 0) {
                            upperCoords[0] = tileUllon;
                            upperCoords[1] = tileUllat;
                        }
                        if (rowNum == boxHeight - 1 && colNum == boxWidth - 1) {
                            lowerCoords[0] = tileLrlon;
                            lowerCoords[1] = tileLrlat;
                        }
                        renderGrid1[rowNum][colNum] = filePath;
                        cols++;
                        rows++;
                    }
                }
            }
        }
        return renderGrid1;
    }

    public String[][] bullshitMethod2() {
        int depth = getQueryDepth(lrlon, ullon, width);
        double tileLength = tileDims(depth)[0];
        double tileHeight = tileDims(depth)[1];
        int totalHeightTiles = (int) ((MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / tileHeight);
        int totalWidthTiles = (int) ((MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / tileLength);
        int boxWidth;
        if ((ullon - MapServer.ROOT_ULLON) < 0 && (MapServer.ROOT_LRLON - lrlon) < 0) {
            boxWidth = totalWidthTiles;
        } else {
            leftUnderEstimate = (int) ((ullon - MapServer.ROOT_ULLON) / tileLength);
            rightOverEstimate = (int) Math.ceil((lrlon - MapServer.ROOT_ULLON) / tileLength);
            boxWidth = rightOverEstimate - leftUnderEstimate;
        }
        int boxHeight;
        if ((ullat - MapServer.ROOT_ULLAT) > 0 && (lrlat - MapServer.ROOT_LRLAT) < 0) {
            boxHeight = totalHeightTiles;
        } else {
            topUnderEstimate = (int) ((MapServer.ROOT_ULLAT - ullat) / tileHeight);
            bottomOverEstimate = (int) Math.ceil((MapServer.ROOT_ULLAT - lrlat) / tileHeight);
            boxHeight = bottomOverEstimate - topUnderEstimate;
        }
        String[][] renderGrid2 = new String[boxHeight][boxWidth];
        int cols;
        int rows;

        int row = 0;
        int yIndex = topUnderEstimate;
        double underEstimatedUllon = MapServer.ROOT_ULLON + (leftUnderEstimate * tileLength);
        double overEstimatedLrlon = MapServer.ROOT_ULLON + (rightOverEstimate * tileLength);
        double underEstimatedUllat = MapServer.ROOT_ULLAT - (topUnderEstimate * tileHeight);
        double overEstimatedLrlat = MapServer.ROOT_ULLAT - (bottomOverEstimate * tileHeight);
        for (double y = underEstimatedUllat; y > overEstimatedLrlat; y -= tileHeight) {
            int xIndex = leftUnderEstimate;
            int col = 0;
            for (double x = underEstimatedUllon; x < overEstimatedLrlon; x += tileLength) {
                double tileUllon = x;
                double tileLrlon = x + tileLength;
                double tileUllat = y;
                double tileLrlat = y - tileHeight;
                if (tileLrlon > ullon && tileUllon < lrlon) {
                    if (tileUllat > lrlat && tileLrlat < ullat) {
                        if (row == 0 && col == 0) {
                            upperCoords[0] = tileUllon;
                            upperCoords[1] = tileUllat;
                        }
                        if (row == boxHeight - 1 && col == boxWidth - 1) {
                            lowerCoords[0] = tileLrlon;
                            lowerCoords[1] = tileLrlat;
                        }
                        String filepath = imageName(depth, xIndex, yIndex);
                        renderGrid2[row][col] = filepath;
                    }
                }
                col++;
                xIndex++;
            }
            row++;
            yIndex++;
        }
        return renderGrid2;
    }


    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
        lrlon = params.get("lrlon");
        ullon = params.get("ullon");
        width = params.get("w");
        ullat = params.get("ullat");
        lrlat = params.get("lrlat");
        int depth = getQueryDepth(lrlon, ullon, width);
        double tileLength = tileDims(depth)[0];
        double tileHeight = tileDims(depth)[1];
        int totalHeightTiles = (int) ((MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / tileHeight);
        int totalWidthTiles = (int) ((MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / tileLength);
        int boxWidth;
        if ((ullon - MapServer.ROOT_ULLON) < 0 && (MapServer.ROOT_LRLON - lrlon) < 0) {
            boxWidth = totalWidthTiles;
        } else {
            leftUnderEstimate = (int) ((ullon - MapServer.ROOT_ULLON) / tileLength);
            rightOverEstimate = (int) Math.ceil((lrlon - MapServer.ROOT_ULLON) / tileLength);
            boxWidth = rightOverEstimate - leftUnderEstimate;
        }
        int boxHeight;
        if ((ullat - MapServer.ROOT_ULLAT) > 0 && (lrlat - MapServer.ROOT_LRLAT) < 0) {
            boxHeight = totalHeightTiles;
        } else {
            topUnderEstimate = (int) ((MapServer.ROOT_ULLAT - ullat) / tileHeight);
            bottomOverEstimate = (int) Math.ceil((MapServer.ROOT_ULLAT - lrlat) / tileHeight);
            boxHeight = bottomOverEstimate - topUnderEstimate;
        }
        String[][] renderGrid;
        if (boxWidth == totalWidthTiles || boxHeight == totalHeightTiles) {
            renderGrid = bullshitMethod1();
        } else {
            renderGrid = bullshitMethod2();

        }

        results.put("depth", depth);
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lon", upperCoords[0]);
        results.put("raster_ul_lat", upperCoords[1]);
        results.put("raster_lr_lon", lowerCoords[0]);
        results.put("raster_lr_lat", lowerCoords[1]);
        results.put("query_success", true);
        return results;
    }
}
