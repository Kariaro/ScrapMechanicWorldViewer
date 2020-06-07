package sm.lwjgl.shader;

import org.lwjgl.opengl.GL20;
import sm.util.FileUtils;

public class PartShader extends Shader {
	public PartShader() throws Exception {
		programId = GL20.glCreateProgram();
		if(programId == 0) {
			throw new Exception("Could not create Shader");
		}
		

		createShaderCode(FileUtils.readStream(Shader.class.getResourceAsStream("/shaders/part/part_fragment.fs")), GL20.GL_FRAGMENT_SHADER);
		createShaderCode(FileUtils.readStream(Shader.class.getResourceAsStream("/shaders/part/part_vertex.vs")), GL20.GL_VERTEX_SHADER);
		bindAttrib(0, "in_Position");
		bindAttrib(1, "in_Uv");
		link();
		
		bind();
		createUniform("projectionView");
		createUniform("transformationMatrix");
		createUniform("color");
		
		setUniform("dif_tex", 0);
		setUniform("asg_tex", 1);
		setUniform("nor_tex", 2);
		setUniform("ao_tex", 3);
		unbind();
	}
}
