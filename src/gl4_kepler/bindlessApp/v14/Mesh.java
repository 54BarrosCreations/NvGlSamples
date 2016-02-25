//----------------------------------------------------------------------------------
// File:        gl4-kepler/BindlessApp/Mesh.cpp
// SDK Version: v2.11 
// Email:       gameworks@nvidia.com
// Site:        http://developer.nvidia.com/
//
// Copyright (c) 2014-2015, NVIDIA CORPORATION. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//  * Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of NVIDIA CORPORATION nor the names of its
//    contributors may be used to endorse or promote products derived
//    from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ``AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
// OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//----------------------------------------------------------------------------------
package gl4_kepler.bindlessApp.v14;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_BYTE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import static gl4_kepler.bindlessApp.v00.Mesh.useVertexArray;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import nvAppBase.BufferUtils;

/**
 *
 * @author GBarbieri
 */
public class Mesh {

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int MAX = 2;
    }

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);    // vertex buffer object
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);
    public static boolean useVertexArray = true;
    public static boolean useHeavyVertexFormat = false;
    public static boolean setVertexFormatOnEveryDrawCall = false;
    public static int drawCallsPerState = 1;
    private int elementCount;
    private static int bindingIndex = 0;

    public Mesh() {
    }

    public void init(GL4 gl4, Vertex[] vertices, short[] elements) {

        gl4.glCreateBuffers(Buffer.MAX, bufferName);

        elementCount = elements.length;

        // Stick the data for the vertices and indices in their respective buffers
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(Vertex.SIZE * vertices.length);
        for (Vertex vertex : vertices) {
            vertex.toBb(vertexBuffer);
        }
        vertexBuffer.rewind();
        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elements);

        gl4.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl4.glBufferStorage(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Byte.BYTES, vertexBuffer, 0);
        gl4.glBindBuffer(GL_ARRAY_BUFFER, 0);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl4.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity() * Short.BYTES, elementBuffer, 0);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        if (setVertexFormatOnEveryDrawCall) {

            if (useVertexArray) {

                gl4.glCreateVertexArrays(1, vertexArrayName);
                gl4.glBindVertexArray(vertexArrayName.get(0));
                {
                    setVertexAttributes(gl4);

                    setVertexBuffers(gl4);
                }
                gl4.glBindVertexArray(0);
            }
        }
    }

    public static void renderPrep_(GL4 gl4) {
        
        setVertexAttributes(gl4);
    }

    /**
     * Sets up the vertex format state.
     *
     * @param gl4
     */
    public void renderPrep(GL4 gl4) {

        if (setVertexFormatOnEveryDrawCall) {

            if (useVertexArray) {

                gl4.glBindVertexArray(vertexArrayName.get(0));

            } else {

                setVertexAttributes(gl4);

                setVertexBuffers(gl4);
            }
        }
    }

    private static void setVertexAttributes(GL4 gl4) {

        gl4.glVertexAttribFormat(Semantic.Attr.POSITION, 3, GL_FLOAT, false, Vertex.PositionOffset);
        gl4.glVertexAttribFormat(Semantic.Attr.COLOR, 4, GL_UNSIGNED_BYTE, true, Vertex.ColorOffset);

        gl4.glVertexAttribBinding(Semantic.Attr.POSITION, bindingIndex);
        gl4.glVertexAttribBinding(Semantic.Attr.COLOR, bindingIndex);

        gl4.glEnableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glEnableVertexAttribArray(Semantic.Attr.COLOR);

        if (useHeavyVertexFormat) {

            gl4.glVertexAttribFormat(Semantic.Attr.ATTR0, 4, GL_FLOAT, false, Vertex.Attrib0Offset);
            gl4.glVertexAttribFormat(Semantic.Attr.ATTR1, 4, GL_FLOAT, false, Vertex.Attrib1Offset);
            gl4.glVertexAttribFormat(Semantic.Attr.ATTR2, 4, GL_FLOAT, false, Vertex.Attrib2Offset);
            gl4.glVertexAttribFormat(Semantic.Attr.ATTR3, 4, GL_FLOAT, false, Vertex.Attrib3Offset);
            gl4.glVertexAttribFormat(Semantic.Attr.ATTR4, 4, GL_FLOAT, false, Vertex.Attrib4Offset);

            gl4.glVertexAttribBinding(Semantic.Attr.ATTR0, bindingIndex);
            gl4.glVertexAttribBinding(Semantic.Attr.ATTR1, bindingIndex);
            gl4.glVertexAttribBinding(Semantic.Attr.ATTR2, bindingIndex);
            gl4.glVertexAttribBinding(Semantic.Attr.ATTR3, bindingIndex);
            gl4.glVertexAttribBinding(Semantic.Attr.ATTR4, bindingIndex);

            gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR0);
            gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR1);
            gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR2);
            gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR3);
            gl4.glEnableVertexAttribArray(Semantic.Attr.ATTR4);
        }
    }

    public void setVertexBuffers(GL4 gl4) {

        gl4.glBindVertexBuffer(bindingIndex, bufferName.get(Buffer.VERTEX), 0, Vertex.SIZE);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
    }

    /**
     * Does the actual rendering of the mesh.
     *
     * @param gl4
     */
    public void render(GL4 gl4) {

        // Do the actual drawing
        for (int i = 0; i < drawCallsPerState; i++) {
            gl4.glDrawElements(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0);
        }
    }
    
    /**
     * Resets state related to the vertex format.
     *
     * @param gl4
     */
    public static void renderFinish(GL4 gl4) {
        
        if (setVertexFormatOnEveryDrawCall) {
            
            if (useVertexArray) {

                gl4.glBindVertexArray(0);

            } else {

                unsetVertexAttributes(gl4);

                unsetVertexBuffers(gl4);
            }
        }
    }

    private static void unsetVertexAttributes(GL4 gl4) {

        gl4.glDisableVertexAttribArray(Semantic.Attr.POSITION);
        gl4.glDisableVertexAttribArray(Semantic.Attr.COLOR);

        if (useHeavyVertexFormat) {

            gl4.glDisableVertexAttribArray(Semantic.Attr.ATTR0);
            gl4.glDisableVertexAttribArray(Semantic.Attr.ATTR1);
            gl4.glDisableVertexAttribArray(Semantic.Attr.ATTR2);
            gl4.glDisableVertexAttribArray(Semantic.Attr.ATTR3);
            gl4.glDisableVertexAttribArray(Semantic.Attr.ATTR4);
        }
    }

    private static void unsetVertexBuffers(GL4 gl4) {

        gl4.glBindVertexBuffer(0, 0, 0, 0);
        gl4.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    public void dispose(GL4 gl4) {

        gl4.glDeleteBuffers(Buffer.MAX, bufferName);
        if (useVertexArray) {
            gl4.glDeleteVertexArrays(1, vertexArrayName);
        }

        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);
    }
}
