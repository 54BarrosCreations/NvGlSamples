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
#version 420
#extension GL_NV_shader_buffer_load : require
#extension GL_NV_bindless_texture : require
#extension GL_NV_gpu_shader5 : require // uint64_t

// Input attributes
layout in vec4 inPos;
layout in vec4 inColor;
layout in vec4 inAttrib0;
layout in vec4 inAttrib1; 
layout in vec4 inAttrib2; 
layout in vec4 inAttrib3; 
layout in vec4 inAttrib4; 

// Outputs
layout smooth out vec4 outColor;
layout flat out vec2 outUV;

// Uniforms
uniform int useTextures;
uniform int currentFrame;
uniform mat4 modelView;
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
    vec4 positionModelSpace = inPos;
    if (useTextures>0) 
        positionModelSpace.y += texture(texture_, vec2(u, v)).g;
    else 
        positionModelSpace.y += sin(positionModelSpace.y * r) * .2f;

    gl_Position = modelViewProjection * positionModelSpace;

    outColor.r = inColor.r * r;
    outColor.g = inColor.g * g;
    outColor.b = inColor.b * b;
    outColor.a = inColor.a;
    outUV.x = u;
    outUV.y = v;
}
