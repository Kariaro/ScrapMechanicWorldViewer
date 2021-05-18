#version 130
#define MAX_LIGHTS 8

in vec2 pass_Uv;
in vec4 pass_ShadowCoords;
in vec3 pass_lightVector[MAX_LIGHTS];
in vec3 pass_lightDirection;
in vec3 pass_toCameraVector;

out vec4 out_Color;

uniform sampler2D dif_tex;
uniform sampler2D asg_tex;
uniform sampler2D nor_tex;
uniform sampler2D ao_tex;

uniform sampler2D shadowMap;

uniform int hasAlpha;
uniform vec4 color;

float calcLightFactor() {
	float objectNearestLight = 0.0;
	for(int y = 0; y < 3; y++) {
		for(int x = 0; x < 3; x++) {
			objectNearestLight += texture(shadowMap, pass_ShadowCoords.xy + vec2(x - 1, y - 1) / 2048.0).r;
		}
	}
	
	float lightFactor = 1.0;
	objectNearestLight /= 9.0;
	float diff = pass_ShadowCoords.z - objectNearestLight;
	if(diff > 0.001) {
		lightFactor = 1.0 - min(diff * 100.0 - 0.1, 0.4);
	}
	
	return lightFactor;
}

void main() {
	vec4 dif = texture2D(dif_tex, pass_Uv);
	vec4 asg = texture2D(asg_tex, pass_Uv);
	vec4 nor = 2.0 * texture(nor_tex, pass_Uv, -1.0) - 1.0;
	vec4 ao = texture2D(ao_tex, pass_Uv);
	
	float lightFactor = calcLightFactor();
	vec3 unitNormal = normalize(nor.rgb);
	
	vec3 col_a = dif.rgb * dif.a;
	vec3 col_b = color.rgb * (1 - dif.a);
	vec4 diffuse = vec4(col_a + col_b, asg.r);
	
	if(hasAlpha > 0 && asg.r < 0.1) {
		discard;
	}
	
	float col_dot = min(max(dot(unitNormal, pass_lightDirection), 0.7), 2) * lightFactor;
	out_Color = vec4(diffuse.rgb * col_dot, 1.0);
}
