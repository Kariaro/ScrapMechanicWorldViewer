#version 130
#define MAX_LIGHTS 8

in vec3 in_Position;
in vec3 in_Normal;
in vec3 in_Tangent;

out vec4 pass_Position;
out vec4 pass_ShadowCoords;
out vec3 pass_lightVector[MAX_LIGHTS];
out vec3 pass_lightDirection;

uniform mat4 projectionView;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec3 scale;

uniform mat4 toShadowMapSpace;
uniform mat4 lightDirection;
uniform vec3 lightPositionViewSpace[MAX_LIGHTS];

void main() {
	vec4 vert = vec4(in_Position * scale, 1.0);
	gl_Position = projectionView * modelMatrix * vert;
	pass_ShadowCoords = toShadowMapSpace * modelMatrix * vert;
	pass_Position = vert;
	
	mat4 modelViewMatrix = viewMatrix * modelMatrix;
	vec3 norm = normalize((modelViewMatrix * vec4(in_Normal, 0.0)).xyz);
	vec3 tang = normalize((modelViewMatrix * vec4(in_Tangent, 0.0)).xyz);
	vec3 bitang = normalize(cross(norm, tang));
	mat3 toTangentSpace = mat3(
		tang.x, bitang.x, norm.x,
		tang.y, bitang.y, norm.y,
		tang.z, bitang.z, norm.z
	);
	
	//pass_lightDirection = normalize(lightDirection * toTangentSpace);
	pass_lightDirection = normalize(vec3(0, 0, -1) * toTangentSpace);// * mat3(viewMatrix));
}
