# Proxied commandsenders

The CommandAPI has extra support for vanilla Minecraft's `/execute` command, by allowing the CommandSender to be an instance of the `ProxiedCommandSender` class. This allows the CommandSender to contain two extra pieces of information: The "proxied sender" and the original sender.

<div class="example">

### Example - Running a command as a chicken

Say we have a command which kills the sender of a command. This is easily implemented as follows:

<div class="multi-pre">

```java,Java
{{#include ../../commandapi-core/src/test/java/Examples.java:proxysender}}
```

```kotlin,Kotlin
{{#include ../../commandapi-core/src/test/kotlin/Examples.kt:proxysender}}
```

</div>

But what if the sender of the command is _not_ a player? By using Minecraft's `/execute` command, we could execute the command as _any_ arbitrary entity, as shown with the command below:

```mccmd
/execute as @e[type=chicken] run killme
```

To handle this case, we can use the `.executesProxy()` method to ensure that the command sender is a `ProxiedCommandSender`. Then, we can kill the `callee` _(the entity which is being 'forced' to run the command /killme)_

<div class="multi-pre">

```java,Java
{{#include ../../commandapi-core/src/test/java/Examples.java:proxysender2}}
```

```kotlin,Kotlin
{{#include ../../commandapi-core/src/test/kotlin/Examples.kt:proxysender2}}
```

</div>

This allows the command above to run successfully, killing all chickens it can find.

</div>
