package com.example.kotlinapp.Rubik

import android.opengl.GLES20
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniformMatrix4fv
import org.opencv.core.Scalar
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ArrowArch {
    enum class Amount { QUARTER_TURN, HALF_TURN }

    // Buffer for Arrow vertex-array
    private var vertexBufferArrow: FloatBuffer

    // Buffer for Arrow Outline vertex-array
    private var vertexBufferOutline: FloatBuffer

    // number of coordinates per vertex in this array
    private val COORDS_PER_VERTEX = 3

    // number of bytes in a float
    private val BYTES_PER_FLOAT = 4

    // number of total bytes in vertex stride: 12 in this case.
    private val VERTEX_STRIDE = COORDS_PER_VERTEX * BYTES_PER_FLOAT

    // number of vertices in the arrow arch
    private val VERTICES_PER_ARCH = 90 + 1

    // OpenGL Opaque Black Color
    private val opaqueBlack = floatArrayOf(0.0f, 0.0f, 0.0f, 1.0f)


    constructor(amount : Amount) {
        val angleScale = if (amount == Amount.QUARTER_TURN) 1.0 else 3.0

        val verticesArrow = FloatArray(VERTICES_PER_ARCH * 6)
        val verticesOutline = FloatArray(VERTICES_PER_ARCH * 6)

        for (i in 0 until VERTICES_PER_ARCH) {
            // Angle will range from 0 to 90, or 0 to 180.
            val angleRads = i.toDouble() * angleScale * Math.PI / 180.0
            val x = Math.cos(angleRads).toFloat()
            val y = Math.sin(angleRads).toFloat()

            verticesArrow[i * 6 + 0] = x
            verticesArrow[i * 6 + 1] = y
            verticesArrow[i * 6 + 2] = -1.0f * calculateWidth(angleRads)

            verticesArrow[i * 6 + 3] = x
            verticesArrow[i * 6 + 4] = y
            verticesArrow[i * 6 + 5] = +1.0f * calculateWidth(angleRads)

            // Fill from 0 to 272
            verticesOutline[i * 3 + 0] = x
            verticesOutline[i * 3 + 1] = y
            verticesOutline[i * 3 + 2] = -1.0f * calculateWidth(angleRads)

            // Fill from 545 to 273
            verticesOutline[(180 - i) * 3 + 3] = x
            verticesOutline[(180 - i) * 3 + 4] = y
            verticesOutline[(180 - i) * 3 + 5] = calculateWidth(angleRads)
        }

        // Setup vertex-array buffer. Vertices in float. A float has 4 bytes
        // This reserves memory that GPU has direct access to (correct?).
        val arrowVbb = ByteBuffer.allocateDirect(verticesArrow.size * 4)
        arrowVbb.order(ByteOrder.nativeOrder()) // Use native byte order
        vertexBufferArrow = arrowVbb.asFloatBuffer() // Convert from byte to float
        vertexBufferArrow.put(verticesArrow)         // Copy data into buffer
        vertexBufferArrow.position(0)           // Rewind

        val outlineVbb = ByteBuffer.allocateDirect(verticesOutline.size * 4)
        outlineVbb.order(ByteOrder.nativeOrder())
        vertexBufferOutline = outlineVbb.asFloatBuffer()
        vertexBufferOutline.put(verticesOutline)
        vertexBufferOutline.position(0)
    }


    /**
     * Calculate Width
     *
     * The width of the arrow is dependent upon the angle with the X axis.  For the
     * first 20 degrees, the arrow width increases to 0.6 to form the head, there after
     * the arrow width is constant at a width of 0.3.
     *
     * @param angleRads
     * @return
     */
    private fun calculateWidth(angleRads: Double): Float {
        val angleDegrees = angleRads * 180.0 / Math.PI

        return if (angleDegrees > 20.0)
            0.5f//0.3f
        else
            (angleDegrees / 20.0 * 0.7).toFloat()
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw this shape.
     * @param color - Color to apply to arrow
     */
    fun draw(mvpMatrix: FloatArray, color: Scalar, programID: Int) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(programID)

        // get handle to vertex shader's vPosition member
        val vertexArrayID = GLES20.glGetAttribLocation(programID, "a_Position")

        // Enable a handle to the cube vertices
        GLES20.glEnableVertexAttribArray(vertexArrayID)

        // get handle to fragment shader's vColor member
        val colorID = glGetUniformLocation(programID, "u_Color")

        // get handle to shape's transformation matrix
        val mvpMatrixID = glGetUniformLocation(programID, "u_Matrix")

        // Apply the projection and view transformation
        glUniformMatrix4fv(mvpMatrixID, 1, false, mvpMatrix, 0)

        // Draw Arrow Outer Surface
        drawArrowOuterSurface(color, colorID, vertexArrayID)

        // Draw Arrow Inner Surface (a bit darker than above)
        drawArrowInnerSurface(color, colorID, vertexArrayID)

        //Draw Arrow Outline
        drawArrowOutline(colorID, vertexArrayID)

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(vertexArrayID)

        // Disable cull face: i.e., will now attempt to render both sides instead of just front
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }

    /**
     * Draw Arrow Outline
     *
     * @param colorID
     * @param vertexArrayID
     */
    private fun drawArrowOutline(colorID: Int, vertexArrayID: Int) {
        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(vertexArrayID, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, VERTEX_STRIDE, vertexBufferOutline)

        GLES20.glUniform4fv(colorID, 1, opaqueBlack, 0)

        GLES20.glLineWidth(10.0f)

        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, VERTICES_PER_ARCH * 2)
    }

    /**
     * Draw Arrow Outer Side
     *
     * @param color
     * @param colorID
     * @param vertexArrayID
     */
    private fun drawArrowOuterSurface(color: Scalar, colorID: Int, vertexArrayID: Int) {
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(vertexArrayID, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, VERTEX_STRIDE, vertexBufferArrow)

        val glFrontSideColor = floatArrayOf(
            color.`val`[0].toFloat() / 256.0f,
            color.`val`[1].toFloat() / 256.0f,
            color.`val`[2].toFloat() / 256.0f, 1.0f)

        GLES20.glUniform4fv(colorID, 1, glFrontSideColor, 0)

        GLES20.glCullFace(GLES20.GL_FRONT)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTICES_PER_ARCH * 2)
    }

    /**
     * Draw Arrow Inner Side
     *
     * @param color
     * @param colorID
     * @param vertexArrayID
     */
    private fun drawArrowInnerSurface(color: Scalar, colorID: Int, vertexArrayID: Int) {
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        // Prepare the cube coordinate data
        GLES20.glVertexAttribPointer(vertexArrayID, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, VERTEX_STRIDE, vertexBufferArrow)

        val glBackSideColor = floatArrayOf(
            color.`val`[0].toFloat() / (256.0f + 256.0f),
            color.`val`[1].toFloat() / (256.0f + 256.0f),
            color.`val`[2].toFloat() / (256.0f + 256.0f), 1.0f)
        GLES20.glUniform4fv(colorID, 1, glBackSideColor, 0)

        GLES20.glCullFace(GLES20.GL_BACK)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTICES_PER_ARCH * 2)
    }
}