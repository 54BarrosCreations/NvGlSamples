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

    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = false;
    public static boolean setVertexFormatOnEveryDrawCall = false;
```

~ 40.2 fps
