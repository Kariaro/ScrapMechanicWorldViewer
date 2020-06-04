package sm.lwjgl.shader;

import org.lwjgl.opengl.GL20;

import sm.util.FileUtils;

public class BlockShader extends Shader {
	public BlockShader() throws Exception {
		programId = GL20.glCreateProgram();
		if(programId == 0) {
			throw new Exception("Could not create Shader");
		}
		
		createShaderCode(FileUtils.readStream(Shader.class.getResourceAsStream("/shaders/block/block_fragment.fs")), GL20.GL_FRAGMENT_SHADER);
		createShaderCode(FileUtils.readStream(Shader.class.getResourceAsStream("/shaders/block/block_vertex.vs")), GL20.GL_VERTEX_SHADER);
		bindAttrib(0, "in_Position");
		link();
		
		createUniform("projectionView");
		createUniform("transformationMatrix");
		createUniform("tiling");
		createUniform("color");
		createUniform("scale");
		
		setUniform("dif", 0);
		setUniform("asg", 1);
		setUniform("nor", 2);
	}
}
