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
#version 450

#define SQRT_BUILDING_COUNT 200

#define POSITION    0
#define COLOR       1
#define PER_MESH    2
// reserved
#define ATTRIB0     4
#define ATTRIB1     5
#define ATTRIB2     6
#define ATTRIB3     7
#define ATTRIB4     8

#define TRANSFORM   0
#define CONSTANT    1

#define BLOCK       0

layout(std140, column_major) uniform;
layout(std430, column_major) buffer;

struct PerMesh
{
    vec4 color;
    vec2 uv;
    float radius;
    float t;
};

// Input attributes
layout (location = POSITION) in vec3 inPos;
layout (location = COLOR) in vec4 inColor;
layout (location = PER_MESH) in vec4 inPerMesh;
layout (location = PER_MESH + 1) in vec4 inPerMesh_;
layout (location = ATTRIB0) in vec4 inAttrib0;
layout (location = ATTRIB1) in vec4 inAttrib1; 
layout (location = ATTRIB2) in vec4 inAttrib2; 
layout (location = ATTRIB3) in vec4 inAttrib3; 
layout (location = ATTRIB4) in vec4 inAttrib4; 

// Uniforms
layout (binding = TRANSFORM) uniform Transform
{
    mat4 modelViewProjection;
} transform;

layout (binding = CONSTANT) uniform Constant
{
    int renderTexture;
} constant;

uniform sampler2D texture_;

// Outputs
layout (location = BLOCK) out Block
{
    PerMesh perMesh;
} outBlock;

PerMesh calculatePerMesh();

void main() 
{
    vec4 positionModelSpace = vec4(inPos, 1);

    PerMesh perMesh = calculatePerMesh();

    if (constant.renderTexture > 0) 
        positionModelSpace.y += texture(texture_, perMesh.uv).g;
    else 
        positionModelSpace.y += sin(positionModelSpace.y * perMesh.color.r) * .2f;

    gl_Position = transform.modelViewProjection * positionModelSpace;

    outBlock.perMesh.color = vec4(inColor.rgb * perMesh.color.rgb, inColor.a);
    outBlock.perMesh.uv = perMesh.uv;
}

PerMesh calculatePerMesh()
{
    PerMesh perMesh;
    
    float id = inPerMesh.x;
    float t = inPerMesh.y;

    float i = int(id) / SQRT_BUILDING_COUNT;
    float j = int(id) % SQRT_BUILDING_COUNT;

    float x = i / SQRT_BUILDING_COUNT - 0.5f;
    float z = j / SQRT_BUILDING_COUNT - 0.5f;

    float radius = sqrt(x * x + z * z);

    perMesh.color.r = sin(10.0f * radius + t);
    perMesh.color.g = cos(10.0f * radius + t);
    perMesh.color.b = radius;
    perMesh.color.a = 0.0f;
    perMesh.uv.x = j / SQRT_BUILDING_COUNT;
    perMesh.uv.y = 1 - i / SQRT_BUILDING_COUNT;

    return perMesh;
}