package com.hardcoded.sm.objects;

/**
 * @author HardCoded
 * @since v0.1
 */
class GenericData {
	/**
	 * ResultSet set = sqlite.execute("SELECT * FROM GenericData WHERE channel = 4");
		
		byte[] data = set.getBytes("data");
		
		int a = 1;
		StringBuilder sb = new StringBuilder();
		for(byte b : data) {
			sb.append(String.format("%02x%s", b, ((a++ % 16) == 0) ? "":""));
		}
		String nows = sb.toString();
		
		int unk_0_4 = Util.getInt(data, 0, true);
		int worldId_4_2 = Short.toUnsignedInt(Util.getShort(data, 4, true));
		int channel_6_1 = Byte.toUnsignedInt(data[6]);
		int flags_7_1 = Byte.toUnsignedInt(data[7]);
		int unk_8_4 = Util.getInt(data, 8, true);
		
		nows = nows.substring(8 * 2);
		
		if(!last.equals(nows)) {
			System.out.println(nows);
			System.out.println("unk_0_4: " + unk_0_4);
			System.out.println("worldId_4_2: " + worldId_4_2);
			System.out.println("channel_6_1: " + channel_6_1);
			System.out.println("flags_7_1: " + flags_7_1);
			System.out.println("unk_8_4: " + unk_8_4);
			System.out.println();
			
			last = nows;
		}
	 */
}
