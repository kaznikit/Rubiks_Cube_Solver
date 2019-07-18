package com.example.kotlinapp.Rubik

import android.opengl.GLES20
import android.opengl.GLES20.*
import android.provider.SyncStateContract
import com.example.kotlinapp.Util.Constants
import java.nio.*
import java.util.ArrayList

class World {

    private val mCubieList = ArrayList<Cubie>()
    private val mVertexList = ArrayList<Vertex>()

    private val BYTES_PER_FLOAT = 4

    private var mIndexCount = 0

    private lateinit var mVertexBuffer: FloatBuffer

    internal var aPositionLocation = 0
    internal var uColorLocation = 0
    internal var uMatrixLocation = 0

    constructor() {
        val byteBuf = ByteBuffer.allocateDirect(6 * 9 * 4 * 3 * BYTES_PER_FLOAT)
        byteBuf.order(ByteOrder.nativeOrder())
        mVertexBuffer = byteBuf.asFloatBuffer()
    }

    fun resetVertexBuffer(){
        for(vert in mVertexList){
            mVertexBuffer.put(vert.x)
            mVertexBuffer.put(vert.y)
            mVertexBuffer.put(vert.z)
        }
    }

    fun addVertex(vertex : Vertex) : Vertex {
        val vert = mVertexList.find { v: Vertex -> v.IsEqual(v, vertex) }
        if(vert != null){
            return vert
        }
        mVertexList.add(vertex)
        return vertex
    }

    fun draw(mvpMatrix: FloatArray, programID: Int) {
        GLES20.glUseProgram(programID)
        // примитивы
        aPositionLocation = glGetAttribLocation(programID, "a_Position")
        mVertexBuffer.position(0)
        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, 0, mVertexBuffer)
        glEnableVertexAttribArray(aPositionLocation)
        // цвет
        uColorLocation = glGetUniformLocation(programID, "u_Color")
        // матрица
        uMatrixLocation = glGetUniformLocation(programID, "u_Matrix")
        glUniformMatrix4fv(uMatrixLocation, 1, false, mvpMatrix, 0)
        var k = 0

        for (vert in mVertexList) {
            glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 0.0f)
            // Draw Triangles
            GLES20.glDrawArrays(GL_TRIANGLE_STRIP, k * 4, 4)
            k++
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }
}