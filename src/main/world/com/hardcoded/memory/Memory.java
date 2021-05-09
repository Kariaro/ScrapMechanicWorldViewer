package com.hardcoded.memory;

/**
 * @author HardCoded
 */
public class Memory {
	private final byte[] bytes;
	private boolean defaultEndian;
	private int index;
	
	public Memory(int capacity) {
		this(null, capacity);
	}
	
	public Memory(byte[] bytes) {
		this(bytes, bytes.length);
	}
	
	public Memory(byte[] bytes, int capacity) {
		byte[] array = new byte[capacity];
		
		if(bytes != null) 
			System.arraycopy(bytes, 0, array, 0, Math.min(bytes.length, capacity));
	
		this.bytes = array;
	}
	
	// ======================= //
	
	public Memory setDefaultBigEndian(boolean b) {
		defaultEndian = b;
		return this;
	}
	
	public Memory set(int i) {
		index = i;
		return this;
	}
	
	public Memory move(int i) {
		index += i;
		return this;
	}
	
	public byte[] data() {
		return bytes;
	}
	
	public int index() {
		return index;
	}
	
	// ======================= //
	
	private long readValue(int offset, int length, boolean bigEndian) {
		long result = 0;
		
		for(int i = 0; i < length; i++) {
			long val = Byte.toUnsignedLong(bytes[index + offset + i]);
			long shr = (bigEndian ? (length - 1 - i):i) * 8L;
			result |= (val << shr);
		}
		
		return result;
	}
	
	private Memory writeValue(Number number, int offset, int length, boolean bigEndian) {
		long value = 0;
		
		if(number instanceof Byte) value = Byte.toUnsignedLong(number.byteValue());
		if(number instanceof Short) value = Short.toUnsignedLong(number.shortValue());
		if(number instanceof Integer) value = Integer.toUnsignedLong(number.intValue());
		if(number instanceof Long) value = number.longValue();
		if(number instanceof Float) value = Integer.toUnsignedLong(Float.floatToIntBits(number.floatValue()));
		if(number instanceof Double) value = Double.doubleToRawLongBits(number.doubleValue());
		
		for(int i = 0; i < length; i++) {
			long shr = (bigEndian ? (length - 1 - i):i) * 8L;
			byte val = (byte)((value >>> shr) & 0xff);
			bytes[offset + i] = val;
		}
		
		return this;
	}
	
	// ======================= //
	
	public byte Byte() { return Byte(0); }
	public byte Byte(int offset) { return bytes[index + offset]; }
	public int UnsignedByte() { return UnsignedByte(0); }
	public int UnsignedByte(int offset) { return Byte.toUnsignedInt(bytes[index + offset]); }
	public Memory WriteByte(int value) { return WriteByte(value, 0); }
	public Memory WriteByte(int value, int offset) {
		bytes[index + offset] = (byte)value;
		return this;
	}
	
	public byte NextByte() {
		byte result = Byte();
		index += 1;
		return result;
	}
	
	public int NextUnsignedByte() {
		int result = UnsignedByte();
		index += 1;
		return result;
	}
	
	public Memory NextWriteByte(int value) {
		WriteByte(value);
		index += 1;
		return this;
	}
	
	// ======================= //
	
	public short Short() { return Short(0, defaultEndian); }
	public short Short(int offset) { return Short(offset, defaultEndian); }
	public short Short(boolean bigEndian) { return Short(0, bigEndian); }
	public short Short(int offset, boolean bigEndian) { return (short)readValue(offset, 2, bigEndian); }
	public int UnsignedShort() { return UnsignedShort(0, defaultEndian); }
	public int UnsignedShort(int offset) { return UnsignedShort(offset, defaultEndian); }
	public int UnsignedShort(boolean bigEndian) { return UnsignedShort(0, bigEndian); }
	public int UnsignedShort(int offset, boolean bigEndian) { return Short.toUnsignedInt(Short(offset, bigEndian)); }
	public Memory WriteShort(int value) { return WriteShort(value, 0, defaultEndian); }
	public Memory WriteShort(int value, int offset) { return WriteShort(value, offset, defaultEndian); }
	public Memory WriteShort(int value, boolean bigEndian) { return WriteShort(value, 0, bigEndian); }
	public Memory WriteShort(int value, int offset, boolean bigEndian) { return writeValue((short)value, offset, 2, bigEndian); }
	
