package zeroxfc.nullpo.custom.modes.objects.gemswap;

import java.util.ArrayList;

public interface Gem {
    int getID();

    int getColour();

    void setColour(int colour);

    int[] getLocation();

    boolean getRecentSwap();

    void setRecentSwap(boolean isRecentSwap);

    boolean getRecentFall();

    void setRecentFall(boolean isRecentFall);

    boolean getMatchedHorizontal();

    void setMatchedHorizontal(boolean isMatched);

    boolean getMatchedVertical();

    void setMatchedVertical(boolean isMatched);

    boolean getSpecial();

    boolean getActionConducted();

    void setActionConducted(boolean isConduted);

    boolean getDestroy();

    void setDestroy(boolean shouldDestroy);

    void setLocation(int x, int y);

    Gem getSelf();

    // Modifies field. This method is reserved for special gems.
    /*
     * TODO: Find a way to resolve special gem creation if two
     *       fall at the same time to complete its match type.
     */
    int conductAction(GemField field, int[] args, ArrayList<ScoreEvent> eventList);
}
