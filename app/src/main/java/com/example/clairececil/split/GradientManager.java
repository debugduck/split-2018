/**This code is based on the gradient manager
 * from the Android Examples Code blog
 * Original code can be found here:
 * https://android--examples.blogspot.com/2015/11/android-how-to-apply-textview-text.html
 */
package com.example.clairececil.split;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import java.util.ArrayList;
import java.util.Random;


public class GradientManager {
    private Random mRandom = new Random();
    private Context mContext;
    private Point mSize;

    public GradientManager(Context context, Point size){
        this.mContext = context;
        this.mSize = size;
    }

    // Custom method to generate a LinearGradient with given colors
    protected LinearGradient getLinearGradient(ArrayList<String> colorStrings){
        LinearGradient gradient = new LinearGradient(
                0,
                0,
                mSize.x,
                mSize.y,
                getColorArray(colorStrings), // Colors to draw the gradient
                null, // No position defined
                getRandomShaderTileMode() // Shader tiling mode
        );
        // Return the LinearGradient
        return gradient;
    }

    // Custom method to generate random Shader TileMode
    protected Shader.TileMode getRandomShaderTileMode(){
        Shader.TileMode mode;
        int indicator = mRandom.nextInt(3);
        if(indicator==0){
            /*
                Shader.TileMode : CLAMP
                    replicate the edge color if the shader draws outside of its original bounds
            */
            mode = Shader.TileMode.CLAMP;
        }else if(indicator==1){
            /*
                Shader.TileMode : MIRROR
                    repeat the shader's image horizontally and vertically, alternating mirror images
                    so that adjacent images always seam
            */
            mode = Shader.TileMode.MIRROR;
        }else {
            /*
                Shader.TileMode : REPEAT
                    repeat the shader's image horizontally and vertically
            */
            mode = Shader.TileMode.REPEAT;
        }
        // Return the random Shader TileMode
        return mode;
    }

    // Parses the hex color strings to ints
    protected int[] getColorArray(ArrayList<String> inColors){
        int length = inColors.size();
        int[] outColors = new int[length];
        for (int i = 0; i < outColors.length; i++){
            outColors[i] = Color.parseColor(inColors.get(i));
        }
        return outColors;
    }
}
