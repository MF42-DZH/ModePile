package zeroxfc.nullpo.custom.modes.objects.gemswap;

import java.util.ArrayList;

public interface Gem {
    int getID();

    int getColour();

    int[] getLocation();

    boolean getRecentSwap();

    boolean getRecentFall();

    boolean getMatchedHorizontal();

    boolean getMatchedVertical();

    boolean getSpecial();

    boolean getActionConducted();

    boolean getDestroy();

    void setColour( int colour );

    void setLocation( int x, int y );

    void setRecentSwap( boolean isRecentSwap );

    void setRecentFall( boolean isRecentFall );

    void setMatchedHorizontal( boolean isMatched );

    void setMatchedVertical( boolean isMatched );

    void setActionConducted( boolean isConduted );

    void setDestroy( boolean shouldDestroy );

    Gem getSelf();

    // Modifies field. This method is reserved for special gems.
    /*
     * TODO: Find a way to resolve special gem creation if two
     *       fall at the same time to complete its match type.
     */
    int conductAction( GemField field, int[] args, ArrayList< ScoreEvent > eventList );
}