	public short NextShort() { return NextShort(defaultEndian); }
	public short NextShort(boolean bigEndian) {
		short result = Short(0, bigEndian);
		index += 2;
		return result;
	}
	
	public int NextUnsignedShort() { return NextUnsignedShort(defaultEndian); }
	public int NextUnsignedShort(boolean bigEndian) {
		int result = UnsignedShort(0, bigEndian);
		index += 2;
		return result;
	}
	
	public Memory NextWriteShort(int value) { return NextWriteShort(value, defaultEndian); }
	public Memory NextWriteShort(int value, boolean bigEndian) {
		WriteShort(value, 0, bigEndian);
		index += 2;
		return this;
	}
	
	// ======================= //
	
	public int Int() { return Int(0, defaultEndian); }
	public int Int(int offset) { return Int(offset, defaultEndian); }
	public int Int(boolean bigEndian) { return Int(0, bigEndian); }
	public int Int(int offset, boolean bigEndian) { return (int)readValue(offset, 4, bigEndian); }
	public long UnsignedInt() { return UnsignedInt(0, defaultEndian); }
	public long UnsignedInt(int offset) { return UnsignedInt(offset, defaultEndian); }
	public long UnsignedInt(boolean bigEndian) { return UnsignedInt(0, bigEndian); }
	public long UnsignedInt(int offset, boolean bigEndian) { return Integer.toUnsignedLong(Int(offset, bigEndian)); }
	public Memory WriteInt(int value) { return WriteInt(value, 0, defaultEndian); }
	public Memory WriteInt(int value, int offset) { return WriteInt(value, offset, defaultEndian); }
	public Memory WriteInt(int value, boolean bigEndian) { return WriteInt(value, 0, bigEndian); }
	public Memory WriteInt(int value, int offset, boolean bigEndian) { return writeValue(value, offset, 4, bigEndian); }
	
	public int NextInt() { return NextInt(defaultEndian); }
	public int NextInt(boolean bigEndian) {
		int result = Int(0, bigEndian);
		index += 4;
		return result;
	}
	
	public long NextUnsignedInt() { return NextUnsignedInt(defaultEndian); }
	public long NextUnsignedInt(boolean bigEndian) {
		long result = UnsignedInt(0, bigEndian);
		index += 4;
		return result;
	}
	
	public Memory NextWriteInt(int value) { return NextWriteInt(value, defaultEndian); }
	public Memory NextWriteInt(int value, boolean bigEndian) {
		WriteInt(value, 0, bigEndian);
		index += 4;
		return this;
	}
	
	// ======================= //
	
	public long Long() { return Long(0, defaultEndian); }
	public long Long(int offset) { return Long(offset, defaultEndian); }
	public long Long(boolean bigEndian) { return Long(0, bigEndian); }
	public long Long(int offset, boolean bigEndian) { return readValue(offset, 8, bigEndian); }
	public Memory WriteLong(long value) { return WriteLong(value, 0, defaultEndian); }
	public Memory WriteLong(long value, int offset) { return WriteLong(value, offset, defaultEndian); }
	public Memory WriteLong(long value, boolean bigEndian) { return WriteLong(value, 0, bigEndian); }
	public Memory WriteLong(long value, int offset, boolean bigEndian) { return writeValue(value, offset, 8, bigEndian); }
	
