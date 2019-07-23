package zeroxfc.nullpo.custom.libs;

import java.util.Random;

public class WeightedRandomiser {
	private int[] results;
	private int[] cumulativeWeightList;
	private int maxWeight;
	private Random localRandom;
	
	public WeightedRandomiser(int[] weightArr, long seed) {
		int numberOfZeroes = 0;
		for (int i : weightArr) {
			if (i == 0) numberOfZeroes++;
		}
		
		cumulativeWeightList = new int[weightArr.length - numberOfZeroes];
		results = new int[weightArr.length - numberOfZeroes];
		maxWeight = 0;
		
		int ctr = 0;
		for (int i = 0; i < weightArr.length; i++) {
			if (weightArr[i] != 0) {
				maxWeight += weightArr[i];
				cumulativeWeightList[ctr] = maxWeight;
				results[ctr] = i;
				
				ctr++;
			}
		}
		
		localRandom = new Random(seed);
	}
	
	public void setWeights(int[] weightArr) {
		int numberOfZeroes = 0;
		for (int i : weightArr) {
			if (i == 0) numberOfZeroes++;
		}
		
		cumulativeWeightList = new int[weightArr.length - numberOfZeroes];
		results = new int[weightArr.length - numberOfZeroes];
		maxWeight = 0;
		
		int ctr = 0;
		for (int i = 0; i < weightArr.length; i++) {
			if (weightArr[i] != 0) {
				maxWeight += weightArr[i];
				cumulativeWeightList[ctr] = maxWeight;
				results[ctr] = i;
				
				ctr++;
			}
		}
	}
	
	public int getMax() {
		return results[results.length - 1];
	}
	
	public int nextInt() {
		int gVal = localRandom.nextInt(maxWeight) + 1;
		int result = 0;
		for (int i = 0; i < cumulativeWeightList.length; i++) {
			if (cumulativeWeightList[i] >= gVal) {
				result = i;
				break;
			}
		}
		
		return results[result];
	}
}
