#version 130

in vec4 in_Position;
in vec2 in_Uv;
in vec3 in_Normal;
//in vec4 in_Weights; // Remove ????????
//in vec4 in; // Remove ????????

out vec2 dif_uv;
out vec2 asg_uv;
out vec2 nor_uv;
out vec2 ao_uv;
out vec3 pass_Normal;

uniform mat4 transformationMatrix;
uniform mat4 projectionView;

void main() {
	gl_Position = projectionView * transformationMatrix * in_Position;
	
	dif_uv = in_Uv;
	asg_uv = vec2(0);
	nor_uv = vec2(0);
	ao_uv = vec2(0);
	
	pass_Normal = in_Normal;
}