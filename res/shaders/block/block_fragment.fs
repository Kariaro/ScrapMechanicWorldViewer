#version 130

in vec4 pass_Pos;
out vec4 out_color;

uniform sampler2D dif_tex;
uniform sampler2D asg_tex;
uniform sampler2D nor_tex;

uniform int tiling;
uniform vec4 color;

void main() {
	// Calculate the uv depending the the world position
	vec2 uv = pass_Pos.xz + vec2(pass_Pos.y, 0);
	
	// Testing stuff.
	float test = pass_Pos.z - floor(pass_Pos.z);
	if(test == 0) {
		uv = pass_Pos.xy;
	}
	
	uv.x = uv.x / (tiling + 0.0);
	uv.y = uv.y / (tiling + 0.0);
	
	vec4 dif = texture2D(dif_tex, uv);
	
	// TODO: Apply the asg texture and nor texture
	
	vec3 col_a = dif.rgb * dif.a;
	vec3 col_b = color.rgb * (1 - dif.a);
	vec4 diffuse = vec4(col_a + col_b, 1);
	
	out_color = diffuse;
}