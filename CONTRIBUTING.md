# Contributing Guidelines

**There are not many rules, but they are the following:**

1. **Do not modify the vanilla source files that are not in this repository, even if it's to make some of its fields public.** This is to **maintain cross-mod compatibility**. If necessary, use reflection, wrapper classes or extension classes. This is the most important guideline. We do not want to 'lock-in' users to only using ModePile and no other mods.
2. **If making engine-dependent methods, make sure as much equal support is to be given to all three engines (Slick, Swing and SDL) as possible.** This is to provide an equal experience to every user, no matter their choice of engine to run. If some features/functionality is impossible (or far too slow) to implement in a certain engine, do not worry too much about skipping engine parity.
    * If any one engine needs to be prioritised for a feature, ensure it at least works in the **Slick** engine frontend, as that is the most commonly-used frontend for NullpoMino.
3. **Javadoc comments are essential on complex libraries.** Modes and mode-specific objects can be left alone at the contributor's discretion, but an explanation must be given if asked.
4. **If making your own modes, libraries or other class files to go into this pack, make your own root folder in the *src* directory (e.g. if you want your root to be *johndoe*, make a *johndoe* package root in the *src* folder).** This is for organisation purposes, and to give credit where credit is due. This also applies to any custom SFX that you use, but not custom images or BGM.
    * It is recommended that your packages follow the `[root].nullpo.custom` format that the existing packages use, with `modes` as the next subpackage for gamemodes and `libs` for library code. Any more packages underneath the two is up to your discretion.

**There are also a few optional guidelines that serve to make other contributors' lives easier:**

1. **Make simpler/less-specific overloads of methods in libraries if existing methods are complicated.** This makes general-use cases easier to type and debug.
2. **When making *onCustom*-centric modes, try to use the structure similar to that in *GameEngine*.** This makes code easier to organise and debug.

**Style guide:**

There is no specific style guideline to follow, but in general, 4-space indents and the OTBS style of braces are preferred.

```java
public int foo() {
    return 0; // Indented with 4 space (\x20, 32d) characters, not tabs.
}
```

Of course:

- Be polite.
- Be efficient.
- Have a plan to fix every bug you find.
