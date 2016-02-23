### Version 0

Features:

- 40'000 objects
- upload spare uniform, no UBOs
- Vertex Array Buffer and Vertex Array Object

```java
    private boolean updateUniformsEveryFrame = false;
    private boolean usePerMeshUniforms = false;
    private boolean renderTextures = false;

    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = false;
    public static int drawCallsPerState = 1;
```

~ 45 fps

```java
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = false;

    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = false;
    public static int drawCallsPerState = 1;
```

~ 14.2 fps

```java
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = false;

    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = true;
    public static int drawCallsPerState = 1;
```

~ 12 fps

```java
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = false;

    public static boolean useVertexArray = true;
    public static boolean useHeavyVertexFormat = false;
    public static int drawCallsPerState = 1;
```

~ 13.3 fps

```java
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = true;

    public static boolean useVertexArray = true;
    public static boolean useHeavyVertexFormat = false;
    public static int drawCallsPerState = 1;
```

~ 13.1 fps

```java
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = true;

    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = false;
    public static int drawCallsPerState = 1;
```

~ 14.3 fps
