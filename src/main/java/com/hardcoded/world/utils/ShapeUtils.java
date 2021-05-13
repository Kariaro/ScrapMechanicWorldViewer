package com.hardcoded.world.utils;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.hardcoded.asset.ScrapMechanicAssetHandler;
import com.hardcoded.db.types.SMPart;
import com.hardcoded.sm.objects.BodyList.ChildShape;
import com.hardcoded.sm.objects.BodyList.RigidBody;

public class ShapeUtils {
	public static Bounds3D getBoundingBox(RigidBody body) {
		Vector3f posi = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Vector3f size = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		
		for(ChildShape shape : body.shapes) {
			if(shape.shapeType == 0x1f) {
				float x = shape.xPos;
				float y = shape.yPos;
				float z = shape.zPos;
				float xs = x + shape.xSize;
				float ys = y + shape.ySize;
				float zs = z + shape.zSize;
				
				if(x < posi.x) posi.x = x;
				if(y < posi.y) posi.y = y;
				if(z < posi.z) posi.z = z;
				if(xs > size.x) size.x = xs;
				if(ys > size.y) size.y = ys;
				if(zs > size.z) size.z = zs;
				continue;
			}
			
			// TODO: Get bounding box of all parts. This should be cached
			SMPart part = ScrapMechanicAssetHandler.getPart(shape.uuid);
			if(part != null) {
				float x = shape.xPos;
				float y = shape.yPos;
				float z = shape.zPos;
				Vector4f zero = new Vector4f();
				Matrix4f rot = new Matrix4f(PartRotation.getRotationMultiplier(shape.partRotation));
				PartBounds bounds = part.getBounds();
				rot.setTranslation(0, 0, 0);
				if(bounds != null) {
					rot.translate(
						bounds.getWidth(),
						bounds.getHeight(),
						bounds.getDepth()
					);
				}
				zero.mul(rot);
				
				float xs = x + zero.x;
				float ys = y + zero.y;
				float zs = z + zero.z;
				if(xs < x) {
					float tmp = xs; xs = x; x = tmp;
				}
				if(ys < y) {
					float tmp = ys; ys = y; y = tmp;
				}
				if(zs < z) {
					float tmp = zs; zs = z; z = tmp;
				}

				if(x < posi.x) posi.x = x;
				if(y < posi.y) posi.y = y;
				if(z < posi.z) posi.z = z;
				if(xs > size.x) size.x = xs;
				if(ys > size.y) size.y = ys;
				if(zs > size.z) size.z = zs;
			}
		}
		
		return new Bounds3D(posi, size);
	}
	
	public static class Bounds3D {
		public float xMin, xMax;
		public float yMin, yMax;
		public float zMin, zMax;
		
		public Bounds3D(Vector3f xyzStart, Vector3f xyzEnd) {
			this(xyzStart.x, xyzEnd.x, xyzStart.y, xyzEnd.y, xyzStart.z, xyzEnd.z);
		}
		public Bounds3D(Vector2f x, Vector2f y, Vector2f z) {
			this(x.x, x.y, y.x, y.y, z.x, z.y);
		}
		public Bounds3D(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax) {
			this.xMin = xMin;
			this.xMax = xMax;
			this.yMin = yMin;
			this.yMax = yMax;
			this.zMin = zMin;
			this.zMax = zMax;
		}
		
		public Vector3f getMiddle() {
			return new Vector3f(
				(xMin - xMax) / 2.0f,
				(yMin - yMax) / 2.0f,
				(zMin - zMax) / 2.0f
			);
		}
		
		@Override
		public String toString() {
			return String.format(
				"Bounds3D@{xMin=%7.4f, xMax=%7.4f, yMin=%7.4f, yMax=%7.4f, zMin=%7.4f, zMax=%7.4f}",
				xMin, xMax,
				yMin, yMax,
				zMin, zMax
			);
		}
	}
}
