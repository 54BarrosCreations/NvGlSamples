//----------------------------------------------------------------------------------
// File:        gl4-kepler/BindlessApp/assets/shaders/simple_vertex.glsl
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
#version 400

// Input attributes
in vec3 inPos;
in vec4 inColor;
in vec4 inAttrib0;
in vec4 inAttrib1; 
in vec4 inAttrib2; 
in vec4 inAttrib3; 
in vec4 inAttrib4; 

// Outputs
smooth out vec4 color;
flat out vec2 uv;

// Uniforms
uniform int useTexture;

uniform mat4 modelViewProjection;

uniform float r;
uniform float g;
uniform float b;
uniform float a;
uniform float u;
uniform float v;

uniform sampler2D texture_;

void main() 
{
    vec4 positionModelSpace = vec4(inPos, 1);

    if (useTexture > 0) 
        positionModelSpace.y += texture(texture_, vec2(u, v)).g;
    else 
        positionModelSpace.y += sin(positionModelSpace.y * r) * .2f;

    gl_Position = modelViewProjection * positionModelSpace;

    color.r = inColor.r * r;
    color.g = inColor.g * g;
    color.b = inColor.b * b;
    color.a = inColor.a;
    uv.x = u;
    uv.y = v;

    //gl_Position = modelViewProjection * vec4(0.1 * float(gl_VertexID % 2), 0.1 * float(gl_VertexID / 2), 0.0, 1.0);
}
