# Contributing Guidelines

There are not many rules, but they are the following:

1. **Do not modify the vanilla source files that are not in this repository, even if it's to make some of its fields public.** This is to maintain cross-mod compatibility. If necessary, use Reflection or extensions. This is the most important guideline.
2. **If making engine-dependent methods, make sure as much equal support is to be given to all three engines (Slick, Swing and SDL) as possible.** This is to provide an equal experience to every user, no matter their choice of engine to run. If some features/functionality is impossible to implement in a certain engine, do not worry too much.
3. **Javadoc comments are essential on complex libraries.** Modes and mode-specific objects can be left alone at the contributor's discretion, but an explanation must be given if asked.

There are also a few optional guidelines that serve to make other contributors' lives easier:

1. **Make simpler/less-specific overloads of methods in libraries if existing methods are complicated.** This makes general-use cases easier to type and debug.
2. **When making *onCustom*-centric modes, try to use the structure similar to that in *GameEngine*.** This makes code easier to organise and debug.
