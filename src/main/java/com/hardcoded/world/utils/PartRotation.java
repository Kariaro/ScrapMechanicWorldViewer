package com.hardcoded.world.utils;

import org.joml.Matrix4f;

/**
 * This was an attempt to understand how rotations work and to fix them in code.
 * 
 * <p>This code is a botched attempt to solve a problem that probably has a better
 * solution than this.
 * 
 * @author HardCoded
 * @since v0.1
 */
public final class PartRotation {
	private static final float pi = (float)Math.PI;
	private static final float hp = pi / 2.0f;
	
	private static final float[] Y_rot = new float[] {
		 hp, 1, 0, 0, 0, 1, 0,
		  0, 1, 1, 0,
		-hp, 0, 1, 0,
		-pi, 0, 1, 1,
		 hp, 1, 1, 1
	};
	
	private static final float[] NegY_rot = new float[] {
		-hp, 1, 0, 0, 0, 1, 0,
		  0, 1, 0, 1,
		 hp, 0, 0, 1,
		 pi, 0, 0, 0,
		-hp, 1, 0, 0
	};

	private static final float[] Z_rot = new float[] {
		  0, 0, 0, 1, 0, 0, 1,
		  0, 1, 1, 1,
		-hp, 0, 1, 1,
		-pi, 0, 0, 1,
		 hp, 1, 0, 1
	};

	private static final float[] NegZ_rot = new float[] {
		 pi, 1, 0, 0, 0, 0, 1,
		  0, 1, 0, 0,
		 hp, 0, 0, 0,
		 pi, 0, 1, 0,
		-hp, 1, 1, 0
	};
	
	private static final float[] X_rot = new float[] {
		-hp, 0, 1, 0, 1, 0, 0,
		 hp, 1, 0, 0,
		  0, 1, 1, 0,
		-hp, 1, 1, 1,
		 pi, 1, 0, 1
	};

	private static final float[] NegX_rot = new float[] {
		 hp, 0, 1, 0, 1, 0, 0,
		 hp, 0, 1, 0,
		 pi, 0, 0, 0,
		-hp, 0, 0, 1,
		  0, 0, 1, 1
	};
	
	private static final Matrix4f[] PartRotationIndex = new Matrix4f[256];
	
	// xaxis, zaxis
	private static final int[] PartRotationDataValue = {
		0b010_0101,
		0b011_0010,
		0b110_0011,
		0b101_0110,
		0b110_0101,
		0b101_0010,
		0b010_0011,
		0b011_0110,
		0b111_0101,
		0b111_0010,
		0b111_0011,
		0b111_0110,
		0b001_0101,
		0b001_0010,
		0b001_0011,
		0b001_0110,
		0b010_0111,
		0b011_0111,
		0b110_0111,
		0b101_0111,
		0b010_0001,
		0b011_0001,
		0b110_0001,
		0b101_0001,
	};
	
	private static final float[][] PartRotationArray = {
		Y_rot, NegY_rot,
		Z_rot, NegZ_rot,
		X_rot, NegX_rot
	};
	
	private static final Matrix4f[] PartRotationMultiplier;
	
	public static Matrix4f getRotationMultiplier(int rotation) {
		Matrix4f mat = PartRotationIndex[rotation];
		if(mat == null) return new Matrix4f();
		return mat.get(new Matrix4f());
	}
	
	public static Matrix4f getAxisRotation(int xaxis, int zaxis) {
		return getRotationMultiplier((((zaxis > 0 ? 4:0) | (zaxis & 3)) << 4) | ((xaxis > 0 ? 4:0) | (xaxis & 3)));
	}
	
	static {
		PartRotationMultiplier = new Matrix4f[24];
		
		for(int i = 0; i < 6; i++) {
			float[] arr = PartRotationArray[i];
			
			for(int j = 0; j < 4; j++) {
				Matrix4f matrix = new Matrix4f();
				matrix.rotate(arr[0], arr[1], arr[2], arr[3]);
				matrix.rotate(arr[7 + j * 4], arr[4], arr[5], arr[6]);
				matrix.translate(
					arr[8 + j * 4],
					arr[9 + j * 4],
					arr[10 + j * 4]
				);
				PartRotationMultiplier[i * 4 + j] = matrix;
				PartRotationIndex[PartRotationDataValue[i * 4 + j]] = matrix;
			}
		}
	}
}
