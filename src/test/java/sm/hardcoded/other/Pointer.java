package sm.hardcoded.other;

import java.util.LinkedList;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

// TODO: Create a private long ReadValue(int offset, int size, boolean bigEndian)
//       that reads 'size' bytes and creates a long.
public class Pointer {
	private LinkedList<Integer> pos = new LinkedList<>();
	private byte[] bytes;
	private int index;
	private int lastSize;
	
	public Pointer(int capacity) {
		this(capacity, null);
	}
	
	public Pointer(byte[] bytes) {
		this(bytes.length, bytes);
	}
	
	public Pointer(int capacity, byte[] source) {
		bytes = new byte[capacity];
		if(source != null) {
			System.arraycopy(source, 0, bytes, 0, source.length);
		}
	}
	
	public Pointer(Pointer pointer) {
		this(pointer.bytes.length, pointer.bytes);
		
		lastSize = pointer.lastSize;
		index = pointer.index;
		pos.addAll(pointer.pos);
	}
	
	public Pointer move(int index) {
		this.index += index;
		return this;
	}
	
	public Pointer set(int index) {
		this.index = index;
		return this;
	}
	
	public int index() {
		return index;
	}
	
	public Pointer next() {
		index += lastSize;
		return this;
	}
	
	public void push() {
		pos.push(index);
	}
	
	public void pop() {
		index = pos.poll();
	}
	
	public byte[] data() {
		return bytes;
	}
	
	// ========== TEST
	
	private <R> R NextValue(Function<Integer, R> a, int offset) {
		R result = a.apply(offset);
		next();
		return result;
	}
	
	private <R> R NextValue(BiFunction<Integer, Boolean, R> a, int offset, boolean bigEndian) {
		R result = a.apply(offset, bigEndian);
		next();
		return result;
	}
	
	// ========== ????
	
	public String String(int length, boolean bigEndian) { return String(length, 0, bigEndian); }
	public byte[] Bytes(int length) { return Bytes(length, 0, true); }
	public byte[] Bytes(int length, boolean bigEndian) { return Bytes(length, 0, bigEndian); }
	public byte[] Bytes(byte[] dst, int dstPos, int length, boolean bigEndian) { return Bytes(dst, dstPos, length, 0, bigEndian); }
	
	// =========== BYTE
	
	public int UnsignedByte() { return Byte.toUnsignedInt(Byte(0)); }
	public int UnsignedByte(int offset) { return Byte.toUnsignedInt(Byte(offset)); }
	
	public byte Byte() { return Byte(0); }
	public byte Byte(int offset) {
		lastSize = 1;
		return bytes[index + offset];
	}

	public byte NextByte() { return NextByte(0); }
	public byte NextByte(int offset) { return NextValue(this::Byte, offset); }
	public int NextUnsignedByte() { return Byte.toUnsignedInt(NextByte(0)); }
	public int NextUnsignedByte(int offset) { return Byte.toUnsignedInt(NextByte(offset)); }
	
	
	public Pointer WriteByte(int value) { return WriteByte(value, 0); }
	public Pointer WriteByte(int value, int offset) {
		bytes[index + offset] = (byte)(value & 0xff);
		lastSize = 1;
		return this;
	}
	
	// =========== SHORT
	
	public int UnsignedShort() { return Short.toUnsignedInt(Short(0, false)); }
	public int UnsignedShort(int offset) { return Short.toUnsignedInt(Short(offset, false)); }
	public int UnsignedShort(boolean bigEndian) { return Short.toUnsignedInt(Short(0, bigEndian)); }
	public int UnsignedShort(int offset, boolean bigEndian) { return Short.toUnsignedInt(Short(offset, bigEndian)); }
	
	public short Short() { return Short(0, false); }
	public short Short(int offset) { return Short(offset, false); }
	public short Short(boolean bigEndian) { return Short(0, bigEndian); }
	public short Short(int offset, boolean bigEndian) {
		int a = UnsignedByte(offset);
		int b = UnsignedByte(offset + 1);
		
		lastSize = 2;
		if(bigEndian) {
			return (short)((a << 8) | b);
		} else {
			return (short)((b << 8) | a);
		}
	}
	
	public short NextShort() { return NextShort(0, false); }
	public short NextShort(int offset) { return NextShort(offset, false); }
	public short NextShort(boolean bigEndian) { return NextShort(0, bigEndian); }
	public short NextShort(int offset, boolean bigEndian) { return NextValue(this::Short, offset, bigEndian); }
	
	public Pointer WriteShort(int value) { return WriteShort(value, 0, false); }
	public Pointer WriteShort(int value, int offset) { return WriteShort(value, offset, false); }
	public Pointer WriteShort(int value, boolean bigEndian) { return WriteShort(value, 0, bigEndian); }
	public Pointer WriteShort(int value, int offset, boolean bigEndian) {
		if(bigEndian) {
			WriteByte((value >>> 8) & 0xff, offset);
			WriteByte(value & 0xff, offset + 1);
		} else {
			WriteByte(value & 0xff, offset);
			WriteByte((value >>> 8) & 0xff, offset + 1);
		}
		
		lastSize = 2;
		return this;
	}
	
