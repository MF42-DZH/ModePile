package zeroxfc.nullpo.custom.libs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import org.apache.log4j.Logger;
// import java.util.Iterator;
// import java.util.List;

public class ArrayRandomiser {
    /**
     * Log
     */
    static Logger log = Logger.getLogger(ResourceHolderSwing.class);
    // Internal randomiser
    private final Random randomiser;

    public ArrayRandomiser() {
        randomiser = new Random();
    }

    public ArrayRandomiser(long seed) {
        randomiser = new Random(seed);
    }

    public int[] permute(int[] arr) {
        int[] h = arr.clone();

        ArrayList<Integer> copy = new ArrayList<Integer>();
        for (int integer : arr) {
            copy.add(integer);
        }

        Collections.shuffle(copy, randomiser);

        for (int i = 0; i < copy.size(); i++) {
            h[i] = copy.get(i).intValue();
        }

        return h;
    }
}
