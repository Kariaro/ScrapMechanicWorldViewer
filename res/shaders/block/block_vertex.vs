#version 130

in vec4 in_Position;
in vec3 in_Normal;

out vec4 pass_Pos;
out vec3 pass_Nor;
out vec3 pass_Cam;

uniform mat4 transformationMatrix;
uniform mat4 projectionView;
uniform vec3 scale;

void main() {
	vec4 vert = in_Position * vec4(scale, 1);
	gl_Position = projectionView * transformationMatrix * vert;
	
	pass_Cam = normalize(vec3(projectionView[0][2], projectionView[1][2], projectionView[2][2]));
	pass_Nor = in_Normal;
	pass_Pos = vert;
}