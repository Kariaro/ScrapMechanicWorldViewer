#version 130

in vec4 pass_Pos;
in vec3 pass_Nor;
in vec3 pass_Cam;
out vec4 out_color;

uniform sampler2D dif_tex;
uniform sampler2D asg_tex;
uniform sampler2D nor_tex;

uniform vec3 localTransform;
uniform vec3 cameraDirection;
uniform vec4 color;
uniform int tiling;

void main() {
	// Calculate the uv depending the the world position
	vec3 pos = pass_Pos.xyz + localTransform;
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
	
	vec4 dif = texture2D(dif_tex, uv);
	vec4 asg = texture2D(asg_tex, uv);
	vec3 nor = texture2D(nor_tex, uv).xyz;
	
	// Normalize the normal map. From [0,1] -> [-1, 1]
	nor = nor * 2.0 - 1.0;
		
	// TODO: Apply the asg texture and nor texture
	
	// Apply the color to the transparent part of the diffuse
	vec3 col_a = dif.rgb * dif.a;
	vec3 col_b = color.rgb * (1 - dif.a);
	vec3 diffuse = clamp(col_a + col_b, 0.1, 1);
	
	// TODO: Calculate the normal using Bitangent, Tangent!
	if(false) {
		if(pass_Nor.x == 0 && pass_Nor.z == 0) {
			mat3x3 TBN = mat3x3(
				0, 0, -1,
				-1, 0, 0,
				0, pass_Nor.y, 0
			);
			
			vec3 worldNormal = TBN * nor;
			
			float diff = max(dot(worldNormal, pass_Cam), 0);
			diffuse *= clamp(diff, 0.6, 1);
		}
		if(pass_Nor.x == 0 && pass_Nor.y == 0) {
			mat3x3 TBN = mat3x3(
				-1, 0, 0,
				0, 0, -1,
				0, 0, pass_Nor.z
			);
			
			vec3 worldNormal = TBN * nor;
			
			float diff = max(dot(worldNormal, pass_Cam), 0);
			diffuse *= clamp(diff, 0.6, 1);
		}
		if(pass_Nor.y == 0 && pass_Nor.z == 0) {
			mat3x3 TBN = mat3x3(
				0, 0, -1,
				-1, 0, 0,
				pass_Nor.x, 0, 0
			);
			
			vec3 worldNormal = TBN * nor;
			
			float diff = max(dot(worldNormal, pass_Cam), 0);
			diffuse *= clamp(diff, 0.6, 1);
		}
	}
	
	out_color = vec4(diffuse, 1);
}