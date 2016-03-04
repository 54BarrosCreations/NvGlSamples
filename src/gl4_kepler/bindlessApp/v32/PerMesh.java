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
package gl4_kepler.bindlessApp.v32;

import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import java.nio.ByteBuffer;

/**
 *
 * @author GBarbieri
 */
public class PerMesh {

    public float r, g, b, a, u, v;

    public static final int COUNT = 8;
    public static final int SIZE = COUNT * Float.BYTES;
    private float[] fa;

    public PerMesh() {
        fa = new float[6];
    }

    public PerMesh(Vec4 color, Vec2 uv) {
        r = color.x;
        g = color.y;
        b = color.z;
        a = color.w;
        u = uv.x;
        v = uv.y;
        fa = new float[6];
    }

    public void toBb(int offset, ByteBuffer bb) {
        bb
                .putFloat(offset + 0 * Float.BYTES, r)
                .putFloat(offset + 1 * Float.BYTES, g)
                .putFloat(offset + 2 * Float.BYTES, b)
                .putFloat(offset + 3 * Float.BYTES, a)
                .putFloat(offset + 4 * Float.BYTES, u)
                .putFloat(offset + 5 * Float.BYTES, v);
    }

    public void toBb(ByteBuffer bb) {
        bb.putFloat(r).putFloat(g).putFloat(b).putFloat(a).putFloat(u).putFloat(v);
    }

    public float[] toFa() {
        fa[0] = r;
        fa[1] = g;
        fa[2] = b;
        fa[3] = a;
        fa[4] = u;
        fa[5] = v;
        return fa;
    }

    public static final int ColorOffset = 0;
    public static final int UvOffset = Vec4.SIZE;
}
