package baqui88.game;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;

public class SingleMission {

    // We only need to provide path of first image in assets/images/[difficulty] folder
    // By convention, if path = [name].png, then:
    //      mirrorPath = [name] + "_mirror.png"
    //      layerPath = [name] + "_layer.png"

    // Use a layer same as original image, but retain only areas need to be spotted
    // Areas are marked by blocks (rectangles), they are separate
    // Layer have transparent background

    public String path;

    public int difficulty;
    public int time;
    public int target;  // number of ares need to be spotted
    // unique color used to check if block was checked, it means very almost TRANSPARENT
    public static final int CHECKED_BLOCK = 0x01000000;

    public SingleMission(){
        path = "";
    }

    public SingleMission(String path, int difficulty, int time, int target){
        this.path = path;
        this.difficulty = difficulty;
        this.time = time;
        this.target = target;
    }

    public String getPathInAssets(){
        return "images/"+difficulty+"/"+ path;
    }

    public String getMirrorPathInAssets(){
        String name = path.split("\\.")[0];
        return "images/" + difficulty + "/" + name + "_mirror.png";
    }

    public String getLayerPathInAssets() {
        String name = path.split("\\.")[0];
        return "images/" + difficulty + "/" + name + "_layer.png";
    }

    private static boolean valid(int color ){
        // except two cases : int color = 0x00000000 => pixel is not inside in any block
        //                 or int color = 0x01000000 => pixel is inside in a checked block
        return color != Color.TRANSPARENT && color != CHECKED_BLOCK;
    }

    // Get block contain touched pixel
    private static Rect getBlock(int x, int y, Bitmap layerBitmap){
        int w = layerBitmap.getWidth();
        int h = layerBitmap.getHeight();
        if ( x < 0 || x > w -1 || y < 0 || y > h - 1)
            return null;

        if ( !valid(layerBitmap.getPixel(x, y)) )
            return null;

        int left, top, right, bottom;
        left = top = right = bottom = 0;
        int step = 4;
        int pX, pY;

        //// Left
        pX = x;
        while (true){
            pX -= step;
            if (pX < 0) {
                pX = -1;
                break;
            }
            else if (layerBitmap.getPixel(pX, y) == Color.TRANSPARENT){
                break;
            }
        }
        // shift back
        for (int i = 1; i <= step; i ++){
            pX += 1 ;
            if (layerBitmap.getPixel(pX, y) != Color.TRANSPARENT) {
                left = pX;
                break;
            }
        }

        //// Right
        pX = x;
        while (true){
            pX += step;
            if (pX > w - 1) {
                pX = w;
                break;
            }
            else if (layerBitmap.getPixel(pX, y) == Color.TRANSPARENT){
                break;
            }
        }
        // shift back
        for (int i = 1; i <= step; i ++){
            pX -= 1 ;
            if (layerBitmap.getPixel(pX, y) != Color.TRANSPARENT) {
                right = pX;
                break;
            }
        }

        /// top
        pY = y;
        while (true){
            pY -= step;
            if (pY < 0) {
                pY = -1;
                break;
            }
            else if (layerBitmap.getPixel(x, pY) == Color.TRANSPARENT){
                break;
            }
        }
        // shift back
        for (int i = 1; i <= step; i ++){
            pY += 1 ;
            if (layerBitmap.getPixel(x, pY) != Color.TRANSPARENT) {
                top = pY;
                break;
            }
        }

        //// bottom
        pY = y;
        while (true){
            pY += step;
            if (pY > h - 1) {
                pY = h;
                break;
            }
            else if (layerBitmap.getPixel(x, pY) == Color.TRANSPARENT){
                break;
            }
        }
        // shift back
        for (int i = 1; i <= step; i ++){
            pY -= 1 ;
            if (layerBitmap.getPixel(x, pY) != Color.TRANSPARENT) {
                bottom = pY;
                break;
            }
        }

        return new Rect(left, top, right, bottom);
    }

    public static Rect getBlock(float x, float y, Bitmap layerBitmap){
        return getBlock(Math.round(x), Math.round(y), layerBitmap);
    }

    ///////////////////// Search block when hint required  //////////////////////////

    // search from left to right, top to bottom
    private static Rect searchFromLeftToRight(Bitmap layerBitmap, int resolutionX, int resolutionY ){
        int w = layerBitmap.getWidth();
        int h = layerBitmap.getHeight();
        for (int x = 0; x < w; x += resolutionX)
            for (int y = 0; y < h; y += resolutionY)
                if (valid(layerBitmap.getPixel(x, y)))
                    return getBlock(x, y, layerBitmap);

        return null;
    }

