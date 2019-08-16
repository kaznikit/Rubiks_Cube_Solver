package com.example.kotlinapp.Rubik

import android.opengl.GLES20
import android.opengl.GLES20.*
import android.provider.SyncStateContract
import com.example.kotlinapp.Util.Constants
import java.nio.*
import java.util.ArrayList

class World {
    private val mVertexList = ArrayList<Vertex>()

    private val BYTES_PER_FLOAT = 4

    private lateinit var mVertexBuffer: FloatBuffer

    constructor() {
        val byteBuf = ByteBuffer.allocateDirect(6 * 9 * 4 * 3 * BYTES_PER_FLOAT)
        byteBuf.order(ByteOrder.nativeOrder())
        mVertexBuffer = byteBuf.asFloatBuffer()
    }

    fun addVertex(vertex : Vertex) : Vertex {
        val vert = mVertexList.find { v: Vertex -> v.isEqual(v, vertex) }
        if(vert != null){
            return vert
        }
        mVertexList.add(vertex)
        return vertex
    }
}