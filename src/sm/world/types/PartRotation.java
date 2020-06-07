package sm.world.types;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import sm.objects.BodyList.ChildShape;

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
		0b100101,
		0b110010,
		0b1100011,
		0b1010110,
		0b1100101,
		0b1010010,
		0b100011,
		0b110110,
		0b1110101,
		0b1110010,
		0b1110011,
		0b1110110,
		0b10101,
		0b10010,
		0b10011,
		0b10110,
		0b100111,
		0b110111,
		0b1100111,
		0b1010111,
		0b100001,
		0b110001,
		0b1100001,
		0b1010001,
	};
	
	private static final float[][] PartRotationArray = {
		Y_rot, NegY_rot,
		Z_rot, NegZ_rot,
		X_rot, NegX_rot
	};
	
	public static final Matrix4f[] PartRotationMultiplier;
	
	public static Vector3f getPartOffset(Part part, ChildShape shape) {
		Matrix4f mul = getRotationMultiplier(shape.rotation_41_1);
		return null;
	}
	
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
