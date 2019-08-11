package zeroxfc.nullpo.custom.libs;

public class ValueWrapper {
	public Byte valueByte;
	public Short valueShort;
	public Integer valueInt;
	public Long valueLong;
	public Float valueFloat;
	public Double valueDouble;
	
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

	public void copy(ValueWrapper vw) {
		this.valueByte = vw.valueByte;
		this.valueShort = vw.valueShort;
		this.valueInt = vw.valueInt;
		this.valueLong = vw.valueLong;
		this.valueDouble = vw.valueDouble;
		this.valueFloat = vw.valueFloat;
	}
}
