#version 150

in vec4 pass_Color;
in vec4 pass_Pos;
in vec2 pass_Uv;
in vec4 pass_ShadowCoords;

in flat mat4 pass_MatA; // m00, m01
in flat mat4 pass_MatB; // m10, m11

uniform sampler2D tex_0;
uniform sampler2D tex_1;
uniform sampler2D tex_2;
uniform sampler2D tex_3;
uniform sampler2D tex_4;
uniform sampler2D tex_5;
uniform sampler2D tex_6;
uniform sampler2D tex_7;
uniform sampler2D tex_8;


uniform sampler2D shadowMap;

out vec4 out_color;

vec4 calculateColor(vec4 color, vec2 uv, vec4 mat_0, vec4 mat_1) {
	vec4 t1 = texture2D(tex_1, uv);
	vec4 t2 = texture2D(tex_2, uv);
	vec4 t3 = texture2D(tex_3, uv);
	vec4 t4 = texture2D(tex_4, uv);
	vec4 t5 = texture2D(tex_5, uv);
	vec4 t6 = texture2D(tex_6, uv);
	vec4 t7 = texture2D(tex_7, uv);
	vec4 t8 = texture2D(tex_8, uv);
	
	vec4 result = color;
	if(mat_0.x > 0) result = mix(result, t1, mat_0.x);
	if(mat_0.y > 0) result = mix(result, t2, mat_0.y);
	if(mat_0.z > 0) result = mix(result, t3, mat_0.z);
	if(mat_0.w > 0) result = mix(result, t4, mat_0.w);
	if(mat_1.x > 0) result = mix(result, t5, mat_1.x);
	if(mat_1.y > 0) result = mix(result, t6, mat_1.y);
	if(mat_1.z > 0) result = mix(result, t7, mat_1.z);
	if(mat_1.w > 0) result = mix(result, t8, mat_1.w);
	
	result.a = 1;
	return result;
}

void main() {
	float objectNearestLight = texture(shadowMap, pass_ShadowCoords.xy).r;
	float lightFactor = 1.0;
	if(pass_ShadowCoords.z - objectNearestLight > 0.001) {
		lightFactor = 1.0 - 0.4;
	}
	
	
	vec2 uv = pass_Pos.xy / 4.0;
	vec4 t0 = texture2D(tex_0, uv);
	
	float a = pass_Uv.x;
	float b = pass_Uv.y;
	vec4 dif;
	
	if(a < 0.5) {
		if(b < 0.5) { // m00
			dif = calculateColor(t0, uv, pass_MatA[0], pass_MatA[1]);
		} else { // m01
			dif = calculateColor(t0, uv, pass_MatA[2], pass_MatA[3]);
		}
	} else {
		if(b < 0.5) { // m10
			dif = calculateColor(t0, uv, pass_MatB[0], pass_MatB[1]);
		} else { // m11
			dif = calculateColor(t0, uv, pass_MatB[2], pass_MatB[3]);
		}
	}
	
	dif.a = 1;
	out_color = dif * vec4(pass_Color.rgb, 1) * lightFactor;
}