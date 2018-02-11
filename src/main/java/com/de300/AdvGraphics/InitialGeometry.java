package com.de300.AdvGraphics;


import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

//relevant tutorial at https://github.com/SilverTiger/lwjgl3-tutorial/wiki/Textures
public class InitialGeometry {

	long window;
	int vbo_0;
	int vbo_1;
	int vao;
	int program;

	public InitialGeometry() {
		setup();
		mainLoop();
		cleanup();
	}

	public static void main(String[] args) {
		InitialGeometry ig = new InitialGeometry();
	}

	void setup() {
		///////////////////////////////////////////////////////////////////////////
		// Set up GLFW window

		GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);
		glfwSetErrorCallback(errorCallback);
		glfwInit();
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		window = glfwCreateWindow(800 /* width */, 600 /* height */, "InitialGeometry", 0, 0);
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);

		///////////////////////////////////////////////////////////////////////////
		// Set up OpenGL

		GL.createCapabilities();
		glClearColor(0.2f, 0.4f, 0.6f, 0.0f);
		glClearDepth(1.0f);

		///////////////////////////////////////////////////////////////////////////
		// Set up minimal shader programs

		// Vertex shader source
		String[] vertex_shader = {
				"#version 330\n",
				"in vec3 v;",
				"in vec2 texcoord;",
				"out vec2 textureCoord;",
				"void main() {",
				"  gl_Position = vec4(v, 1.0);",
				"  textureCoord = texcoord;",
				"}"
		};

		// Fragment shader source
		String[] fragment_shader = {
				"#version 330\n",
				"in vec2 textureCoord;",
				"out vec4 frag_color;",
				"uniform sampler2D texImage;",
				"void main() {",
				"  vec4 textureColor = texture(texImage, textureCoord);",
				//"  fragColor = textureColor;",
				"  frag_color = vec4(1.0, 1.0, 1.0, 1.0);",
				"}"
		};

		// Compile vertex shader
		int vs = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs, vertex_shader);
		glCompileShader(vs);

		// Compile fragment shader
		int fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, fragment_shader);
		glCompileShader(fs);

		// Link vertex and fragment shaders into an active program
		program = glCreateProgram();
		glAttachShader(program, vs);
		glAttachShader(program, fs);
		glLinkProgram(program);
		glUseProgram(program);
	}

	void cleanup() {
		///////////////////////////////////////////////////////////////////////////
		// Clean up

		glDeleteBuffers(vbo_0);
		glDeleteBuffers(vbo_1);
		glDeleteVertexArrays(vao);
		glfwDestroyWindow(window);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	void mainLoop() {

		/////////////////////////////////////
		// textures

		int texture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texture);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		int width[] = {0};
		int height[] = {0};
		int comp[] = {0};

		stbi_set_flip_vertically_on_load(true);
		String s = "/Users/bionicbug/Documents/Cambridge/3rdYear/Supervisions/Graphics/brick.bmp";
		ByteBuffer image = stbi_load(s.subSequence(0, s.length()), width, height, comp, 4);
		if (image == null) {
			throw new RuntimeException("Failed to load a texture file!"
					+ System.lineSeparator() + stbi_failure_reason());
		}

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

		///////////////////////////////////////////////////////////////////////////
		// Set up data

		// Fill a Java FloatBuffer object with memory-friendly floats
		int numVertices = 100;
		float[] coords = new float[numVertices * 3];
		for (int i = 0; i < numVertices; i++) {
			coords[3 * i] = (float) (0.5 * Math.cos(2 * Math.PI * i / numVertices));
			coords[3 * i + 1] = (float) (0.5 * Math.sin(2 * Math.PI * i / numVertices));
			coords[3 * i + 2] = 0.0f;
		}
		FloatBuffer fbo = BufferUtils.createFloatBuffer(coords.length);
		fbo.put(coords);                                // Copy the vertex coords into the floatbuffer
		fbo.flip();                                     // Mark the floatbuffer ready for reads

		float[] texCoords = new float[numVertices * 2];
		for (int i = 0; i < numVertices; i++) {
			texCoords[2 * i] = (float) (0.5 * Math.cos(2 * Math.PI * i / numVertices));
			texCoords[2 * i + 1] = (float) (0.5 * Math.sin(2 * Math.PI * i / numVertices));
		}
		FloatBuffer fbo2 = BufferUtils.createFloatBuffer(texCoords.length);
		fbo2.put(texCoords);                                // Copy the vertex coords into the floatbuffer
		fbo2.flip();                                     // Mark the floatbuffer ready for reads


		// Bind the VBO in a Vertex Array Object
		vao = glGenVertexArrays();             // Get an OGL name for the VAO
		glBindVertexArray(vao);                    // Activate the VAO
		glEnableVertexAttribArray(0);              // Enable the VAO's first attribute (0)
		int texAttrib = glGetAttribLocation(program, "texcoord");
		glEnableVertexAttribArray(texAttrib);

		// Store the FloatBuffer's contents in a Vertex Buffer Object
		vbo_0 = glGenBuffers();                  // Get an OGL name for the VBO
		glBindBuffer(GL_ARRAY_BUFFER, vbo_0);   // Activate the VBO
		glBufferData(GL_ARRAY_BUFFER, fbo, GL_STATIC_DRAW);  // Send VBO data to GPU
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);  // Link VBO to VAO attrib 0

		vbo_1 = glGenBuffers();                  // Get an OGL name for the VBO
		glBindBuffer(GL_ARRAY_BUFFER, vbo_1);
		glBufferData(GL_ARRAY_BUFFER, fbo2, GL_STATIC_DRAW);  // Send VBO data to GPU
		glVertexAttribPointer(texAttrib, 2, GL_FLOAT, false, 0, 0);

		///////////////////////////////////////////////////////////////////////////
		// Loop until window is closed

		while (!glfwWindowShouldClose(window)) {
			glfwPollEvents();

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			glBindVertexArray(vao);
			glDrawArrays(GL_TRIANGLE_FAN, 0 /* start */, numVertices /* num vertices */);

			glfwSwapBuffers(window);
		}
	}
}