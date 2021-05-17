#version 130

in vec2 pass_Uv;
in vec3 pass_toCameraVector;
in vec3 pass_lightVector[8];
in vec3 pass_Tangent;
in mat3 pass_toTangentSpace;

out vec4 out_Color;

// RGBA
uniform sampler2D dif_tex;

// ASG
// r: alpha
// g: specular level
// b: glow
// a: reflectivity
uniform sampler2D asg_tex;
uniform sampler2D nor_tex;
uniform sampler2D ao_tex; // Ambient occlusion

uniform int hasAlpha;
uniform vec4 color;

void main() {
	// Calculate the uv depending the the world position
	vec4 dif = texture2D(dif_tex, pass_Uv);
	vec4 asg = texture2D(asg_tex, pass_Uv);
	vec4 nor = 2.0 * texture(nor_tex, pass_Uv, -1.0) - 1.0;
	vec4 ao = texture2D(ao_tex, pass_Uv);
	
	float alpha = asg.r;
	float shineDamper = asg.g;
	float glow = asg.b;
	float reflectivity = asg.a;
	
	vec3 attenuation_i = vec3(1, 0, 0);
	vec3 lightColour_i = vec3(1, 1, 1);
	vec3 unitNormal = normalize(nor.rgb);
	vec3 unitVectorToCamera = normalize(pass_toCameraVector);
	//shineDamper = 10.0;
	//reflectivity = 0.5;
	
	/*
	vec3 totalDiffuse = vec3(0.0);
	vec3 totalSpecular = vec3(0.0);
	for(int i = 0; i < 1; i++){
		float dist = length(pass_lightVector[i]);
		float attFactor = attenuation_i.x + (attenuation_i.y * dist) + (attenuation_i.z * dist * dist);
		vec3 unitLightVector = normalize(pass_lightVector[i]);	
		float nDotl = dot(unitNormal, unitLightVector);
		float brightness = max(nDotl,0.0);
		vec3 lightDirection = -unitLightVector;
		vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
		float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
		specularFactor = max(specularFactor, 0.0);
		float dampedFactor = pow(specularFactor, shineDamper);
		totalDiffuse = totalDiffuse + (brightness * lightColour_i) / attFactor;
		totalSpecular = totalSpecular + (dampedFactor * reflectivity * lightColour_i) / attFactor;
	}
	
	totalDiffuse = max(totalDiffuse, 0.2);
	*/
	
	vec3 col_a = dif.rgb * dif.a;
	vec3 col_b = color.rgb * (1 - dif.a);
	vec4 diffuse = vec4(col_a + col_b, asg.r);
	
	if(hasAlpha > 0 && asg.r < 0.1) {
		discard;
	}
	
	//vec3 unitTangent = normalize(2.0 * pass_Tangent - 1.0);
	//diffuse = vec4((diffuse.xyz / 6) * 5 + vec3(1 / 6.0), 1);
	//float diff = min(max(dot(unitTangent, -unitNormal), 0.3), 4);
	//vec3 reflectedLightDirection = reflect(-unitNormal, unitTangent);
	//float specularFactor = max(dot(reflectedLightDirection, unitVectorToCamera), 0.0);
	// diffuse.xyz *= clamp(diff, 0.8, 1);
	
	vec3 lightDir = normalize(vec3(0, 0, -1));
	vec3 col = normalize(-unitNormal * pass_toTangentSpace);
	
	float col_dot = min(max(dot(col, lightDir), 0.3), 2);
	out_Color = vec4(diffuse.rgb * col_dot, 1.0);
	//out_Color = vec4(totalDiffuse, 1.0) * dif + vec4(totalSpecular,1.0);
	//out_Color = vec4(dif.rgb * diff, 1.0) + vec4(specularFactor, specularFactor, specularFactor, 1.0);
	//out_Color = vec4(pass_Tangent, 1.0);
}