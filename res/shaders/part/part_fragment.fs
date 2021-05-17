#version 130
#define MAX_LIGHTS 8

in vec2 pass_Uv;
in vec4 pass_ShadowCoords;
in vec3 pass_lightVector[MAX_LIGHTS];
in mat3 pass_toTangentSpace;

out vec4 out_Color;

uniform sampler2D dif_tex;
uniform sampler2D asg_tex;
uniform sampler2D nor_tex;
uniform sampler2D ao_tex; // Ambient occlusion

uniform sampler2D shadowMap;

uniform int hasAlpha;
uniform vec4 color;

void main() {
	vec4 dif = texture2D(dif_tex, pass_Uv);
	vec4 asg = texture2D(asg_tex, pass_Uv);
	vec4 nor = 2.0 * texture(nor_tex, pass_Uv, -1.0) - 1.0;
	vec4 ao = texture2D(ao_tex, pass_Uv);
	
	float objectNearestLight = texture(shadowMap, pass_ShadowCoords.xy).r;
	float lightFactor = 1.0;
	if(pass_ShadowCoords.z - objectNearestLight > 0.001) {
		lightFactor = 1.0 - 0.4;
	}
	vec3 unitNormal = normalize(nor.rgb);
	
	vec3 col_a = dif.rgb * dif.a;
	vec3 col_b = color.rgb * (1 - dif.a);
	vec4 diffuse = vec4(col_a + col_b, asg.r);
	
	if(hasAlpha > 0 && asg.r < 0.1) {
		discard;
	}
	
	vec3 lightDir = normalize(vec3(0, 0, -1));
	vec3 col = normalize(-unitNormal * pass_toTangentSpace);
	float col_dot = min(max(dot(col, lightDir), 0.7), 2) * lightFactor;
	out_Color = vec4(diffuse.rgb * col_dot, 1.0);
}