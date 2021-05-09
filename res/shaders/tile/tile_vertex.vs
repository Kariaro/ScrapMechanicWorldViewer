#version 460

in vec4 in_Position;
in vec4 in_Color;
in vec2 in_Uv;

// Ground material (0xAA BB CC DD), (0xEE FF GG HH)
// 4 bytes * 4 ints = 16 bytes
// 32 bytes

in vec4 in_Material_0;
in vec4 in_Material_1;
in vec4 in_Material_2;
in vec4 in_Material_3;

out vec4 pass_Color;
out mat4 pass_MatA;
out mat4 pass_MatB;
out vec2 pass_Uv;
out vec4 pass_Pos;

uniform mat4 transformationMatrix;
uniform mat4 projectionView;

uniform ivec4 testColor;

// Works
/*vec4 createVec(float value) {
	return vec4(
		float((value >> 16) & 255) / 255.0,
		float((value >>  8) & 255) / 255.0,
		float((value      ) & 255) / 255.0,
		
		float((value >> 24) & 255) / 255.0
	);
}
*/

vec4 createVec(vec2 v) {
	return vec4(
		mod(v.y, 256.0)    / 255.001,
		floor(v.x / 256.0) / 255.001,
		mod(v.x, 256.0)    / 255.001,
		floor(v.y / 256.0) / 255.001
	);
}

void main() {
	gl_Position = projectionView * transformationMatrix * in_Position;
	
	pass_Color = in_Color;
	pass_Pos = in_Position;
	pass_Uv = in_Uv;
	
	pass_MatA = mat4(
		createVec(in_Material_0.xy), // m00
		createVec(in_Material_0.zw), // m00
		createVec(in_Material_1.xy), // m01
		createVec(in_Material_1.zw)  // m01
	);
	
	pass_MatB = mat4(
		createVec(in_Material_2.xy), // m10
		createVec(in_Material_2.zw), // m10
		createVec(in_Material_3.xy), // m11
		createVec(in_Material_3.zw)  // m11
	);
}