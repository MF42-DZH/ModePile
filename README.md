![ModePile Banner](https://github.com/Shots243/ModePile/blob/master/GitHub%20ModePile%20Banner-Final.png)

# ModePile
A set of gameplay mods for [NullpoMino](https://github.com/nullpomino/nullpomino).

Requires the above to compile and run. The "res" directory in the "required files" needs to be merged with the one in the NullpoMino folder.

## Modes Available:

* _2048_ - A 2048 clone.
* _Accelerator_ - A marathon mode where playing fast is rewarded.
* _Colour Power_ - A marathon mode where clearing lines using pieces of different colours nets you different power-ups.
* _Express Shipping_ - A clone of the space-filling game Puzzle Express by HipSoft.
* _EX Reborn_ - A clone of Tetris-EX.
* _Firework Challenge_ - An inaccurate version of Tetris the Grand Master 3's Easy mode.
* _Gem Swap_ (incomplete) - A generic match-3 game.
* _Idiot% Mode_ - A mode inspired by [this video](https://www.youtube.com/watch?v=omaDz_w4cgg) where it emulates the TGM style of play, but normal line clears are next-to-worthless but spin line clears are boosted to be better than normal.
* _Joker_ - A mode that approximates the JOKER mode from DTET.
* _Minesweeper_ - It's Minesweeper. What else is there to say?
* _Mission Mode_ - Play a marathon mode, but it asks you to clear Puyo-Puyo-styled missions.
* _Scanline_ - Inspired by Scanner mode on [Tetris for the PSP Mini](https://harddrop.com/wiki/Tetris_(PSP_Mini)) and [Tetris Pop](https://harddrop.com/wiki/Tetris_Pop).
* _Score Trial_ - An approximation of the SCORE ATTACK modes in DTET.
* _Single Death Marathon_ - Marathon, but the pieces queue up to become big.

## Libraries Available:

* _ArrayRandomiser_ - Generates a permutation of an array.
* _BlockParticle_ - Animated block particle for line clear effects.
* _BlockParticleCollection_ - Collection class for easy generation, update and drawing of block particles.
* _DoubleVector_ - 2D vector type that uses doubles for more precision.
* _FieldExtension_ - Adds and fixes methods for dealing with the game's _Field_ type.
* _FlyInOutText_ - Adds text objects that can fly into the screen from the outside, linger, then return to their starting positions before disappearing.
* _Interpolation_ - Linear interpolation functions.
* _IntWrapper_ - Class that holds a single int, designed to handle the counting of recursive function calls.
* _MouseParser_ - Allows the use of mouse inside a mode.
* _ResourceHolderCustomAssetExtension_ - Allows the use of custom images and BGM in a mode.
* _ScrollingMarqueeText_ - Horizontal scrolling text.
* _SoundLoader_ - Allows the use of custom SFX.
* _StaticFlyInText_ - Similar to _FlyInOutText_, but the text does not fly back out.
* _WeightedRandomiser_ - A weighted pseudorandom number generator. 

## Compilation Instructions:

1. Install JDK 1.8.0. Either use OpenJDK by RedHat or use the official Oracle JDK.
2. Install your Java IDE of choice.
3. Import the ***whole*** NullpoMino directory as a project. Make sure all the classpaths are set up.
4. Merge the src directory from this repository and the one in the NullpoMino directory together.
5. Compilation should be working.

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
