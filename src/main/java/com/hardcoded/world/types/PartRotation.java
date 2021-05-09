package com.hardcoded.world.types;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.hardcoded.sm.objects.BodyList.ChildShape;

/**
 * This was an attempt to understand how rotations work and to fix them in code.
 * 
 * <p>This code is a botched attempt to solve a problem that probably has a better
 * solution than this.
 * 
 * @author HardCoded
 * @since v0.1
 * @deprecated Find the actual method for doing this
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
	
	public static final int[] PartRotationDataValue = {
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
	
	public static final Matrix4f[] PartRotationMultiplier;
	
	// TODO: Cache these for each part!
	public static Vector3f getPartOffset(SMPart part, ChildShape shape) {
		Matrix4f mul = getRotationMultiplier(shape.partRotation);
		PartBounds bounds = part.getBounds();
		if(bounds != null) {//instanceof BoxBounds) {
			Matrix4f mat = mul.translate(
				(bounds.getWidth() - 1) / 2.0f,
				(bounds.getHeight() - 1) / 2.0f,
				(bounds.getDepth() - 1) / 2.0f
			, new Matrix4f());
			
			return mat.getColumn(3, new Vector3f());
		}

		return mul.getColumn(3, new Vector3f());
	}
	
	@Deprecated
	public static Matrix4f getRotationMultiplier(int rotation) {
		for(int i = 0; i < 24; i++) {
			if(PartRotationDataValue[i] == rotation) {
				return PartRotationMultiplier[i];
			}
		}
		
		return null;
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
			}
		}
	}
}
