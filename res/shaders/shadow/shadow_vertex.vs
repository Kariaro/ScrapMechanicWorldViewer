#version 130

in vec3 in_Position;
uniform mat4 mvpMatrix;

void main() {
	gl_Position = mvpMatrix * vec4(in_Position, 1.0);
}
