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
package gl4_kepler.bindlessApp.v14;

import dev.Vec4u8;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class Vertex {

    public Vec3 position;
    public Vec4u8 color;
    public Vec4 attrib0;
    public Vec4 attrib1;
    public Vec4 attrib2;
    public Vec4 attrib3;
    public Vec4 attrib4;

    public Vertex(float x, float y, float z, float r, float g, float b, float a) {

        position = new Vec3(x, y, z);
        color = new Vec4u8(
                (byte) (Math.max(Math.min(r, 1.0f), 0.0f) * 255.5f),
                (byte) (Math.max(Math.min(g, 1.0f), 0.0f) * 255.5f),
                (byte) (Math.max(Math.min(b, 1.0f), 0.0f) * 255.5f),
                (byte) (Math.max(Math.min(a, 1.0f), 0.0f) * 255.5f));
        attrib0 = new Vec4();
        attrib1 = new Vec4();
        attrib2 = new Vec4();
        attrib3 = new Vec4();
        attrib4 = new Vec4();
    }

    public void toBb(ByteBuffer bb) {
        bb
                .putFloat(position.x).putFloat(position.y).putFloat(position.z)
                .put(color.x).put(color.y).put(color.z).put(color.w)
                .putFloat(attrib0.x).putFloat(attrib0.y).putFloat(attrib0.z).putFloat(attrib0.w)
                .putFloat(attrib1.x).putFloat(attrib1.y).putFloat(attrib1.z).putFloat(attrib1.w)
                .putFloat(attrib2.x).putFloat(attrib2.y).putFloat(attrib2.z).putFloat(attrib2.w)
                .putFloat(attrib3.x).putFloat(attrib3.y).putFloat(attrib3.z).putFloat(attrib3.w)
                .putFloat(attrib4.x).putFloat(attrib4.y).putFloat(attrib4.z).putFloat(attrib4.w);
    }

    public static final int PositionOffset = 0;
    public static final int ColorOffset = Vec3.SIZE; // 12
    public static final int Attrib0Offset = Vec3.SIZE + Vec4u8.SIZE; // 16
    public static final int Attrib1Offset = Vec3.SIZE + Vec4u8.SIZE + Vec4.SIZE; // 32
    public static final int Attrib2Offset = Vec3.SIZE + Vec4u8.SIZE + 2 * Vec4.SIZE; // 48
    public static final int Attrib3Offset = Vec3.SIZE + Vec4u8.SIZE + 3 * Vec4.SIZE; // 64
    public static final int Attrib4Offset = Vec3.SIZE + Vec4u8.SIZE + 4 * Vec4.SIZE; // 80
    public static final int SIZE = Vec3.SIZE + Vec4u8.SIZE + 5 * Vec4.SIZE; // 96
}