	public long NextLong() { return NextLong(defaultEndian); }
	public long NextLong(boolean bigEndian) {
		long result = Long(0, bigEndian);
		index += 8;
		return result;
	}
	
	public Memory NextWriteLong(long value) { return NextWriteLong(value, defaultEndian); }
	public Memory NextWriteLong(long value, boolean bigEndian) {
		WriteLong(value, 0, bigEndian);
		index += 8;
		return this;
	}
	
	// ======================= //
	
	public float Float() { return Float(0, defaultEndian); }
	public float Float(int offset) { return Float(offset, defaultEndian); }
	public float Float(boolean bigEndian) { return Float(0, bigEndian); }
	public float Float(int offset, boolean bigEndian) { return Float.intBitsToFloat(Int(offset, bigEndian)); }
	public Memory WriteFloat(float value) { return WriteFloat(value, 0, defaultEndian); }
	public Memory WriteFloat(float value, int offset) { return WriteFloat(value, offset, defaultEndian); }
	public Memory WriteFloat(float value, boolean bigEndian) { return WriteFloat(value, 0, bigEndian); }
	public Memory WriteFloat(float value, int offset, boolean bigEndian) { return WriteInt(Float.floatToIntBits(value), offset, bigEndian); }
	public float NextFloat() { return NextFloat(defaultEndian); }
	public float NextFloat(boolean bigEndian) { return Float.intBitsToFloat(NextInt(bigEndian)); }
	public Memory NextWriteFloat(float value) { return NextWriteFloat(value, defaultEndian); }
	public Memory NextWriteFloat(float value, boolean bigEndian) { return NextWriteInt(Float.floatToIntBits(value), bigEndian); }
	
	// ======================= //
	
	public double Double() { return Double(0, defaultEndian); }
	public double Double(int offset) { return Double(offset, defaultEndian); }
	public double Double(boolean bigEndian) { return Double(0, bigEndian); }
	public double Double(int offset, boolean bigEndian) { return Double.longBitsToDouble(Long(offset, bigEndian)); }
	public Memory WriteDouble(double value) { return WriteDouble(value, 0, defaultEndian); }
	public Memory WriteDouble(double value, int offset) { return WriteDouble(value, offset, defaultEndian); }
	public Memory WriteDouble(double value, boolean bigEndian) { return WriteDouble(value, 0, bigEndian); }
	public Memory WriteDouble(double value, int offset, boolean bigEndian) { return WriteLong(Double.doubleToLongBits(value), offset, bigEndian); }
	public double NextDouble() { return NextDouble(defaultEndian); }
	public double NextDouble(boolean bigEndian) { return Double.longBitsToDouble(NextLong(bigEndian)); }
	public Memory NextWriteDouble(double value) { return NextWriteDouble(value, defaultEndian); }
	public Memory NextWriteDouble(double value, boolean bigEndian) { return NextWriteLong(Double.doubleToLongBits(value), bigEndian); }
	
	// ======================= //
	
	public String String(int length) { return String(length, 0, false); }
	public String String(int length, int offset) { return String(length, offset, false); }
	public String String(int length, boolean reverse) { return String(length, 0, reverse); }
	public String String(int length, int offset, boolean reverse) { return new String(Bytes(length, offset, reverse)); }
	public Memory WriteString(String value) { return WriteString(value, value.length(), 0, false); }
	public Memory WriteString(String value, int length) { return WriteString(value, length, 0, false); }
	public Memory WriteString(String value, int length, int offset) { return WriteString(value, length, offset, false); }
	public Memory WriteString(String value, int length, boolean reverse) { return WriteString(value, length, 0, reverse); }
	public Memory WriteString(String value, int length, int offset, boolean reverse) { return WriteBytes(value.getBytes(), length, offset, reverse); }
	public String NextString(int length) { return NextString(length, false); }
	public String NextString(int length, boolean reverse) { return new String(NextBytes(length, reverse)); }
	public Memory NextWriteString(String value) { return NextWriteString(value, value.length(), false); }
	public Memory NextWriteString(String value, int length) { return NextWriteString(value, length, false); }
	public Memory NextWriteString(String value, int length, boolean reverse) { return NextWriteBytes(value.getBytes(), length, reverse); }
	
