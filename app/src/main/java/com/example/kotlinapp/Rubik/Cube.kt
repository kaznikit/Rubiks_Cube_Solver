package com.example.kotlinapp.Rubik

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.ArrayList
import android.R.color
import com.example.kotlinapp.Util.Constants


class Cube() {
    val spaceBetweenCubies : Float = 0.3f;

    val lowerBound : Float = -3.0f;
    val upperBound : Float = 3.0f;

    val sideLength : Float = (upperBound - lowerBound - spaceBetweenCubies * 2) / 3.0f;

    val cubies = arrayListOf<Cubie>()

    val world : World = World()

    private val BYTES_PER_FLOAT = 4

    private var mIndexCount = 0

    private lateinit var mVertexBuffer: FloatBuffer

    internal var aPositionLocation = 0
    internal var uColorLocation = 0
    internal var uMatrixLocation = 0

    init {
        val byteBuf = ByteBuffer.allocateDirect(27 * 6 * 4 * 3 * BYTES_PER_FLOAT)
        byteBuf.order(ByteOrder.nativeOrder())
        mVertexBuffer = byteBuf.asFloatBuffer()

        CreateCubies()
    }

    fun resetVertexBuffer(){
        for(cubie in cubies){
            for(tile in cubie.tiles) {
                for(point in tile.coordinates) {
                    mVertexBuffer.put(point.x)
                    mVertexBuffer.put(point.y)
                    mVertexBuffer.put(point.z)
                }
            }
        }
    }

    fun CreateCubies(){
        var leftX = lowerBound
        var leftY = lowerBound
        var leftZ = lowerBound

        for(i in 0 until 3){
            for(j in 0 until 3){
                for(k in 0 until 3) {
                    cubies.add(Cubie(leftX, leftY, leftZ, sideLength, world))
                    leftX += sideLength
                }
                leftX = lowerBound
                leftZ += sideLength
            }
            leftX = lowerBound
            leftZ = lowerBound
            leftY += sideLength
        }
        //world.resetVertexBuffer()
        resetVertexBuffer()
    }

    fun draw(mvpMatrix: FloatArray, programID: Int) {
        GLES20.glUseProgram(programID)
        // примитивы
        aPositionLocation = GLES20.glGetAttribLocation(programID, "a_Position")
        mVertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionLocation)
        // цвет
        uColorLocation = GLES20.glGetUniformLocation(programID, "u_Color")
        // матрица
        uMatrixLocation = GLES20.glGetUniformLocation(programID, "u_Matrix")
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mvpMatrix, 0)
        var k = 0

        var x : Float = 1.0f
        var y : Float = 1.0f
        var z : Float = 0.0f

        for(cubie in cubies) {

            GLES20.glUniform4f(uColorLocation, x, y, z,0.0f)

            for (tile in cubie.tiles) {
                // Draw Triangles
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, k * 4, 4)
                k++
            }
            x -= 0.031f
            y -= 0.031f
            z += 0.031f
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }
}