	// =========== INT

	public long UnsignedInt() { return Integer.toUnsignedLong(Int(0, false)); }
	public long UnsignedInt(int offset) { return Integer.toUnsignedLong(Int(offset, false)); }
	public long UnsignedInt(boolean bigEndian) { return Integer.toUnsignedLong(Int(0, bigEndian)); }
	public long UnsignedInt(int offset, boolean bigEndian) { return Integer.toUnsignedLong(Int(offset, bigEndian)); }
	
	public int Int() { return Int(0, false); }
	public int Int(int offset) { return Int(offset, false); }
	public int Int(boolean bigEndian) { return Int(0, bigEndian); }
	public int Int(int offset, boolean bigEndian) {
		int a = UnsignedShort(offset, bigEndian);
		int b = UnsignedShort(offset + 2, bigEndian);
		
		lastSize = 4;
		if(bigEndian) {
			return (a << 16L) | b;
		} else {
			return (b << 16L) | a;
		}
	}
	
	public int NextInt() { return NextInt(0, false); }
	public int NextInt(int offset) { return NextInt(offset, false); }
	public int NextInt(boolean bigEndian) { return NextInt(0, bigEndian); }
	public int NextInt(int offset, boolean bigEndian) { return NextValue(this::Int, offset, bigEndian); }
	
	public Pointer WriteInt(int value) { return WriteInt(value, 0, false); }
	public Pointer WriteInt(int value, int offset) { return WriteInt(value, offset, false); }
	public Pointer WriteInt(int value, boolean bigEndian) { return WriteInt(value, 0, bigEndian); }
	public Pointer WriteInt(int value, int offset, boolean bigEndian) {
		if(bigEndian) {
			WriteShort((value >>> 16) & 0xffff, offset, bigEndian);
			WriteShort(value & 0xffff, offset + 2, bigEndian);
		} else {
			WriteShort(value & 0xffff, offset, bigEndian);
			WriteShort((value >>> 16) & 0xffff, offset + 2, bigEndian);
		}
		
		lastSize = 4;
		return this;
	}
	
	// =========== LONG
	
	public long Long() { return Long(0, false); }
	public long Long(int offset) { return Long(offset, false); }
	public long Long(boolean bigEndian) { return Long(0, bigEndian); }
	public long Long(int offset, boolean bigEndian) {
		long a = UnsignedInt(offset    , bigEndian);
		long b = UnsignedInt(offset + 4, bigEndian);
		
		lastSize = 8;
		if(bigEndian) {
			return (a << 32L) | b;
		} else {
			return (b << 32L) | a;
		}
	}

	public long NextLong() { return NextLong(0, false); }
	public long NextLong(int offset) { return NextLong(offset, false); }
	public long NextLong(boolean bigEndian) { return NextLong(0, bigEndian); }
	public long NextLong(int offset, boolean bigEndian) { return NextValue(this::Long, offset, bigEndian); }
	
	public Pointer WriteLong(long value) { return WriteLong(value, 0, false); }
	public Pointer WriteLong(long value, int offset) { return WriteLong(value, offset, false); }
	public Pointer WriteLong(long value, boolean bigEndian) { return WriteLong(value, 0, bigEndian); }
	public Pointer WriteLong(long value, int offset, boolean bigEndian) {
		if(bigEndian) {
			WriteInt((int)((value >>> 32L) & 0xffffffff), offset, bigEndian);
			WriteInt((int)(value & 0xffffffff), offset + 4, bigEndian);
		} else {
			WriteInt((int)(value & 0xffffffff), offset, bigEndian);
			WriteInt((int)((value >>> 32L) & 0xffffffff), offset + 4, bigEndian);
		}
		
		lastSize = 8;
		return this;
	}
	
	public Pointer NextWriteLong(long value) { return NextWriteLong(value, 0, false); }
	public Pointer NextWriteLong(long value, int offset) { return NextWriteLong(value, offset, false); }
	public Pointer NextWriteLong(long value, boolean bigEndian) { return NextWriteLong(value, 0, bigEndian); }
	public Pointer NextWriteLong(long value, int offset, boolean bigEndian) {
		WriteLong(value, offset, bigEndian);
		next();
		return this;
	}
	
	// =========== FLOAT
	
	public float Float() { return Float(0, false); }
	public float Float(int offset) { return Float(offset, false); }
	public float Float(boolean bigEndian) { return Float(0, bigEndian); }
	public float Float(int offset, boolean bigEndian) {
		return Float.intBitsToFloat(Int(offset, bigEndian));
	}
	
