![ModePile Banner](https://github.com/MF42-DZH/ModePile/blob/master/GitHub%20ModePile%20Banner-Final.png)

# ModePile
A set of gameplay mods for [NullpoMino](https://github.com/nullpomino/nullpomino).

Requires the above to compile and run. The "res" directory in the "required files" needs to be merged with the one in the NullpoMino folder.

[![ModePile Pack CI](https://github.com/0xFC963F18DC21/ModePile/actions/workflows/main.yml/badge.svg?branch=master)](https://github.com/0xFC963F18DC21/ModePile/actions/workflows/main.yml)

## Modes Available:

Click the links that are available in the mode names to get a YouTube video demonstration.

* _2048_ - A 2048 clone.
* [_Accelerator_](https://www.youtube.com/watch?v=uRVD2QM7smU) - A marathon mode where playing fast is rewarded.
* [_Colour Power_](https://www.youtube.com/watch?v=3NJaC0kZKn4) - A marathon mode where clearing lines using pieces of different colours nets you different power-ups.
* [_Collapse_](https://www.youtube.com/watch?v=X6mryz4HaC8) - A clone of Super Collapse II's Traditional mode.
* [_Constantris_](https://www.youtube.com/watch?v=emwbuzGGwro) - A 電車でＧＯ！-inspired mode where you must advance to the next level at the correct time.
* [_Deltatris_](https://www.youtube.com/watch?v=BDLKE6J0DUs) - A ΔMAX-inspired mode where every piece pushes you closer to maximum velocity.
* [_Express Shipping_](https://www.youtube.com/watch?v=l9FZFv8zxUM) - A clone of the space-filling game Puzzle Express by HipSoft.
* [_EX Reborn_](https://www.youtube.com/watch?v=1GJJRNpn4Zk) - A clone of \*\*\*ris-EX.
* _Firework Challenge_ - An inaccurate version of \*\*\*ris the Grand Master 3's Easy mode.
* _Gem Swap_ (incomplete; on hold) - A generic match-3 game.
* [_Idiot% Mode_](https://www.youtube.com/watch?v=LlzxPgzajTQ) - A mode inspired by [this video](https://www.youtube.com/watch?v=omaDz_w4cgg) where it emulates the TGM style of play, but normal line clears are next-to-worthless but spin line clears are boosted to be better than normal.
* [_Joker_](https://www.youtube.com/watch?v=XW5ou46iz98) - A mode that approximates the JOKER mode from DTET.
* [_Marathon II_](https://www.youtube.com/watch?v=QxsDKW0oUgs) - "If Marathon was so good, why isn't there a Marathon 2?"
* [_Minesweeper_](https://www.youtube.com/watch?v=yvsnhtv1zvw) - It's Minesweeper. What else is there to say?
* _Mission Mode_ - Play a marathon mode, but it asks you to clear Puyo-Puyo-styled missions.
* [_Pong_](https://www.youtube.com/watch?v=Xpn3dzklIb8) - A single-player Pong clone.
* _Retro Mania 2_ - A mode based on *Sega \*\*\*ris '99*
* _Roll Training_ - Practice your credit roll plays here.
* [_Scanline_](https://www.youtube.com/watch?v=ouooFDdWdf4) - Inspired by Scanner mode on [\*\*\*ris for the PSP Mini](https://harddrop.com/wiki/Tetris_(PSP_Mini)) and [\*\*\*ris Pop](https://harddrop.com/wiki/Tetris_Pop).
* [_Score Trial_](https://www.youtube.com/watch?v=-hc6ggAOQf8) - An approximation of the SCORE ATTACK modes in DTET.
* [_Shadow Marahton_](https://www.youtube.com/watch?v=HxuRyM-g1TQ) - Build your own pieces in this extended Marathon.
* _Single Death Marathon_ - Marathon, but the pieces queue up to become big.
* _Subscriber Challenge_ - git gud, get subscriber!

## Libraries Available:

* _AnimatedBackgroundHook_ - Allows the simple animation of backgrounds, DTET/TI style.
* _ArrayRandomiser_ - Generates a permutation of an array.
* _BlockParticle_ - Animated block particle for line clear effects.
* _BlockParticleCollection_ - Collection class for easy generation, update and drawing of block particles.
* _BufferedPrimitiveDrawingHook_ - For fast primitive drawing.
* _DoubleVector_ - 2D vector type that uses doubles for more precision.
* _CustomResourceHolder_ - Allows the use of custom images and BGM in a mode.
* _DynamicReactiveSound_ (incomplete; on hold) - Generates and plays PCM samples on the fly.
* _ExamSpinner_ - TI-style spinner like the one used during Promotion Exam results.
* _FieldManipulation_ - Adds and fixes methods for dealing with the game's _Field_ type.
* _FieldScatter_ - A 2D implementation of Sega \*\*\*ris '99's block explosion.
* _FlyInOutText_ - Adds text objects that can fly into the screen from the outside, linger, then return to their starting positions before disappearing.
* _GameTextUtilities_ - Adds methods for coloured text and other random text / text obfuscation methods.
* _Interpolation_ - Linear interpolation functions.
* _MathHelper_ - A few extra mathematical functions such as LCM or GCD.
* _MouseParser_ - Allows the use of mouse inside a mode.
* _Particle_ - Particles for the particle engine.
* _ParticleEmitterBase_ - Particle emitter engine. Used for holding and drawing particles.
* _PhysicsObject_ - Allows the use of basic frictionless rigid body physics with square objects.
* _PrimitiveDrawingHook_ - For normal primitive drawing.
* _ProfileProperties_ - Allows the use of TI-styled profiles.
* _RendererExtension_ - Extra drawing methods.
* _ScrollingMarqueeText_ - Horizontal scrolling text.
* _ShakingText_ - Text that vibrates around a fixed point.
* _SideWaveText_ - _Super Collapse II_-styled text popups.
* _SoundLoader_ - Allows the use of custom SFX.
* _StaticFlyInText_ - Similar to _FlyInOutText_, but the text does not fly back out.
* _ValueWrapper_ - Class that holds a set of one of each primitive numbers.
* _WeightedRandomiser_ - A weighted pseudorandom number generator. 

## Compilation Instructions:

### With an IDE

1. Install JDK 1.8.0. OpenJDK is recommended.
2. Install your Java IDE of choice.
3. Download a source distribution of *specifically* NullpoMino 7.5.0. The bleeding edge commits will not work.
4. Import the ***whole*** NullpoMino directory as a project. Make sure all the classpaths are set up.
5. Merge the src directory from this repository and the one in the NullpoMino directory together.
6. Compilation should be working.

### Without an IDE

1. Perform steps 1-3, and 5 of the steps with IDE.
2. Set your working directory to inside the repository.
3. Run `build.bat` or `build.sh` depending on your OS.

## Installation Notes

### If a *bin* folder exists in the *NullpoMino* install folder

1. Follow the instructions in the release's *README.txt* as stated.

### In the case that a *bin* folder is non-existent in the *NullpoMino* install folder

1. Open *NullpoMino.jar* with an archiving program.
2. Create a folder in the root install directory for your copy of *NullpoMino* called *bin*.
3. Extract the folders in *NullpoMino.jar* to the newly-created *bin* folder.
4. Follow the instructions as given in the *README.txt* in the release.

## Resource Credits

* Deltarune Assets (*Jevil image, The World Revolving*) - Toby Fox
* Super Collapse II Assets (_SFX_) - Super Collapse II, released by GameHouse
* Sega \*\*\*ris Music - Sega

## Main Contributors

* [0xFC963F18DC21 / Azullia](https://github.com/MF42-DZH)
* [MandL27](https://github.com/MandL27)
* [Nightshade](https://github.com/snightshade)

## Special Thanks

* Oshisaure - A main inspiration for starting this modding project, a code design helper and motivator to allow this to continue.
* Nightshade - A main inspiration for starting this modding project, a code design helper and motivator to allow this to continue.
* GlitchyPSI - A main inspiration for starting this modding project, a code design helper (esp. for allowing the adaptation of their custom sound loader code) and motivator to allow this to continue.
* Akari - A main inspiration for starting this modding project (see *Idiot% Mode* above) and motivator to allow this to continue and for providing Japanese translations of the mode descriptions.
* The original creator of *NullpoMino*, NullNoName.
* All others that worked on *NullpoMino* - For helping to develop the game into the extensible platform we know today.
