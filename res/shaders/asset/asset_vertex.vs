#version 130

in vec4 in_Position;
in vec2 in_Uv;
in vec3 in_Normal;
//in vec4 in_Weights;
//in vec4 in;

out vec2 dif_uv;
out vec2 asg_uv;
out vec2 nor_uv;
out vec2 ao_uv;
out vec3 pass_Normal;
out vec3 pass_Cam;
out vec4 pass_ShadowCoords;

uniform mat4 modelMatrix;
uniform mat4 projectionView;

uniform mat4 toShadowMapSpace;

void main() {
	gl_Position = projectionView * modelMatrix * in_Position;
	
	pass_ShadowCoords = toShadowMapSpace * modelMatrix * in_Position;
	
	mat4 test = projectionView * modelMatrix;
	pass_Cam = normalize(vec3(-test[0][2], -test[1][2], -test[2][2]));
	
	dif_uv = in_Uv;
	
	pass_Normal = in_Normal;
}