/*
 * This library class was created by 0xFC963F18DC21 / Shots243
 * It is part of an extension library for the game NullpoMino (copyright 2010)
 *
 * Herewith shall the term "Library Creator" be given to 0xFC963F18DC21.
 * Herewith shall the term "Game Creator" be given to the original creator of NullpoMino.
 *
 * THIS LIBRARY AND MODE PACK WAS NOT MADE IN ASSOCIATION WITH THE GAME CREATOR.
 *
 * Repository: https://github.com/Shots243/ModePile
 *
 * When using this library in a mode / library pack of your own, the following
 * conditions must be satisfied:
 *     - This license must remain visible at the top of the document, unmodified.
 *     - You are allowed to use this library for any modding purpose.
 *         - If this is the case, the Library Creator must be credited somewhere.
 *             - Source comments only are fine, but in a README is recommended.
 *     - Modification of this library is allowed, but only in the condition that a
 *       pull request is made to merge the changes to the repository.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package zeroxfc.nullpo.custom.libs;

import java.util.Random;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

public class StaticFlyInText {
    // String to draw
    private String mainString;

    // Timings
    private int flyInTime;

    // Lifetime variable
    private int currentLifetime;

    // Vector array of positions
    private DoubleVector[] letterPositions;

    // Start location
    private DoubleVector[] startLocation;

    // Destination vector
    private DoubleVector[] destinationLocation;

    // Colour
    private int textColour;

    public int getTextColour() {
        return textColour;
    }

    public void setTextColour( int textColour ) {
        this.textColour = textColour;
    }

    // Randomiser for start pos
    private Random positionRandomiser;

    // Text scale
    private float textScale;

    /**
     * Create a text object that has its letters fly in from various points on the edges and stays in one spot.
     *
     * @param text         String to draw.
     * @param destinationX X-position of pixel of destination.
     * @param destinationY Y-position of pixel of destination.
     * @param timeIn       Frames to fly in.
     * @param colour       Text colour.
     * @param scale        Text scale (0.5f, 1.0f, 2.0f).
     * @param seed         Random seed for position.
     */
    public StaticFlyInText( String text, int destinationX, int destinationY, int timeIn, int colour, float scale, long seed ) {
        mainString = text;
        flyInTime = timeIn;
        textColour = colour;
        textScale = scale;
        positionRandomiser = new Random( seed );

        letterPositions = new DoubleVector[ mainString.length() ];
        startLocation = new DoubleVector[ mainString.length() ];
        destinationLocation = new DoubleVector[ mainString.length() ];

        int sMod = 16;
        if ( textScale == 2.0f ) sMod = 32;
        if ( textScale == 0.5f ) sMod = 16;

        for ( int i = 0; i < mainString.length(); i++ ) {
            int startX = 0, startY = 0;
            DoubleVector position = DoubleVector.zero();

            double dec1 = positionRandomiser.nextDouble();
            double dec2 = positionRandomiser.nextDouble();

            if ( dec1 < 0.5 ) {
                startX = -sMod;
                if ( dec2 < 0.5 ) startX = 41 * sMod;

                startY = ( int ) ( positionRandomiser.nextDouble() * ( 32 * sMod ) ) - sMod;
            } else {
                startY = -sMod;
                if ( dec2 < 0.5 ) startY = 31 * sMod;

                startX = ( int ) ( positionRandomiser.nextDouble() * ( 42 * sMod ) ) - sMod;
            }

            position = new DoubleVector( startX, startY, false );

            letterPositions[ i ] = position;
            startLocation[ i ] = position;
            destinationLocation[ i ] = new DoubleVector( destinationX + ( sMod * i ), destinationY, false );
        }

        currentLifetime = 0;
    }

    /**
     * Updates the position and lifetime of this object.
     */
    public void update() {
        if ( currentLifetime < flyInTime ) {
            currentLifetime++;

            for ( int i = 0; i < letterPositions.length; i++ ) {
                int v1 = ( int ) ( Interpolation.lerp( startLocation[ i ].getX(), destinationLocation[ i ].getX(), ( ( double ) ( currentLifetime ) / flyInTime ) ) );
                int v2 = ( int ) ( Interpolation.lerp( startLocation[ i ].getY(), destinationLocation[ i ].getY(), ( ( double ) ( currentLifetime ) / flyInTime ) ) );
                letterPositions[ i ] = new DoubleVector( v1, v2, false );
            }
        }
    }

    /**
     * Draws the text at its current position.
     */
    public void draw( GameEngine engine, EventReceiver receiver, int playerID ) {
        for ( int j = 0; j < letterPositions.length; j++ ) {
            receiver.drawDirectFont( engine, playerID,
                    ( int ) letterPositions[ j ].getX(), ( int ) letterPositions[ j ].getY(),
                    String.valueOf( mainString.charAt( j ) ),
                    textColour, textScale );
        }
    }
}
