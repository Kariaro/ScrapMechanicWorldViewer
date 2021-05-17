#version 150

in vec4 in_Position;
in vec2 in_Uv;
in vec3 in_Normal;
in vec3 in_Tangent;

out vec2 pass_Uv;
out vec3 pass_toCameraVector;
out vec3 pass_lightVector[8];
out vec3 pass_Tangent;
out mat3 pass_toTangentSpace;

uniform mat4 projectionView;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec3 lightPositionEyeSpace[8];

void main() {
	mat4 modelViewMatrix = viewMatrix * modelMatrix;
	vec4 positionRelativeToCam = projectionView * modelMatrix * in_Position;
	gl_Position = projectionView * modelMatrix * in_Position;
	pass_Uv = in_Uv;
	
	vec3 norm = normalize((modelViewMatrix * vec4(in_Normal, 0.0)).xyz);
	vec3 tang = normalize((modelViewMatrix * vec4(in_Tangent, 0.0)).xyz);
	vec3 bitang = normalize(cross(norm, tang));
	pass_Tangent = tang;
	
	mat3 toTangentSpace = mat3(
		tang.x, bitang.x, norm.x,
		tang.y, bitang.y, norm.y,
		tang.z, bitang.z, norm.z
	);
	
	pass_toTangentSpace = toTangentSpace;
	
	for(int i = 0; i < 8; i++) {
		pass_lightVector[i] = toTangentSpace * (lightPositionEyeSpace[i] - positionRelativeToCam.xyz);
	}
	
	pass_toCameraVector = toTangentSpace * (-positionRelativeToCam.xyz);
}