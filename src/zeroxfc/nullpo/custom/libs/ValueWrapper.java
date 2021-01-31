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

public class ValueWrapper {
    public Byte valueByte;
    public Short valueShort;
    public Integer valueInt;
    public Long valueLong;
    public Float valueFloat;
    public Double valueDouble;

    public ValueWrapper() {
        this((byte) 0, (short) 0, 0, 0, 0, 0);
    }

    public ValueWrapper(byte value) {
        this(value, (short) 0, 0, 0, 0, 0);
    }

    public ValueWrapper(short value) {
        this((byte) 0, value, 0, 0, 0, 0);
    }

    public ValueWrapper(int value) {
        this((byte) 0, (short) 0, value, 0, 0, 0);
    }

    public ValueWrapper(long value) {
        this((byte) 0, (short) 0, 0, value, 0, 0);
    }

    public ValueWrapper(float value) {
        this((byte) 0, (short) 0, 0, 0, value, 0);
    }

    public ValueWrapper(double value) {
        this((byte) 0, (short) 0, 0, 0, 0, value);
    }

    public ValueWrapper(byte valueByte, short valueShort, int valueInt, long valueLong, float valueFloat, double valueDouble) {
        this.valueByte = valueByte;
        this.valueShort = valueShort;
        this.valueInt = valueInt;
        this.valueLong = valueLong;
        this.valueDouble = valueDouble;
        this.valueFloat = valueFloat;
    }

    public void copy(ValueWrapper vw) {
        this.valueByte = vw.valueByte;
        this.valueShort = vw.valueShort;
        this.valueInt = vw.valueInt;
        this.valueLong = vw.valueLong;
        this.valueDouble = vw.valueDouble;
        this.valueFloat = vw.valueFloat;
    }
}
