package com.hardcoded.lua;

public enum LuaSaveDataType {
	Nil(1),
	Boolean(2),
	Number(3),
	String(4),
	Table(5),
	Int32(6),
	Int16(7),
	Int8(8),
	Json(9),
	Userdata(100),
	Unknown(0);
	
	public final int id;
	private LuaSaveDataType(int id) {
		this.id = id;
	}
	
	public static LuaSaveDataType valueOf(int i) {
		switch(i) {
			case 1: return Nil;
			case 2: return Boolean;
			case 3: return Number;
			case 4: return String;
			case 5: return Table;
			case 6: return Int32;
			case 7: return Int16;
			case 8: return Int8;
			case 9: return Json;
			case 100: return Userdata;
		}
		
		return Unknown;
	}
}
