#version 130

in vec4 in_Position;
in vec2 in_Uv;

out vec2 dif_uv;
out vec2 asg_uv;
out vec2 nor_uv;
out vec2 ao_uv;

uniform mat4 transformationMatrix;
uniform mat4 projectionView;

void main() {
	gl_Position = projectionView * transformationMatrix * in_Position;
	
	dif_uv = in_Uv;
	asg_uv = vec2(0);
	nor_uv = vec2(0);
	ao_uv = vec2(0);
}