#version 130

in vec4 in_Position;
out vec4 pass_Pos;

uniform mat4 transformationMatrix;
uniform mat4 projectionView;
uniform vec3 scale;

void main() {
	vec4 vert = in_Position * vec4(scale, 1);
	gl_Position = projectionView * transformationMatrix * vert;
	
	pass_Pos = vert * transformationMatrix;
}