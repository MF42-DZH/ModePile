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
 *     - Only binaries of this library are allowed to be distributed unless at the
 *       permission of the Library Creator.
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
