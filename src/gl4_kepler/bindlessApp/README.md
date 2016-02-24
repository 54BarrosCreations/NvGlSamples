### Version 0

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

### Version 1

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

### Version 2

Features:

- condensing `perMesh` values allocating the maximum as possible for the ubo

~ 43.1 fps

### Version 3

Features:

- https://www.seas.upenn.edu/~pcozzi/OpenGLInsights/OpenGLInsights-AsynchronousBufferTransfers.pdf, 
- round robin fashion
- 40k UBOs

~ 40.5 fps