	public float NextFloat() { return NextFloat(0, false); }
	public float NextFloat(int offset) { return NextFloat(offset, false); }
	public float NextFloat(boolean bigEndian) { return NextFloat(0, bigEndian); }
	public float NextFloat(int offset, boolean bigEndian) { return NextValue(this::Float, offset, bigEndian); }
	
	// =========== DOUBLE
	
	public double Double() { return Double(0, false); }
	public double Double(int offset) { return Double(offset, false); }
	public double Double(boolean bigEndian) { return Double(0, bigEndian); }
	public double Double(int offset, boolean bigEndian) {
		return Double.longBitsToDouble(Long(offset, bigEndian));
	}
	
	public double NextDouble() { return NextDouble(0, false); }
	public double NextDouble(int offset) { return NextDouble(offset, false); }
	public double NextDouble(boolean bigEndian) { return NextDouble(0, bigEndian); }
	public double NextDouble(int offset, boolean bigEndian) { return NextValue(this::Double, offset, bigEndian); }
	
	// =========== OTHER

	public UUID Uuid(boolean bigEndian) { return Uuid(0, bigEndian); }
	public UUID Uuid(int offset, boolean bigEndian) {
		long a = Long(offset, bigEndian);
		long b = Long(offset + 8, bigEndian);
		
		lastSize = 16;
		if(bigEndian) {
			return new UUID(a, b);
		} else {
			return new UUID(b, a);
		}
	}
	
	
	public String String(int length, int offset, boolean bigEndian) {
		StringBuilder sb = new StringBuilder().append(new String(bytes, index, length));
		
		lastSize = length;
		if(bigEndian) {
			return sb.toString();
		} else {
			return sb.reverse().toString();
		}
	}
	
	public byte[] Bytes(int length, int offset) {
		return Bytes(new byte[length], 0, length, offset, true);
	}
	public byte[] Bytes(int length, int offset, boolean bigEndian) {
		return Bytes(new byte[length], 0, length, offset, bigEndian);
	}
	
	public byte[] Bytes(byte[] dst, int dstPos, int length, int offset, boolean bigEndian) {
		System.arraycopy(bytes, index + offset, dst, dstPos, length);
		lastSize = length;
		
		if(bigEndian) {
			return dst;
		} else {
			for(int i = dstPos; i < length / 2; i++) {
				byte tmp = dst[i];
				dst[i] = dst[dstPos + length - i - 1];
				dst[dstPos + length - i - 1] = tmp;
			}
		}
		
		return dst;
	}
	
	public UUID NextUuid(boolean bigEndian) { return NextUuid(0, bigEndian); }
	public UUID NextUuid(int offset, boolean bigEndian) {
		UUID a = Uuid(offset, bigEndian); next(); return a;
	}
	
	public String NextString(int length, boolean bigEndian) { return NextString(length, 0, bigEndian); }
	public String NextString(int length, int offset, boolean bigEndian) {
		String a = String(length, offset, bigEndian); next(); return a;
	}
	
	public byte[] NextBytes(byte[] dst, int dstPos, int length) { return NextBytes(dst, dstPos, length, 0, true); }
	public byte[] NextBytes(byte[] dst, int dstPos, int length, boolean bigEndian) { return NextBytes(dst, dstPos, length, 0, bigEndian); }
	public byte[] NextBytes(byte[] dst, int dstPos, int length, int offset, boolean bigEndian) {
		byte[] a = Bytes(dst, dstPos, length, offset, bigEndian); next(); return a;
	}
	public byte[] NextBytes(int length, boolean bigEndian) { return NextBytes(length, 0, bigEndian); }
	public byte[] NextBytes(int length, int offset, boolean bigEndian) {
		byte[] a = Bytes(length, offset, bigEndian); next(); return a;
	}
	
	// ======================= WRITE =======================
	
	public Pointer WriteBytes(byte[] src) { return WriteBytes(src, 0, src.length, 0); }
	public Pointer WriteBytes(byte[] src, int length) { return WriteBytes(src, 0, length, 0); }
	public Pointer WriteBytes(byte[] src, int length, int offset) { return WriteBytes(src, 0, length, offset); }
	public Pointer WriteBytes(byte[] src, int srcPos, int length, int offset) {
		System.arraycopy(src, srcPos, bytes, index + offset, length);
		lastSize = length;
		return this;
	}
	
	public Pointer NextWriteBytes(byte[] src) { return NextWriteBytes(src, 0, src.length, 0); }
	public Pointer NextWriteBytes(byte[] src, int length) { return NextWriteBytes(src, 0, length, 0); }
	public Pointer NextWriteBytes(byte[] src, int length, int offset) { return NextWriteBytes(src, 0, length, offset); }
	public Pointer NextWriteBytes(byte[] src, int srcPos, int length, int offset) {
		System.arraycopy(src, srcPos, bytes, index + offset, length);
		lastSize = length;
		next();
		return this;
	}
}
