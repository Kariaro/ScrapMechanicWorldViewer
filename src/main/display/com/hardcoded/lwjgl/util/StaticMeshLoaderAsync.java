package com.hardcoded.lwjgl.util;

import static org.lwjgl.assimp.Assimp.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

/**
 * This is an asynchronious mesh loader
 * 
 * @author HardCoded
 * @since v0.2
 */
public class StaticMeshLoaderAsync {
	public static AsyncMesh[] load(String resourcePath) throws Exception {
		return load(resourcePath,
			aiProcessPreset_TargetRealtime_MaxQuality
//			aiProcess_OptimizeMeshes |
//			aiProcess_JoinIdenticalVertices |
//			aiProcess_Triangulate |
//			aiProcess_FixInfacingNormals |
//			aiProcess_CalcTangentSpace
		);
	}
	
	public static AsyncMesh[] load(String resourcePath, int flags) throws Exception {
		AIScene aiScene = aiImportFile(resourcePath, flags);
		if(aiScene == null) {
			throw new Exception("Error loading model");
		}
		
		int numMaterials = aiScene.mNumMaterials();
		PointerBuffer aiMaterials = aiScene.mMaterials();
		List<LoadedMaterial> materials = new ArrayList<>();
		for(int i = 0; i < numMaterials; i++) {
			AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
			processMaterial(aiMaterial, materials);
		}
		
		int numMeshes = aiScene.mNumMeshes();
		PointerBuffer aiMeshes = aiScene.mMeshes();
		
		AsyncMesh[] meshes = new AsyncMesh[numMeshes];
		for(int i = 0; i < numMeshes; i++) {
			AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
			meshes[i] = processMesh(aiMesh, materials);
		}
		
		return meshes;
	}
	
	private static void processMaterial(AIMaterial aiMaterial, List<LoadedMaterial> materials) throws Exception {
		AIColor4D colour = AIColor4D.create();
		AIString path = AIString.calloc();
		Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer)null, null, null, null, null, null);
		
		Vector4f ambient = LoadedMaterial.DEFAULT_COLOR;
		int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, colour);
		if(result == 0) {
			ambient = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
		}
		
		Vector4f diffuse = LoadedMaterial.DEFAULT_COLOR;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, colour);
		if(result == 0) {
			diffuse = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
		}
		
		Vector4f specular = LoadedMaterial.DEFAULT_COLOR;
		result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, colour);
		if(result == 0) {
			specular = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
		}
		
		AIString aiString = AIString.create();
		Assimp.aiGetMaterialString(aiMaterial, "?mat.name", aiTextureType_NONE, 0, aiString);
		
		LoadedMaterial material = new LoadedMaterial(aiString.dataString(), ambient, diffuse, specular, 1.0f);
		materials.add(material);
	}
	
	private static AsyncMesh processMesh(AIMesh aiMesh, List<LoadedMaterial> materials) {
		List<Float> vertices = new ArrayList<>();
		List<Float> textures = new ArrayList<>();
		List<Float> normals = new ArrayList<>();
		List<Float> tangents = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		
		processVertices(aiMesh, vertices);
		processTextCoords(aiMesh, textures);
		processNormals(aiMesh, normals);
		processTangents(aiMesh, tangents);
		processIndices(aiMesh, indices);
		
		LoadedMaterial material;
		int materialIdx = aiMesh.mMaterialIndex();
		
		if(materialIdx >= 0 && materialIdx < materials.size()) {
			material = materials.get(materialIdx);
		} else {
			material = new LoadedMaterial();
		}
		
		return new AsyncMesh(
			listToArray(vertices),
			listToArray(textures),
			listToArray(normals),
			listToArray(tangents),
			listIntToArray(indices),
			material
		);
	}
	
	private static void processVertices(AIMesh aiMesh, List<Float> vertices) {
		AIVector3D.Buffer aiVertices = aiMesh.mVertices();
		
		while(aiVertices.remaining() > 0) {
			AIVector3D aiVertex = aiVertices.get();
			vertices.add(aiVertex.x());
			vertices.add(aiVertex.y());
			vertices.add(aiVertex.z());
		}
	}
	
	private static void processTextCoords(AIMesh aiMesh, List<Float> textures) {
		AIVector3D.Buffer textCoords = aiMesh.mTextureCoords(0);
		int numTextCoords = textCoords != null ? textCoords.remaining() : 0;
		for(int i = 0; i < numTextCoords; i++) {
			AIVector3D textCoord = textCoords.get();
			textures.add(textCoord.x());
			textures.add(1 - textCoord.y());
		}
	}
	
	private static void processIndices(AIMesh aiMesh, List<Integer> indices) {
		int numFaces = aiMesh.mNumFaces();
		AIFace.Buffer aiFaces = aiMesh.mFaces();
		for(int i = 0; i < numFaces; i++) {
			AIFace aiFace = aiFaces.get(i);
			IntBuffer buffer = aiFace.mIndices();
			while(buffer.remaining() > 0) {
				indices.add(buffer.get());
			}
		}
	}
	
	private static void processNormals(AIMesh aiMesh, List<Float> normals) {
		AIVector3D.Buffer aiNormals = aiMesh.mNormals();
		if(aiNormals == null) return;
		
		while(aiNormals.remaining() > 0) {
			AIVector3D aiNormal = aiNormals.get();
			normals.add(aiNormal.x());
			normals.add(aiNormal.y());
			normals.add(aiNormal.z());
		}
	}
	
	private static void processTangents(AIMesh aiMesh, List<Float> tangents) {
		AIVector3D.Buffer aiTangents = aiMesh.mTangents();
		if(aiTangents == null) return;
		
		while(aiTangents.remaining() > 0) {
			AIVector3D aiTangent = aiTangents.get();
			tangents.add(aiTangent.x());
			tangents.add(aiTangent.y());
			tangents.add(aiTangent.z());
		}
	}
	
	private static int[] listIntToArray(List<Integer> list) {
		int[] ret = list.stream().mapToInt((Integer v) -> v).toArray();
		return ret;
	}

	private static float[] listToArray(List<Float> list) {
		if(list == null) return new float[0];
		final int size = list.size();
		float[] result = new float[size];
		
		for(int i = 0; i < size; i++) {
			Float value = list.get(i);
			result[i] = value == null ? Float.NaN:value;
		}
		
		return result;
	}
	
	
	/**
	 * This class does not process the data but stores it before
	 * it can be loaded into a VAO on the main thread.
	 * 
	 * @author HardCoded
	 * @since v0.2
	 */
	public static class AsyncMesh {
		public final float[] vertexs;
		public final float[] uvs;
		public final float[] normals;
		public final float[] tangents;
		public final int[] indices;
		public final LoadedMaterial material;
		
		public AsyncMesh(float[] vertexs, float[] uvs, float[] normals, float[] tangents, int[] indices, LoadedMaterial material) {
			this.vertexs = vertexs;
			this.uvs = uvs;
			this.normals = normals;
			this.tangents = tangents;
			this.indices = indices;
			this.material = material;
		}
	}
}
