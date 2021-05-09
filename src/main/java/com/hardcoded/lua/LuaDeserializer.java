package com.hardcoded.lua;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hardcoded.memory.Memory;

public class LuaDeserializer {
	public static final int VERSION = 1;
	
	public static void Deserialize(Memory data) {
		int data_length = data.data().length - data.index();
		System.out.println();
		for(int i = 0; i < data_length; i++) System.out.printf("%02x", data.Byte(i));
		System.out.println();
		for(int i = 0; i < data_length; i++) {
			char c = (char)data.Byte(i);
			System.out.printf("%2s", (Character.isWhitespace(c) ? "":c) + " ");
		}
		System.out.println();
		System.out.println();
		
		data.setDefaultBigEndian(true);
		
		String str = data.NextString(3);
		if(!str.equals("LUA")) {
			throw new AssertionError("Serialized data is invalid");
		}
		System.out.printf("Val: '%s'\n", str);
		
		int version = data.NextInt();
		
		if(version != VERSION) {
			throw new AssertionError("The version of the serialized data is not '" + VERSION + "'");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		
		Deserialize(data, mapper, rootNode);
		
		String jsonString = "Failed";
		try {
			jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
		} catch(JsonProcessingException e) {
			e.printStackTrace();
		}
		System.out.println(jsonString);
		System.out.println();
	}
	
	private static void Deserialize(Memory data, ObjectMapper mapper, ObjectNode node) {
		int read = data.NextUnsignedByte();
		LuaSaveDataType type = LuaSaveDataType.valueOf(read);
		
		String nextName = "";
		
		switch(type) {
			case Table: {
				int length = data.NextInt();
				System.out.println("Length: " + length);
				
				ObjectNode table = mapper.createObjectNode();
				Deserialize(data, mapper, table);
				node.set(nextName, table);
				break;
			}
			case Boolean: {
				node.put(nextName, data.NextInt() != 0);
				break;
			}
			case String: {
				int stringLength = data.NextInt();
				String string = data.NextString(stringLength);
				node.put(nextName, string);
				break;
			}
			case Int32: node.put(nextName, data.NextInt()); break;
			case Int16: node.put(nextName, data.NextUnsignedShort()); break;
			case Int8: node.put(nextName, data.NextUnsignedByte()); break;
			
			default: {
				System.err.println("Unknown type id " + read + " / " + type);
			}
		}
	}
	
	private static int Something(Memory data, int size, int param_3) {
		if(size == 0) return 0;
		
		int uVar2 = 0;
		int uVar4 = data.Int(8);
		int in_EAX = uVar4 + size;
		int type = 1;
		
		System.out.println("uVar2: " + uVar2);
		System.out.println("uVar4: " + uVar4);
		System.out.println("in_EAX: " + in_EAX);
		System.out.println("type: " + type);
		
		if(in_EAX <= data.Int()) {
			uVar2 = uVar4 & 7;
			if((uVar2 == 0) && ((size & 7) == 0)) {
				// memcpy something
				// set something...
				// return something
				return 0;
			}
			// set return type to (size + 7) / 8;
			
			do {
				
				uVar4 = uVar2;
				if((uVar2 != 0) && (8 - uVar2 < size)) {
					// ????
					// ????
				}
				
				if(size < 8) {
					if(size - 8 < 0) {
						
					} else {
						
					}
					size = 0;
				} else {
					size -= 8;
				}
				
				type = type + 1;
			} while(size != 0);
			
			// return CONCAT31((int3)(uVar4 >> 8),1);
			return 12;
		}
		
		return in_EAX & 0xffffff00;
	}
}
