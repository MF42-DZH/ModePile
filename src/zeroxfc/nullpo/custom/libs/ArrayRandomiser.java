package zeroxfc.nullpo.custom.libs;

import java.util.ArrayList;
// import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.Logger;

import mu.nu.nullpo.gui.swing.ResourceHolderSwing;

import java.util.Collections;
// import java.util.Iterator;
// import java.util.List;

public class ArrayRandomiser {
	// Internal randomiser
	private Random randomiser;
	
	/** Log */
	static Logger log = Logger.getLogger(ResourceHolderSwing.class);
	
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
