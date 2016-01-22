//----------------------------------------------------------------------------------
// File:        gl4-kepler/BindlessApp/Mesh.h
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
package gl4_kepler.bindlessApp;

import com.jogamp.opengl.util.GLBuffers;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class Vertex {

    public float[] position = new float[3];
    public byte[] color = new byte[4];
    public float[] attrib0 = new float[4];
    public float[] attrib1 = new float[4];
    public float[] attrib2 = new float[4];
    public float[] attrib3 = new float[4];
    public float[] attrib4 = new float[4];
    public float[] attrib5 = new float[4];
    public float[] attrib6 = new float[4];

    public Vertex(float x, float y, float z, float r, float g, float b, float a) {

        position[0] = x;
        position[1] = y;
        position[2] = z;
        color[0] = (byte) (Math.max(Math.min(r, 1.0f), 0.0f) * 255.5f);
        color[1] = (byte) (Math.max(Math.min(g, 1.0f), 0.0f) * 255.5f);
        color[2] = (byte) (Math.max(Math.min(b, 1.0f), 0.0f) * 255.5f);
        color[3] = (byte) (Math.max(Math.min(a, 1.0f), 0.0f) * 255.5f);
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer result = GLBuffers.newDirectByteBuffer(SIZEOF);
        for (int i = 0; i < 4; i++) {
            if (i < 3) {
                result.putFloat(PositionOffset + i * Float.BYTES, position[i]);
            }
            result.put(ColorOffset + i * Byte.BYTES, color[i]);
            result.putFloat(Attrib0Offset + i * Float.BYTES, attrib0[i]);
            result.putFloat(Attrib1Offset + i * Float.BYTES, attrib1[i]);
            result.putFloat(Attrib2Offset + i * Float.BYTES, attrib2[i]);
            result.putFloat(Attrib3Offset + i * Float.BYTES, attrib3[i]);
            result.putFloat(Attrib4Offset + i * Float.BYTES, attrib4[i]);
            result.putFloat(Attrib5Offset + i * Float.BYTES, attrib5[i]);
            result.putFloat(Attrib6Offset + i * Float.BYTES, attrib6[i]);
        }
        result.rewind();
        return result;
    }

//    @Override
//    public String toString() {
//        return "position: (" + position[0] + ", " + position[1] + ", " + position[2] + ") color: ("
//                + color[0] + ", " + color[1] + ", " + color[2] + ", " + color[3] + ")";
//    }

    public static final int PositionOffset = 0;
    public static final int ColorOffset = 3 * Float.BYTES; // 12
    public static final int Attrib0Offset = 3 * Float.BYTES + 4 * Byte.BYTES; // 16
    public static final int Attrib1Offset = (3 + 4 * 1) * Float.BYTES + 4 * Byte.BYTES; // 32
    public static final int Attrib2Offset = (3 + 4 * 2) * Float.BYTES + 4 * Byte.BYTES; // 48
    public static final int Attrib3Offset = (3 + 4 * 3) * Float.BYTES + 4 * Byte.BYTES; // 64
    public static final int Attrib4Offset = (3 + 4 * 4) * Float.BYTES + 4 * Byte.BYTES; // 80
    public static final int Attrib5Offset = (3 + 4 * 5) * Float.BYTES + 4 * Byte.BYTES; // 96
    public static final int Attrib6Offset = (3 + 4 * 6) * Float.BYTES + 4 * Byte.BYTES; // 112
    public static final int SIZEOF = (3 + 4 * 7) * Float.BYTES + 4 * Byte.BYTES; // 128
//    public static final int SIZEOF = 3 * Float.BYTES + 4 * Byte.BYTES; // 128
}
