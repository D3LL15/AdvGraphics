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
public class Lighting {

	long window;
	int vbo;
	int vbo_1;
	int vao;
	int program;

	public Lighting() {
		setup();
		mainLoop();
		cleanup();
	}

	public static void main(String[] args) {
		Lighting l = new Lighting();
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
		window = glfwCreateWindow(800 /* width */, 600 /* height */, "Lighting", 0, 0);
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
				"uniform mat4 model;",
				"uniform mat4 view;",
				"uniform mat4 projection;",
				"in vec3 v;",
				"in vec3 n;",
				"out vec3 position;",
				"out vec3 normal;",
				"void main() {\n",
				"  normal = normalize(view * model * vec4(n, 1.0)).xyz;\n",
				"  position = (view * model * vec4(v, 1.0)).xyz;\n",
				"  gl_Position = projection * view * model * vec4(v, 1.0);\n",
				//"  gl_Position = vec4(v, 1.0);",
				"}"
		};

		// Fragment shader source
		String[] fragment_shader = {
				"#version 330\n",
				"in vec3 position;",
				"in vec3 normal;",
				"out vec4 frag_color;",
				"const vec3 purple = vec3(0.2, 0.6, 0.8);",
				"const vec3 lightPosition = vec3(100.0, -50.0, 100.0);",
				"const vec3 eyePosition = vec3(0.0, 0.0, 0.0);",
				"void main() {",
				"  vec3 n = normalize(normal);",
				"  vec3 l = normalize(lightPosition - position);",
				"  vec3 e = normalize(position - eyePosition);",
				"  vec3 r = reflect(l, n);",
				"  float ambient = 0.2;",
				"  float diffuse = 0.4 * clamp(0, dot(n, l), 1);",
				"  float specular = 0.4 * pow(clamp(0, dot(e, r), 1), 2);",
				"  frag_color = vec4(purple * (ambient + diffuse + specular), 1.0);",
				"}"
		};

