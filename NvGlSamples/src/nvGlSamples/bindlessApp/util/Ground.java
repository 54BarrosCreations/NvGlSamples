/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.bindlessApp.util;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import java.util.ArrayList;

/**
 *
 * @author gbarbieri
 */
public class Ground extends Mesh {

    private int[] vao;

    public Ground() {

        super();

        vao = new int[1];
    }

    @Override
    public void update(GL4 gl4, ArrayList<Vertex> vertices, ArrayList<Short> indices) {

        if (vertexBuffer[0] == 0) {

            gl4.glGenBuffers(1, vertexBuffer, 0);
        }
        if (indexBuffer[0] == 0) {

            gl4.glGenBuffers(1, indexBuffer, 0);
        }
        // Stick the data for the vertices and indices in their respective buffers
        int vertexLenght = vertices.get(0).toFloatArray().length;
        float[] verticesArray = new float[vertices.size() * vertexLenght];
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            System.arraycopy(vertex.toFloatArray(), 0, verticesArray, i * vertexLenght, vertexLenght);
        }
        short[] indicesArray = new short[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indicesArray[i] = indices.get(i);
        }
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, vertexBuffer[0]);
        {
            gl4.glBufferData(GL4.GL_ARRAY_BUFFER, verticesArray.length * GLBuffers.SIZEOF_FLOAT,
                    GLBuffers.newDirectFloatBuffer(verticesArray), GL4.GL_STATIC_DRAW);
        }
        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);

        gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0]);
        {
            gl4.glBufferData(GL4.GL_ELEMENT_ARRAY_BUFFER, indices.size() * GLBuffers.SIZEOF_SHORT,
                    GLBuffers.newDirectShortBuffer(indicesArray), GL4.GL_STATIC_DRAW);
        }
        gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, 0);

        if (vao[0] == 0) {
            gl4.glGenVertexArrays(1, vao, 0);
        }
        gl4.glBindVertexArray(vao[0]);
        {
            gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, indexBuffer[0]);

            gl4.glEnableVertexAttribArray(0);
            gl4.glEnableVertexAttribArray(1);

            gl4.glVertexAttribPointer(0, 3, GL4.GL_FLOAT, false, Vertex.size(), Vertex.positionOffset);
            gl4.glVertexAttribPointer(1, 4, GL4.GL_UNSIGNED_BYTE, true, Vertex.size(), Vertex.colorOffset);
        }
        gl4.glBindVertexArray(0);
    }

    public void renderP(GL4 gl4) {

        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, vertexBuffer[0]);
        {
            gl4.glBindVertexArray(vao[0]);
            {

            }
        }
    }

    @Override
    public void render(GL4 gl4) {

        gl4.glDrawElements(GL4.GL_TRIANGLES, 3, GL4.GL_UNSIGNED_SHORT, 0);
    }

    public void renderF(GL4 gl4) {

        gl4.glBindVertexArray(0);

        gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
    }
}
