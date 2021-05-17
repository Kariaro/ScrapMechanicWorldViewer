#version 130
#define MAX_LIGHTS 8

in vec4 pass_Position;
in vec4 pass_ShadowCoords;
in mat3 pass_toTangentSpace;

out vec4 out_Color;

uniform sampler2D dif_tex;
uniform sampler2D asg_tex;
uniform sampler2D nor_tex;
uniform sampler2D shadowMap;

uniform vec3 localTransform;
uniform vec3 cameraDirection;
uniform vec4 color;
uniform int tiling;

vec2 calculateUv() {
	// Calculate the uv depending the the world position
	vec3 pos = pass_Position.xyz + localTransform;
	vec2 uv = vec2(0, 0);//pos.xz;
	
	// Checks if if the texture is applied on the x, y or z axis
	// TODO: What if the block is shifted 0.5 units??
	float test_a = pos.x - floor(pos.x);
	float test_b = pos.y - floor(pos.y);
	float test_c = pos.z - floor(pos.z);
	if(test_a == 0) uv = pos.yz;
	if(test_b == 0) uv = pos.xz;
	if(test_c == 0) uv = pos.xy;
	
	// TODO: Flip y when loading the texture!
	uv.x =  uv.x / (tiling + 0.0);
	uv.y = -uv.y / (tiling + 0.0);
	
	return uv;
}

void main() {
	vec2 uv = calculateUv();
	
	vec4 dif = texture2D(dif_tex, uv);
	vec4 asg = texture2D(asg_tex, uv);
	vec4 nor = 2.0 * texture(nor_tex, uv, -1.0) - 1.0;
	
	float objectNearestLight = texture(shadowMap, pass_ShadowCoords.xy).r;
	float lightFactor = 1.0;
	if(pass_ShadowCoords.z - objectNearestLight > 0.001) {
		lightFactor = 1.0 - 0.4;
	}
	vec3 unitNormal = normalize(nor.rgb);
	
	// Apply the color to the transparent part of the diffuse
	vec3 col_a = dif.rgb * dif.a;
	vec3 col_b = color.rgb * (1 - dif.a);
	vec3 diffuse = clamp(col_a + col_b, 0.1, 1);
	//diffuse = (pass_Nor + 1.0) / 2.0;
	
	//out_Color = vec4(diffuse, 1);
	
	vec3 lightDir = normalize(vec3(0, 0, -1));
	vec3 col = normalize(-unitNormal * pass_toTangentSpace);
	float col_dot = min(max(dot(col, lightDir), 0.7), 2) * lightFactor;
	out_Color = vec4(diffuse.rgb * col_dot, 1.0);
}