	// ======================= //
	
	public byte[] Bytes(int length) { return Bytes(length, 0, false); }
	public byte[] Bytes(int length, int offset) { return Bytes(length, offset, false); }
	public byte[] Bytes(int length, boolean reverse) { return Bytes(length, 0, reverse); }
	public byte[] Bytes(int length, int offset, boolean reverse) {
		byte[] result = new byte[length];
		for(int i = 0; i < length; i++) {
			int idx = reverse ? (length - 1 - i):i;
			result[idx] = bytes[index + offset + i];
		}
		return result;
	}
	
	public Memory WriteBytes(byte[] value) { return WriteBytes(value, value.length, 0, false); }
	public Memory WriteBytes(byte[] value, int length) { return WriteBytes(value, length, 0, false); }
	public Memory WriteBytes(byte[] value, int length, int offset) { return WriteBytes(value, length, offset, false); }
	public Memory WriteBytes(byte[] value, int length, boolean reverse) { return WriteBytes(value, length, 0, reverse); }
	public Memory WriteBytes(byte[] value, int length, int offset, boolean reverse) {
		for(int i = 0; i < length; i++) {
			int idx = reverse ? (length - 1 - i):i;
			bytes[index + offset + i] = value[idx];
		}
		return this;
	}
	
	public byte[] NextBytes(int length) { return NextBytes(length, false); }
	public byte[] NextBytes(int length, boolean reverse) {
		byte[] result = Bytes(length, 0, reverse);
		index += length;
		return result;
	}
	
	public Memory NextWriteBytes(byte[] value) { return NextWriteBytes(value, value.length, false); }
	public Memory NextWriteBytes(byte[] value, int length) { return NextWriteBytes(value, length, false); }
	public Memory NextWriteBytes(byte[] value, int length, boolean reverse) {
		WriteBytes(value, length, 0, reverse);
		index += length;
		return this;
	}
	
	// ======================= //
	
