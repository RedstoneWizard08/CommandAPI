# Itemstack arguments

![An item stack argument with suggestions for Minecraft items](./images/arguments/itemstack.png)

The `ItemStackArgument` class represents in-game items. As expected, this should be casted to Bukkit's `ItemStack` object. The `ItemStack` which is returned by the `ItemStackArgument` always has a size of 1.

<div class="example">

### Example - Giving a player an itemstack

Say we want to create a command that gives you items. For this command, we will use the following syntax:

```mccmd
/item <itemstack>
```

With this syntax, we can easily create our command:

<div class="multi-pre">

```java,Java
{{#include ../../commandapi-core/src/test/java/Examples.java:itemstackarguments}}
```

```kotlin,Kotlin
{{#include ../../commandapi-core/src/test/kotlin/Examples.kt:itemstackarguments}}
```

</div>

</div>