		// Compile vertex shader
		int vs = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs, vertex_shader);
		glCompileShader(vs);

		System.out.println(glGetShaderInfoLog(vs));

		// Compile fragment shader
		int fs = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, fragment_shader);
		glCompileShader(fs);

		System.out.println(glGetShaderInfoLog(fs));

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
		glDeleteBuffers(vbo_1);
		glDeleteVertexArrays(vao);
		glfwDestroyWindow(window);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	void mainLoop() {

		///////////////////////////////////////////////////////////////////////////
		// Set up data

		// Fill a Java FloatBuffer object with memory-friendly floats
		int numVertices = 36;
		//float[] coords = new float[] { -0.5f, -0.5f, 0.5f,  0.0f, 0.0f, 0.0f,  0.5f, -0.5f, 0.5f };
		float[] coords = new float[] { -1f, 1f, 1f, 1f, 1f, 1f, -1f, -1f, 1f, //top
				-1f, -1f, 1f, 1f, 1f, 1f, 1f, -1f, 1f, //top
				-1f, 1f, -1f, -1f, -1f, -1f, 1f, 1f, -1f,  //bottom
				-1f, -1f, -1f, 1f, -1f, -1f, 1f, 1f, -1f,  //bottom
				-1f, -1f, 1f, -1f, -1f, -1f, -1f, 1f, -1f, //-x
				-1f, 1f, -1f, -1f, 1f, 1f, -1f, -1f, 1f, //-x
				1f, -1f, 1f, 1f, 1f, -1f, 1f, -1f, -1f,  //+x
				1f, 1f, -1f, 1f, -1f, 1f, 1f, 1f, 1f,  //+x
				1f, 1f, 1f, -1f, 1f, 1f, -1f, 1f, -1f, //+y
				1f, 1f, 1f, -1f, 1f, -1f, 1f, 1f, -1f, //+y
				1f, -1f, 1f, -1f, -1f, -1f, -1f, -1f, 1f,  //-y
				1f, -1f, 1f, 1f, -1f, -1f, -1f, -1f, -1f  //-y
		};
		/*float[] coords = new float[] { -1f, 1f, 1f, -1f, -1f, 1f, 1f, 1f, 1f, //top
				-1f, -1f, 1f,1f, -1f, 1f, 1f, 1f, 1f,  //top
				-1f, 1f, -1f,1f, 1f, -1f, -1f, -1f, -1f,   //bottom
				-1f, -1f, -1f,1f, 1f, -1f, 1f, -1f, -1f,   //bottom
				-1f, -1f, 1f, -1f, 1f, -1f, -1f, -1f, -1f, //-x
				-1f, 1f, -1f, -1f, -1f, 1f, -1f, 1f, 1f, //-x
				1f, -1f, 1f,1f, -1f, -1f, 1f, 1f, -1f,   //+x
				1f, 1f, -1f,1f, 1f, 1f, 1f, -1f, 1f,   //+x
				1f, 1f, 1f, -1f, 1f, -1f, -1f, 1f, 1f, //+y
				1f, 1f, 1f, 1f, 1f, -1f, -1f, 1f, -1f, //+y
				1f, -1f, 1f, -1f, -1f, 1f, -1f, -1f, -1f,  //-y
				1f, -1f, 1f, -1f, -1f, -1f, 1f, -1f, -1f  //-y
		};*/
		FloatBuffer fbo = BufferUtils.createFloatBuffer(coords.length);
		fbo.put(coords);                                // Copy the vertex coords into the floatbuffer
		fbo.flip();                                     // Mark the floatbuffer ready for reads

		FloatBuffer fbo2 = BufferUtils.createFloatBuffer(coords.length);
		fbo2.put(coords);                                // Copy the normal coords into the floatbuffer
		fbo2.flip();                                     // Mark the floatbuffer ready for reads


		vao = glGenVertexArrays();             // Get an OGL name for the VAO
		glBindVertexArray(vao);                    // Activate the VAO
		int v = glGetAttribLocation(program, "v");
		System.out.println(v);
		glEnableVertexAttribArray(v);              // Enable the VAO's first attribute (0)
		int n = glGetAttribLocation(program, "n");
		System.out.println(n);
		glEnableVertexAttribArray(n);

		int modelLoc = glGetUniformLocation(program, "model");
		System.out.println(modelLoc);
		if (modelLoc != -1) {
			Matrix4f model = new Matrix4f();
			FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
			model.toBuffer(buffer);
			glUniformMatrix4fv(modelLoc, false, buffer);
		}

		int viewLoc = glGetUniformLocation(program, "view");
		System.out.println(viewLoc);
		if (viewLoc != -1) {
			//Matrix4f scale = Matrix4f.scale(0.5f, 0.5f, 0.5f);
			Matrix4f scale = new Matrix4f();
			Matrix4f translation = Matrix4f.translate(0.0f, 0.0f, -1.5f);
			Matrix4f rotation = Matrix4f.rotate(20f, 1f, 1f, 1f);
			Matrix4f view = translation.multiply(rotation.multiply(scale));
			FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
			view.toBuffer(buffer);
			glUniformMatrix4fv(viewLoc, false, buffer);
		}

		int projLoc = glGetUniformLocation(program, "projection");
		System.out.println(projLoc);
		if (projLoc != -1) {
			float ratio = 640f / 480f;
			//Matrix4f projection = Matrix4f.orthographic(-ratio, ratio, -1f, 1f, -10f, 10f);
			Matrix4f projection = Matrix4f.frustum(-ratio, ratio, -1f, 1f, 0.1f, 10f);
			/*Matrix4f projection = new Matrix4f(new Vector4f(1/ratio, 0f, 0f, 0f),
					new Vector4f(0f, 1, 0f, 0f),
					new Vector4f(0f, 0f, (-0.1f-10f)/(0.1f-10f), 1f),
					new Vector4f(0f, 0f, 2*10f*0.1f/(0.1f-10f), 0f));*/
			/*Matrix4f projection = new Matrix4f(new Vector4f(1/ratio, 0f, 0f, 0f),
					new Vector4f(0f, 1, 0f, 0f),
					new Vector4f(0f, 0f, (-0.1f-10f)/(0.1f-10f), 2*10f*0.1f/(0.1f-10f)),
					new Vector4f(0f, 0f, 1f, 0f));*/
			FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
			projection.toBuffer(buffer);
			glUniformMatrix4fv(projLoc, false, buffer);
		}

		// Store the FloatBuffer's contents in a Vertex Buffer Object
		vbo = glGenBuffers();                  // Get an OGL name for the VBO
		glBindBuffer(GL_ARRAY_BUFFER, vbo);   // Activate the VBO
		glVertexAttribPointer(v, 3, GL_FLOAT, false, 0, 0);  // Link VBO to VAO attrib 0
		glBufferData(GL_ARRAY_BUFFER, fbo, GL_STATIC_DRAW);  // Send VBO data to GPU

		vbo_1 = glGenBuffers();                  // Get an OGL name for the VBO
		glBindBuffer(GL_ARRAY_BUFFER, vbo_1);
		glVertexAttribPointer(n, 3, GL_FLOAT, false, 0, 0);
		glBufferData(GL_ARRAY_BUFFER, fbo2, GL_STATIC_DRAW);  // Send VBO data to GPU

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