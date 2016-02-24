Features:

- 40'000 objects
- UBOs, mutable, `glBufferData`, `glBufferSubData`
- Vertex Array Binding

```java
    private boolean updateUniformsEveryFrame = false;
    private boolean usePerMeshUniforms = false;
    private boolean renderTextures = false;

    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = false;
    public static boolean setVertexFormatOnEveryDrawCall = false;
```

~ 75.3 fps

```java
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = false;
```

```java
    public static boolean useVertexArray = true;
    public static boolean useHeavyVertexFormat = true;
    public static boolean setVertexFormatOnEveryDrawCall = true;
```

~ 20.6 fps

```java
    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = true;
    public static boolean setVertexFormatOnEveryDrawCall = true;
```

~ 17.8 fps

```java
    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = false;
    public static boolean setVertexFormatOnEveryDrawCall = true;
```

~ 27.6 fps

```java
    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = false;
    public static boolean setVertexFormatOnEveryDrawCall = false;
```

~ 40.8 fps

```java
    public static boolean useVertexArray = true;
    public static boolean useHeavyVertexFormat = false;
    public static boolean setVertexFormatOnEveryDrawCall = false;
```

~ 41.4 fps

```java
    public static boolean useVertexArray = true;
    public static boolean useHeavyVertexFormat = true;
    public static boolean setVertexFormatOnEveryDrawCall = false;
```

~ 40.0 fps

```java
    public static boolean useVertexArray = false;
    public static boolean useHeavyVertexFormat = true;
    public static boolean setVertexFormatOnEveryDrawCall = false;
```

~ 40.1 fps

```java
    private boolean updateUniformsEveryFrame = true;
    private boolean usePerMeshUniforms = true;
    private boolean renderTextures = true;

    public static boolean useVertexArray = true;
    public static boolean useHeavyVertexFormat = false;
    public static boolean setVertexFormatOnEveryDrawCall = false;
```

~ 40.8 fps

Same but passing the `renderTexture` via block instead declaring the same `constant` UBO also in the fragment shader

~ 40.4 fps
