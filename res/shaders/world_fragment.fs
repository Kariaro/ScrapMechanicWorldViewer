#version 130

in vec2 dif_uv;
in vec2 asg_uv;
in vec2 nor_uv;
in vec2 ao_uv;

uniform sampler2D dif_tex;
uniform sampler2D asg_tex;
uniform sampler2D nor_tex;
uniform sampler2D ao_tex;

out vec4 out_color;

void main() {
	vec4 diffuse = texture2D(dif_tex, dif_uv);
	
	out_color = diffuse;
}