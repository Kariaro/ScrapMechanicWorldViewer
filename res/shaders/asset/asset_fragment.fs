#version 130

in vec3 pass_Normal;
in vec3 pass_Cam;
in vec2 dif_uv;

out vec4 out_color;

uniform sampler2D dif_tex;
uniform sampler2D asg_tex;
uniform sampler2D nor_tex;
uniform sampler2D ao_tex;

uniform int hasAlpha;
uniform int tiling;
uniform vec4 color;


void main() {
	// Calculate the uv depending the the world position
	vec4 dif = texture2D(dif_tex, dif_uv);
	vec4 asg = texture2D(asg_tex, dif_uv);
	vec4 nor = texture2D(nor_tex, dif_uv);
	vec4 ao = texture2D(ao_tex, dif_uv);
	
	vec3 col_a = dif.rgb * dif.a;
	vec3 col_b = color.rgb * (1 - dif.a);
	vec4 diffuse = vec4(col_a + col_b, 1);
	
	
	if(hasAlpha > 0 && asg.r < 0.1) {
		discard;
	}
	
	diffuse = vec4((diffuse.xyz / 6) * 5 + vec3(1 / 6.0), 1);
	// float diff = max(dot(pass_Normal, pass_Cam), 0);
	// diffuse.xyz *= clamp(diff, 0.8, 1);
	
	out_color = diffuse;
}