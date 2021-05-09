package com.hardcoded.lwjgl.util;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MathUtils {
	public static final float deg2Rad = (float)Math.PI / 180.0f;
	public static final float rad2Deg = (float)(180.0f / Math.PI);
	
	public static float cosDeg(double a) {
		return (float)Math.cos(a * deg2Rad);
	}
	
	public static float sinDeg(double a) {
		return (float)Math.sin(a * deg2Rad);
	}
	
	public static float toRadians(double deg) {
		return (float)(deg * deg2Rad);
	}
	
	public static float[] toFloatArray(Vector4f[][] src, int depth) {
		return toFloatArray(src, depth, new float[src.length * 4 * depth]);
	}
	
	public static float[] toFloatArray(Vector3f[][] src, int depth) {
		return toFloatArray(src, depth, new float[src.length * 3 * depth]);
	}
	
	public static float[] toFloatArray(Vector2f[][] src, int depth) {
		return toFloatArray(src, depth, new float[src.length * 2 * depth]);
	}
	
	public static float[] toFloatArray(Vector4f[] src) {
		return toFloatArray(src, new float[src.length * 4]);
	}
	
	public static float[] toFloatArray(Vector3f[] src) {
		return toFloatArray(src, new float[src.length * 3]);
	}
	
	public static float[] toFloatArray(Vector4f[] src, float[] dest) {
		for(int i = 0; i < src.length; i++) {
			Vector4f v = src[i];
			dest[i * 4    ] = v.x;
			dest[i * 4 + 1] = v.y;
			dest[i * 4 + 2] = v.z;
			dest[i * 4 + 3] = v.w;	
		}
		return dest;
	}
	
	public static float[] toFloatArray(Vector3f[] src, float[] dest) {
		for(int i = 0; i < src.length; i++) {
			Vector3f v = src[i];
			dest[i * 3    ] = v.x;
			dest[i * 3 + 1] = v.y;
			dest[i * 3 + 2] = v.z;
		}
		return dest;
	}
	
	public static float[] toFloatArray(Vector4f[][] src, int depth, float[] dest) {
		for(int i = 0; i < src.length; i++) {
			int ix = i * 4 * depth;
			
			Vector4f[] v = src[i];
			if(v == null) continue;
			
			for(int j = 0; j < depth; j++) {
				if(v[j] == null) continue;
				dest[ix + j * 4    ] = v[j].x;
				dest[ix + j * 4 + 1] = v[j].y;
				dest[ix + j * 4 + 2] = v[j].z;
				dest[ix + j * 4 + 3] = v[j].w;	
			}
		}
		return dest;
	}
	
	public static float[] toFloatArray(Vector3f[][] src, int depth, float[] dest) {
		for(int i = 0; i < src.length; i++) {
			int ix = i * 3 * depth;
			
			Vector3f[] v = src[i];
			for(int j = 0; j < depth; j++) {
				dest[ix + j * 3    ] = v[j].x;
				dest[ix + j * 3 + 1] = v[j].y;
				dest[ix + j * 3 + 2] = v[j].z;
			}
		}
		return dest;
	}
	
	public static float[] toFloatArray(Vector2f[][] src, int depth, float[] dest) {
		for(int i = 0; i < src.length; i++) {
			int ix = i * 2 * depth;
			
			Vector2f[] v = src[i];
			for(int j = 0; j < depth; j++) {
				dest[ix + j * 2    ] = v[j].x;
				dest[ix + j * 2 + 1] = v[j].y;
			}
		}
		return dest;
	}
}