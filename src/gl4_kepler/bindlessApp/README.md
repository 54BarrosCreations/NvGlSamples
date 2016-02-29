### Version 0.0

Features:

- 40'000 objects
- upload spare uniform, no UBOs
- Vertex Array Buffer and Vertex Array Object

Fastest:
```java
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = false;
    private boolean queryUniformsOnce = true;

    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = false;
```

~ 14.5 fps

### Version 1.0

Features:

- 40'000 objects
- UBOs, mutable, `glBufferData`, `glBufferSubData`
- Vertex Array Binding

Fastest:
```java
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = false;
    private boolean mapBuffers = false;

    public static boolean useVertexArray = true;
    public static boolean useHeavyVertexFormat = false;
    public static boolean setVertexFormatOnEveryDrawCall = false;
```

~ 41.4 fps

### Version 1.1

Features:

- condensing `perMesh` values allocating the maximum as possible for the ubo and setting a `perMesh` every alignment

~ 43.1 fps

### Version 1.2

Features:

- condensing even more `perMesh` values via an array of struct. To finish

### Version 1.3

Features:

- https://www.seas.upenn.edu/~pcozzi/OpenGLInsights/OpenGLInsights-AsynchronousBufferTransfers.pdf
- round robin fashion
- 40k UBOs

~ 40.5 fps

### Version 1.4

Features:

- https://www.seas.upenn.edu/~pcozzi/OpenGLInsights/OpenGLInsights-AsynchronousBufferTransfers.pdf
- orphaning with `glBufferData` and `null`

~ 40.4 fps

- orphaning with `glMapBufferRange` and `GL_MAP_INVALIDATE_BUFFER_BIT`

~ 20.6 fps

### Version 1.5

Features:

- https://www.seas.upenn.edu/~pcozzi/OpenGLInsights/OpenGLInsights-AsynchronousBufferTransfers.pdf
- synchronized with fences

~ broken

### Version 2.0

Features:

- immutable storages
- synchronized with fences

~ 0.9 fps

### Version 2.1

Features:

- immutable storages
- shader storage buffer objects (SSBO)
- synchronized with fences

~ 35.8 fps

### Version 2.2

Features:

- immutable storages
- shader storage buffer objects (SSBO) ring buffer
- synchronized with fences
- http://www.bfilipek.com/2015/01/persistent-mapped-buffers-benchmark.html
- https://github.com/nvMcJohn/apitest/blob/master/src/framework/bufferlock.cpp#L54

on 1 minute

1 ring buffer, a lot of stalls

~ 33.6 fps

2 ring buffer, few stalls

- 36.0 fps
 
3 ring buffer, zero stalls

- 37.7 fps

### Version 2.3

Features:

- `GL_UNIFORM_BUFFER` immutable storages `constant` and `transform`
- `GL_ARRAY_BUFFER` mutable storage `perMeshPointer` -> `glBufferSubData`

~ 50.3 fps

### Version 2.4

Features:

- `GL_UNIFORM_BUFFER` and `GL_ARRAY_BUFFER` immutable storages
- 3 ring buffer

~ 50.2 fps

### Version 3.0

Features:

- moved `perMesh` calculations inside the vertex shader

~ 65.0 fps, 15.30ms/frame

### Version 3.1

Features:

- moved `perMesh` calculations inside the vertex shader
- halfed data input from one integer (32b) to two unsigned bytes (2 * 8 = 16b)

~ 64.3 fps, 15.48ms/frame
