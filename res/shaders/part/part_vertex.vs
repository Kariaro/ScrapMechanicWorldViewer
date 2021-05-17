#version 150
#define MAX_LIGHTS 8

in vec4 in_Position;
in vec2 in_Uv;
in vec3 in_Normal;
in vec3 in_Tangent;

out vec2 pass_Uv;
out vec4 pass_ShadowCoords;
out vec3 pass_lightVector[MAX_LIGHTS];
out mat3 pass_toTangentSpace;

uniform mat4 projectionView;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

uniform mat4 toShadowMapSpace;
uniform vec3 lightPositionEyeSpace[MAX_LIGHTS];

void main() {
	gl_Position = projectionView * modelMatrix * in_Position;
	pass_ShadowCoords = toShadowMapSpace * modelMatrix * in_Position;
	pass_Uv = in_Uv;
	
	mat4 modelViewMatrix = viewMatrix * modelMatrix;
	vec3 norm = normalize((modelViewMatrix * vec4(in_Normal, 0.0)).xyz);
	vec3 tang = normalize((modelViewMatrix * vec4(in_Tangent, 0.0)).xyz);
	vec3 bitang = normalize(cross(norm, tang));
	mat3 toTangentSpace = mat3(
		tang.x, bitang.x, norm.x,
		tang.y, bitang.y, norm.y,
		tang.z, bitang.z, norm.z
	);
	
	pass_toTangentSpace = toTangentSpace;
	
	//vec4 positionRelativeToCam = projectionView * modelMatrix * in_Position;
	//for(int i = 0; i < MAX_LIGHTS; i++) {
	//	pass_lightVector[i] = toTangentSpace * (lightPositionEyeSpace[i] - positionRelativeToCam.xyz);
	//}
	//pass_toCameraVector = toTangentSpace * (-positionRelativeToCam.xyz);
}