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

public class WeightedRandomiser {
    private final Random localRandom;
    private int[] results;
    private int[] cumulativeWeightList;
    private int maxWeight;

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