	public short[] Shorts(int length) { return Shorts(length, 0, defaultEndian, false); }
	public short[] Shorts(int length, int offset) { return Shorts(length, offset, defaultEndian, false); }
	public short[] Shorts(int length, boolean bigEndian) { return Shorts(length, 0, bigEndian, false); }
	public short[] Shorts(int length, int offset, boolean bigEndian) { return Shorts(length, offset, bigEndian, false); }
	public short[] Shorts(int length, int offset, boolean bigEndian, boolean reverse) {
		short[] result = new short[length];
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			result[idx] = Short(i * 2, bigEndian);
		}
		return result;
	}
	
	public Memory WriteShorts(short[] value) { return WriteShorts(value, value.length, 0, defaultEndian, false); }
	public Memory WriteShorts(short[] value, int length) { return WriteShorts(value, length, 0, defaultEndian, false); }
	public Memory WriteShorts(short[] value, int length, int offset) { return WriteShorts(value, length, offset, defaultEndian, false); }
	public Memory WriteShorts(short[] value, int length, boolean bigEndian) { return WriteShorts(value, length, 0, bigEndian, false); }
	public Memory WriteShorts(short[] value, int length, int offset, boolean bigEndian) { return WriteShorts(value, length, offset, bigEndian, false); }
	public Memory WriteShorts(short[] value, int length, int offset, boolean bigEndian, boolean reverse) {
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			WriteShort(value[idx], i * 2, bigEndian);
		}
		return this;
	}
	
	public short[] NextShorts(int length) { return NextShorts(length, defaultEndian, false); }
	public short[] NextShorts(int length, boolean bigEndian) { return NextShorts(length, bigEndian, false); }
	public short[] NextShorts(int length, boolean bigEndian, boolean reverse) {
		short[] result = Shorts(length, 0, reverse);
		index += length * 2;
		return result;
	}
	
	public Memory NextWriteShorts(short[] value) { return NextWriteShorts(value, value.length, defaultEndian, false); }
	public Memory NextWriteShorts(short[] value, int length) { return NextWriteShorts(value, length, defaultEndian, false); }
	public Memory NextWriteShorts(short[] value, int length, boolean bigEndian) { return NextWriteShorts(value, length, bigEndian, false); }
	public Memory NextWriteShorts(short[] value, int length, boolean bigEndian, boolean reverse) {
		WriteShorts(value, length, 0, bigEndian, reverse);
		index += length * 2;
		return this;
	}
	
	// ======================= //
	
	public int[] Ints(int length) { return Ints(length, 0, defaultEndian, false); }
	public int[] Ints(int length, int offset) { return Ints(length, offset, defaultEndian, false); }
	public int[] Ints(int length, boolean bigEndian) { return Ints(length, 0, bigEndian, false); }
	public int[] Ints(int length, int offset, boolean bigEndian) { return Ints(length, offset, bigEndian, false); }
	public int[] Ints(int length, int offset, boolean bigEndian, boolean reverse) {
		int[] result = new int[length];
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			result[idx] = Int(i * 4, bigEndian);
		}
		return result;
	}
	
	public Memory WriteInts(int[] value) { return WriteInts(value, value.length, 0, defaultEndian, false); }
	public Memory WriteInts(int[] value, int length) { return WriteInts(value, length, 0, defaultEndian, false); }
	public Memory WriteInts(int[] value, int length, int offset) { return WriteInts(value, length, offset, defaultEndian, false); }
	public Memory WriteInts(int[] value, int length, boolean bigEndian) { return WriteInts(value, length, 0, bigEndian, false); }
	public Memory WriteInts(int[] value, int length, int offset, boolean bigEndian) { return WriteInts(value, length, offset, bigEndian, false); }
	public Memory WriteInts(int[] value, int length, int offset, boolean bigEndian, boolean reverse) {
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			WriteInt(value[idx], i * 4, bigEndian);
		}
		return this;
	}
	
	public int[] NextInts(int length) { return NextInts(length, defaultEndian, false); }
	public int[] NextInts(int length, boolean bigEndian) { return NextInts(length, bigEndian, false); }
	public int[] NextInts(int length, boolean bigEndian, boolean reverse) {
		int[] result = Ints(length, 0, reverse);
		index += length * 4;
		return result;
	}
	
	public Memory NextWriteInts(int[] value) { return NextWriteInts(value, value.length, defaultEndian, false); }
	public Memory NextWriteInts(int[] value, int length) { return NextWriteInts(value, length, defaultEndian, false); }
	public Memory NextWriteInts(int[] value, int length, boolean bigEndian) { return NextWriteInts(value, length, bigEndian, false); }
	public Memory NextWriteInts(int[] value, int length, boolean bigEndian, boolean reverse) {
		WriteInts(value, length, 0, bigEndian, reverse);
		index += length * 4;
		return this;
	}
	
	// ======================= //
	
	public long[] Longs(int length) { return Longs(length, 0, defaultEndian, false); }
	public long[] Longs(int length, int offset) { return Longs(length, offset, defaultEndian, false); }
	public long[] Longs(int length, boolean bigEndian) { return Longs(length, 0, bigEndian, false); }
	public long[] Longs(int length, int offset, boolean bigEndian) { return Longs(length, offset, bigEndian, false); }
	public long[] Longs(int length, int offset, boolean bigEndian, boolean reverse) {
		long[] result = new long[length];
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			result[idx] = Long(i * 8, bigEndian);
		}
		return result;
	}
	
	public Memory WriteLongs(long[] value) { return WriteLongs(value, value.length, 0, defaultEndian, false); }
	public Memory WriteLongs(long[] value, int length) { return WriteLongs(value, length, 0, defaultEndian, false); }
	public Memory WriteLongs(long[] value, int length, int offset) { return WriteLongs(value, length, offset, defaultEndian, false); }
	public Memory WriteLongs(long[] value, int length, boolean bigEndian) { return WriteLongs(value, length, 0, bigEndian, false); }
	public Memory WriteLongs(long[] value, int length, int offset, boolean bigEndian) { return WriteLongs(value, length, offset, bigEndian, false); }
	public Memory WriteLongs(long[] value, int length, int offset, boolean bigEndian, boolean reverse) {
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			WriteLong(value[idx], i * 8, bigEndian);
		}
		return this;
	}
	
	public long[] NextLongs(int length) { return NextLongs(length, defaultEndian, false); }
	public long[] NextLongs(int length, boolean bigEndian) { return NextLongs(length, bigEndian, false); }
	public long[] NextLongs(int length, boolean bigEndian, boolean reverse) {
		long[] result = Longs(length, 0, reverse);
		index += length * 8;
		return result;
	}
	
	public Memory NextWriteLongs(long[] value) { return NextWriteLongs(value, value.length, defaultEndian, false); }
	public Memory NextWriteLongs(long[] value, int length) { return NextWriteLongs(value, length, defaultEndian, false); }
	public Memory NextWriteLongs(long[] value, int length, boolean bigEndian) { return NextWriteLongs(value, length, bigEndian, false); }
	public Memory NextWriteLongs(long[] value, int length, boolean bigEndian, boolean reverse) {
		WriteLongs(value, length, 0, bigEndian, reverse);
		index += length * 8;
		return this;
	}
	
	// ======================= //
	
	public float[] Floats(int length) { return Floats(length, 0, defaultEndian, false); }
	public float[] Floats(int length, int offset) { return Floats(length, offset, defaultEndian, false); }
	public float[] Floats(int length, boolean bigEndian) { return Floats(length, 0, bigEndian, false); }
	public float[] Floats(int length, int offset, boolean bigEndian) { return Floats(length, offset, bigEndian, false); }
	public float[] Floats(int length, int offset, boolean bigEndian, boolean reverse) {
		float[] result = new float[length];
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			result[idx] = Float(i * 4, bigEndian);
		}
		return result;
	}
	
	public Memory WriteFloats(float[] value) { return WriteFloats(value, value.length, 0, defaultEndian, false); }
	public Memory WriteFloats(float[] value, int length) { return WriteFloats(value, length, 0, defaultEndian, false); }
	public Memory WriteFloats(float[] value, int length, int offset) { return WriteFloats(value, length, offset, defaultEndian, false); }
	public Memory WriteFloats(float[] value, int length, boolean bigEndian) { return WriteFloats(value, length, 0, bigEndian, false); }
	public Memory WriteFloats(float[] value, int length, int offset, boolean bigEndian) { return WriteFloats(value, length, offset, bigEndian, false); }
	public Memory WriteFloats(float[] value, int length, int offset, boolean bigEndian, boolean reverse) {
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			WriteFloat(value[idx], i * 4, bigEndian);
		}
		return this;
	}
	
	public float[] NextFloats(int length) { return NextFloats(length, defaultEndian, false); }
	public float[] NextFloats(int length, boolean bigEndian) { return NextFloats(length, bigEndian, false); }
	public float[] NextFloats(int length, boolean bigEndian, boolean reverse) {
		float[] result = Floats(length, 0, reverse);
		index += length * 4;
		return result;
	}
	
	public Memory NextWriteFloats(float[] value) { return NextWriteFloats(value, value.length, defaultEndian, false); }
	public Memory NextWriteFloats(float[] value, int length) { return NextWriteFloats(value, length, defaultEndian, false); }
	public Memory NextWriteFloats(float[] value, int length, boolean bigEndian) { return NextWriteFloats(value, length, bigEndian, false); }
	public Memory NextWriteFloats(float[] value, int length, boolean bigEndian, boolean reverse) {
		WriteFloats(value, length, 0, bigEndian, reverse);
		index += length * 4;
		return this;
	}
	
	// ======================= //
	
	public double[] Doubles(int length) { return Doubles(length, 0, defaultEndian, false); }
	public double[] Doubles(int length, int offset) { return Doubles(length, offset, defaultEndian, false); }
	public double[] Doubles(int length, boolean bigEndian) { return Doubles(length, 0, bigEndian, false); }
	public double[] Doubles(int length, int offset, boolean bigEndian) { return Doubles(length, offset, bigEndian, false); }
	public double[] Doubles(int length, int offset, boolean bigEndian, boolean reverse) {
		double[] result = new double[length];
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			result[idx] = Double(i * 8, bigEndian);
		}
		return result;
	}
	
	public Memory WriteDoubles(double[] value) { return WriteDoubles(value, value.length, 0, defaultEndian, false); }
	public Memory WriteDoubles(double[] value, int length) { return WriteDoubles(value, length, 0, defaultEndian, false); }
	public Memory WriteDoubles(double[] value, int length, int offset) { return WriteDoubles(value, length, offset, defaultEndian, false); }
	public Memory WriteDoubles(double[] value, int length, boolean bigEndian) { return WriteDoubles(value, length, 0, bigEndian, false); }
	public Memory WriteDoubles(double[] value, int length, int offset, boolean bigEndian) { return WriteDoubles(value, length, offset, bigEndian, false); }
	public Memory WriteDoubles(double[] value, int length, int offset, boolean bigEndian, boolean reverse) {
		for(int i = 0; i < length; i++) {
			int idx = reverse ? i:(length - 1 - i);
			WriteDouble(value[idx], i * 8, bigEndian);
		}
		return this;
	}
	
	public double[] NextDoubles(int length) { return NextDoubles(length, defaultEndian, false); }
	public double[] NextDoubles(int length, boolean bigEndian) { return NextDoubles(length, bigEndian, false); }
	public double[] NextDoubles(int length, boolean bigEndian, boolean reverse) {
		double[] result = Doubles(length, 0, reverse);
		index += length * 8;
		return result;
	}
	
	public Memory NextWriteDoubles(double[] value) { return NextWriteDoubles(value, value.length, defaultEndian, false); }
	public Memory NextWriteDoubles(double[] value, int length) { return NextWriteDoubles(value, length, defaultEndian, false); }
	public Memory NextWriteDoubles(double[] value, int length, boolean bigEndian) { return NextWriteDoubles(value, length, bigEndian, false); }
	public Memory NextWriteDoubles(double[] value, int length, boolean bigEndian, boolean reverse) {
		WriteDoubles(value, length, 0, bigEndian, reverse);
		index += length * 8;
		return this;
	}
	
	// ======================= //
	
	public static final void main(String[] args) {
		byte[] data = { 0x12, 0x43, 0x56, (byte)0x87, (byte)0x9a, (byte)0xcb, (byte)0xde, (byte)0xf0 };
		
		Memory m = new Memory(data);
		long v = m.readValue(0, 8, true);
		
		System.out.printf("%04x\n", v);
		
		//p.writeValue(1.0D, 0, 2, false);
		m.WriteLong(0xff00ff00L, false);
		m.WriteFloat(0.434343434f);
		m.WriteBytes("Hello".getBytes());
		
		System.out.printf("%016x\n", m.Long());
		
		int[] i = m.Ints(2);
		System.out.printf("%08x %08x\n", i[0], i[1]);
		
		short[] s = m.Shorts(4);
		System.out.printf("%04x %04x %04x %04x\n", s[0], s[1], s[2], s[3]);
		
		System.out.println(m.String(5));
		
	}
}
