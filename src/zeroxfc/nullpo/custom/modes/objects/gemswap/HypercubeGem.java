package zeroxfc.nullpo.custom.modes.objects.gemswap;

import java.util.ArrayList;
import mu.nu.nullpo.game.component.Block;

public class HypercubeGem implements Gem {
    /**
     * Gem ID
     */
    private static final int ID = GemField.GEMID_HYPERCUBE;
    private final boolean special;
    /**
     * Class properties.
     */
    private int[] location;
    private int colour;
    private boolean recentSwap;
    private boolean recentFall;
    private boolean matchedHorizontal;
    private boolean matchedVertical;
    private boolean actionConducted;
    private boolean destroy;

    public HypercubeGem(int x, int y) {
        location = new int[] { x, y };
        this.colour = Block.BLOCK_COLOR_GEM_RAINBOW;

        recentSwap = false;
        recentFall = false;
        matchedHorizontal = false;
        matchedVertical = false;
        actionConducted = false;
        special = true;
        destroy = false;
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public int getColour() {
        return colour;
    }

    @Override
    public void setColour(int colour) {
        this.colour = colour;
    }

    @Override
    public int[] getLocation() {
        return location;
    }

    @Override
    public boolean getRecentSwap() {
        return recentSwap;
    }

    @Override
    public void setRecentSwap(boolean isRecentSwap) {
        recentSwap = isRecentSwap;
    }

    @Override
    public boolean getRecentFall() {
        return recentFall;
    }

    @Override
    public void setRecentFall(boolean isRecentFall) {
        recentFall = isRecentFall;
    }

    @Override
    public void setLocation(int x, int y) {
        location = new int[] { x, y };
    }

    @Override
    public int conductAction(GemField field, int[] args, ArrayList<ScoreEvent> eventList) {
        // THIS METHOD EXPECTS args[0] TO BE THE COLOUR OF THE GEM THAT TRIGGERED IT.
        actionConducted = true;
        destroy = true;

        int ctr = 0;
        int colourSelected = args[0];

        for (int y = 0; y < field.getHeight(); y++) {
            for (int x = 0; x < field.getWidth(); x++) {
                if (field.getCell(x, y).getColour() == colourSelected && !field.getCell(x, y).getActionConducted()) {
                    ctr++;
                    field.getCell(x, y).conductAction(field, new int[] { colour }, eventList);
                    field.getCell(x, y).setDestroy(true);
                } else if (colourSelected == Block.BLOCK_COLOR_GEM_RAINBOW && !field.getCell(x, y).getActionConducted()) {
                    ctr++;
                    field.getCell(x, y).conductAction(field, new int[] { colour }, eventList);
                    field.getCell(x, y).setDestroy(true);
                }
            }
        }

        eventList.add(new ScoreEvent(ctr, location[0], location[1], ID, colour));

        return ctr;
    }

    @Override
    public boolean getSpecial() {
        return special;
    }

    @Override
    public boolean getMatchedHorizontal() {
        return matchedHorizontal;
    }

    @Override
    public void setMatchedHorizontal(boolean isMatched) {
        matchedHorizontal = isMatched;
    }

    @Override
    public boolean getMatchedVertical() {
        return matchedVertical;
    }

    @Override
    public void setMatchedVertical(boolean isMatched) {
        matchedVertical = isMatched;
    }

    @Override
    public boolean getActionConducted() {
        return actionConducted;
    }

    @Override
    public void setActionConducted(boolean isConduted) {
        actionConducted = isConduted;
    }

    @Override
    public Gem getSelf() {
        return new HypercubeGem(location[0], location[1]);
    }

    @Override
    public boolean getDestroy() {
        // TODO Auto-generated method stub
        return destroy;
    }

    @Override
    public void setDestroy(boolean shouldDestroy) {
        // TODO Auto-generated method stub
        destroy = shouldDestroy;
    }

}