    // search from right to left, bottom to top
    private static Rect searchFromRightToLeft(Bitmap layerBitmap, int resolutionX, int resolutionY ){
        int w = layerBitmap.getWidth();
        int h = layerBitmap.getHeight();
        for (int x = w - 1; x >= 0; x -= resolutionX)
            for (int y = h - 1; y >= 0; y -= resolutionY)
                if (valid(layerBitmap.getPixel(x, y)))
                    return getBlock(x, y, layerBitmap);

        return null;
    }

    // search from top to bottom, right to left
    private static Rect searchFromTopToBottom(Bitmap layerBitmap, int resolutionX, int resolutionY ){
        int w = layerBitmap.getWidth();
        int h = layerBitmap.getHeight();
        for (int y = 0; y < h; y += resolutionY)
            for (int x = w - 1; x >= 0; x -= resolutionX)
                if (valid(layerBitmap.getPixel(x, y)))
                    return getBlock(x, y, layerBitmap);

        return null;
    }

    // search from bottom to top, left to right
    private static Rect searchFromBottomToTop(Bitmap layerBitmap, int resolutionX, int resolutionY ){
        int w = layerBitmap.getWidth();
        int h = layerBitmap.getHeight();
        for (int y = h - 1; y >= 0; y -= resolutionY)
            for (int x = 0; x < w; x += resolutionX)
                if (valid(layerBitmap.getPixel(x, y)))
                    return getBlock(x, y, layerBitmap);

        return null;
    }

    // search from center
    private static Rect searchFromCenter(Bitmap layerBitmap, int resolutionX, int resolutionY){
        int w = layerBitmap.getWidth();
        int h = layerBitmap.getHeight();
        int left, right, top, bottom;
        left = right = w/2;
        top = bottom = h/2;

        if (valid(layerBitmap.getPixel(w/2, h/2)))
            return getBlock(w/2, h/2, layerBitmap);

        while (true){
            left -= resolutionX;
            right += resolutionX;
            top -= resolutionY;
            bottom += resolutionY;
            left = Math.max(0, left);
            right = Math.min(right, w-1);
            top = Math.max(0, top);
            bottom = Math.min(bottom, h-1);

            for (int y = top; y <= bottom; y += resolutionY){
                if (valid(layerBitmap.getPixel(left, y)))
                    return getBlock(left, y, layerBitmap);
                if (valid(layerBitmap.getPixel(right, y)))
                    return getBlock(right, y, layerBitmap);
            }

            for (int x = left + resolutionX; x < right; x += resolutionX){
                if (valid(layerBitmap.getPixel(x, bottom)))
                    return getBlock(x, bottom, layerBitmap);
                if (valid(layerBitmap.getPixel(x, top)))
                    return getBlock(x, top, layerBitmap);
            }
            if (left == 0 && right == w-1 && top == 0 && bottom == h-1)
                break;
        }

        return null;
    }

    private interface SearchAction{
        public Rect search(Bitmap layerBitmap, int resolutionX, int resolutionY);
    }

    public static Rect hint(Bitmap layerBitmap){
        ArrayList<SearchAction> searchList = new ArrayList<>();
        searchList.add(new SearchAction() {
            @Override
            public Rect search(Bitmap layerBitmap, int resolutionX, int resolutionY) {
                return searchFromLeftToRight(layerBitmap, resolutionX, resolutionY);
            }
        });

        searchList.add(new SearchAction() {
            @Override
            public Rect search(Bitmap layerBitmap, int resolutionX, int resolutionY) {
                return searchFromRightToLeft(layerBitmap, resolutionX, resolutionY);
            }
        });
        searchList.add(new SearchAction() {
            @Override
            public Rect search(Bitmap layerBitmap, int resolutionX, int resolutionY) {
                return searchFromTopToBottom(layerBitmap, resolutionX, resolutionY);
            }
        });
        searchList.add(new SearchAction() {
            @Override
            public Rect search(Bitmap layerBitmap, int resolutionX, int resolutionY) {
                return searchFromBottomToTop(layerBitmap, resolutionX, resolutionY);
            }
        });
        searchList.add(new SearchAction() {
            @Override
            public Rect search(Bitmap layerBitmap, int resolutionX, int resolutionY) {
                return searchFromCenter(layerBitmap, resolutionX, resolutionY);
            }
        });

        // choose a random search method
        Collections.shuffle(searchList);
        SearchAction action = searchList.get(0);

        Rect r;
        r = action.search(layerBitmap, 20, 20);
        if (r != null)
            return r;
        r = action.search(layerBitmap, 12, 12);
        if (r != null)
            return r;
        r = action.search(layerBitmap, 8, 8);
        if (r != null)
            return r;
        r = action.search(layerBitmap, 2, 2);
        if (r != null)
            return r;

        return null;
    }

}
