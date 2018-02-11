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
public class Perspective {

	long window;
	int vbo;
	int vao;
	int program;

	public Perspective() {
		setup();
		mainLoop();
		cleanup();
	}

	public static void main(String[] args) {
		Perspective p = new Perspective();
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
				"uniform mat4 modelToScreen;",
				"in vec3 v;",
				"void main() {",
				"  gl_Position = modelToScreen * vec4(v, 1.0);",
				"}"
		};

		// Fragment shader source
		String[] fragment_shader = {
				"#version 330\n",
				"out vec4 frag_color;",
				"void main() {",
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

		glDeleteBuffers(vbo);
		glDeleteVertexArrays(vao);
		glfwDestroyWindow(window);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	void mainLoop() {

		///////////////////////////////////////////////////////////////////////////
		// Set up data
		float near_plane = 0.1f;
		float far_plane = 100f;
		float frustum_length = far_plane - near_plane;

		float data[][] = new float[][] {{1/ (float) Math.tan((50/360) * Math.PI), 0, 0, 0},
				{0, 1/ (float) Math.tan((50/360) * Math.PI), 0, 0},
				{0, 0, -((far_plane + near_plane) / frustum_length), -((2 * near_plane * far_plane) / frustum_length)},
				{0, 0, 1, 0}};
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		for (int col = 0; col < 4; col++) {
			for (int row = 0; row < 4; row++) {
				buffer.put(data[row][col]);
			}
		}
		buffer.flip();

		int uniformLoc = glGetUniformLocation(program, "modelToScreen");
		if (uniformLoc != -1) {
			glUniformMatrix4fv(uniformLoc, false, buffer);
		}

		// Fill a Java FloatBuffer object with memory-friendly floats
		int numVertices = 12;
		float[] coords = new float[] { -0.5f, -0.5f, 0.5f,  -0.5f, 0.5f, 0.5f,  0.5f, 0.5f, 0.5f,
				-0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f,
				-0.5f, -0.5f, 1.5f, 0.5f, 0.5f, 1.5f,-0.5f, 0.5f, 1.5f,
				-0.5f, -0.5f, 1.5f, 0.5f, -0.5f, 1.5f, 0.5f, 0.5f, 1.5f
		};
		FloatBuffer fbo = BufferUtils.createFloatBuffer(coords.length);
		fbo.put(coords);                                // Copy the vertex coords into the floatbuffer
		fbo.flip();                                     // Mark the floatbuffer ready for reads


		// Store the FloatBuffer's contents in a Vertex Buffer Object
		vbo = glGenBuffers();                  // Get an OGL name for the VBO
		glBindBuffer(GL_ARRAY_BUFFER, vbo);   // Activate the VBO
		glBufferData(GL_ARRAY_BUFFER, fbo, GL_STATIC_DRAW);  // Send VBO data to GPU

		// Bind the VBO in a Vertex Array Object
		vao = glGenVertexArrays();             // Get an OGL name for the VAO
		glBindVertexArray(vao);                    // Activate the VAO
		glEnableVertexAttribArray(0);              // Enable the VAO's first attribute (0)
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);  // Link VBO to VAO attrib 0

		///////////////////////////////////////////////////////////////////////////
		// Loop until window is closed

		while (!glfwWindowShouldClose(window)) {
			glfwPollEvents();

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			glBindVertexArray(vao);
			glDrawArrays(GL_TRIANGLES, 0 /* start */, numVertices /* num vertices */);

			glfwSwapBuffers(window);
		}
	}
}