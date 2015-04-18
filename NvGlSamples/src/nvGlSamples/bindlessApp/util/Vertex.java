/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.bindlessApp.util;

/**
 *
 * @author elect
 */
public class Vertex {

    public float[] position = new float[3];
    public int[] color = new int[4];
    public float[] attrib0 = new float[4];
    public float[] attrib1 = new float[4];
    public float[] attrib2 = new float[4];
    public float[] attrib3 = new float[4];
    public float[] attrib4 = new float[4];
    public float[] attrib5 = new float[4];
    public float[] attrib6 = new float[4];

    public final int positioOffset = 0;
    public final int colorOffset = 12;
    public final int attrib0Offset = 16;
    public final int attrib1Offset = 32;
    public final int attrib2Offset = 48;
    public final int attrib3Offset = 64;
    public final int attrib4Offset = 72;
    public final int attrib5Offset = 96;
    public final int attrib6Offset = 112;

    public Vertex(float x, float y, float z, float r, float g, float b, float a) {

        position[0] = x;
        position[1] = y;
        position[2] = z;
        
//        color[0] = 
    }
}
