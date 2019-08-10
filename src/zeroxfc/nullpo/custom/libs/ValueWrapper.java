package zeroxfc.nullpo.custom.libs;

public class ValueWrapper {
	public byte valueByte;
	public short valueShort;
	public int valueInt;
	public long valueLong;
	public float valueFloat;
	public double valueDouble;
	
	public ValueWrapper() {
		this((byte)0, (short)0, 0, 0, 0, 0);
	}

	public ValueWrapper(byte value) {
		this(value, (short)0, 0, 0, 0, 0);
	}
	public ValueWrapper(short value) {
		this((byte)0, value, 0, 0,0, 0);
	}

	public ValueWrapper(int value) {
		this((byte)0, (short)0, value, 0, 0, 0);
	}

	public ValueWrapper(long value) {
		this((byte)0, (short)0, 0, value, 0, 0);
	}

	public ValueWrapper(float value) {
		this((byte)0, (short)0, 0, 0, value, 0);
	}

	public ValueWrapper(double value) {
		this((byte)0, (short)0,0, 0, 0, value);
	}

	public ValueWrapper(byte valueByte, short valueShort, int valueInt, long valueLong, float valueFloat, double valueDouble) {
		this.valueByte = valueByte;
		this.valueShort = valueShort;
		this.valueInt = valueInt;
		this.valueLong = valueLong;
		this.valueDouble = valueDouble;
		this.valueFloat = valueFloat;
	}
}
