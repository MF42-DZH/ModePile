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
package zeroxfc.nullpo.custom.libs.backgroundtypes;

public class ImageChunk {
	private int anchorType;
	private int[] anchorLocation;
	private int[] drawLocation;
	private int[] sourceLocation;
	private int[] sourceDimensions;
	private float[] scale;

	public static final int ANCHOR_POINT_TL = 0, ANCHOR_POINT_TM = 1, ANCHOR_POINT_TR = 2, ANCHOR_POINT_ML = 3, ANCHOR_POINT_MM = 4, ANCHOR_POINT_MR = 5, ANCHOR_POINT_LL = 6, ANCHOR_POINT_LM = 7, ANCHOR_POINT_LR = 8;

	public ImageChunk() {
		this(0, new int[] { 0, 0 }, new int[] { 0, 0 }, new int[] { 1, 1 }, new float[] { 1, 1 });
	}

	public ImageChunk(int anchorType, int[] anchorLocation, int[] sourceLocation, int[] sourceDimensions, float[] scale) {
		this.anchorType = anchorType;
		this.anchorLocation = anchorLocation;
		this.sourceLocation = sourceLocation;
		this.sourceDimensions = sourceDimensions;
		this.scale = scale;

		calibrateDrawLocation();
	}

	private void calibrateDrawLocation() {
		int[] ddim = getDrawDimensions();

		switch (anchorType) {
			case ANCHOR_POINT_TM:
				drawLocation = new int[] { anchorLocation[0] - (ddim[0] / 2), anchorLocation[1] };
				break;
			case ANCHOR_POINT_TR:
				drawLocation = new int[] { anchorLocation[0] - ddim[0], anchorLocation[1] };
				break;
			case ANCHOR_POINT_ML:
				drawLocation = new int[] { anchorLocation[0], anchorLocation[1] - (ddim[1] / 2) };
				break;
			case ANCHOR_POINT_MM:
				drawLocation = new int[] { anchorLocation[0] - (ddim[0] / 2), anchorLocation[1] - (ddim[1] / 2) };
				break;
			case ANCHOR_POINT_MR:
				drawLocation = new int[] { anchorLocation[0] - ddim[0], anchorLocation[1]  - (ddim[1] / 2) };
				break;
			case ANCHOR_POINT_LL:
				drawLocation = new int[] { anchorLocation[0], anchorLocation[1] - ddim[1] };
				break;
			case ANCHOR_POINT_LM:
				drawLocation = new int[] { anchorLocation[0] - (ddim[0] / 2), anchorLocation[1] - ddim[1] };
				break;
			case ANCHOR_POINT_LR:
				drawLocation = new int[] { anchorLocation[0] - ddim[0], anchorLocation[1] - ddim[1] };
				break;
			default:
				drawLocation = new int[] { anchorLocation[0], anchorLocation[1] };
				break;
		}
	}

	public int getAnchorType() {
		return anchorType;
	}

	public void setAnchorType(int anchorType) {
		this.anchorType = anchorType;
		calibrateDrawLocation();
	}

	public void setAnchorLocation(int[] anchorLocation) {
		this.anchorLocation = anchorLocation;
		calibrateDrawLocation();
	}

	public int[] getAnchorLocation() {
		return anchorLocation;
	}

	public void setSourceDimensions(int[] sourceDimensions) {
		this.sourceDimensions = sourceDimensions;
		calibrateDrawLocation();
	}

	public void setScale(float[] scale) {
		this.scale = scale;
		calibrateDrawLocation();
	}

	public float[] getScale() {
		return scale;
	}

	public int[] getSourceLocation() {
		return sourceLocation;
	}

	public int[] getSourceDimensions() {
		return sourceDimensions;
	}

	public int[] getDrawDimensions() {
		return new int[] { (int)(sourceDimensions[0] * scale[0]), (int)(sourceDimensions[1] * scale[1]) };
	}

	public int[] getDrawLocation() {
		return drawLocation;
	}
}
