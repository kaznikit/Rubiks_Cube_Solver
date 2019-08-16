package com.example.kotlinapp.Rubik

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.kotlinapp.Rubik.Enums.Axis
import com.example.kotlinapp.Rubik.Enums.Color
import com.example.kotlinapp.Rubik.Enums.Direction
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.random.Random

class Cubie {
    var world : World

    private lateinit var mVertexBuffer: FloatBuffer

    internal var aPositionLocation = 0
    internal var uColorLocation = 0
    internal var uMatrixLocation = 0
    private val BYTES_PER_FLOAT = 4

    //matrix of tiles
    val tiles = arrayListOf<Tile>()

    var centerPoint : Vertex
    //main gl matrix
    var mTransformMatrix = FloatArray(16)

    //matrix with info about rotations
    var mAnimationMatrix = FloatArray(16)

    var id = 0

    var currentRotation = 0.0f
    var rotationAngle = 0.0f
    var rotationX = 0.0f
    var rotationY = 0.0f
    var rotationZ = 0.0f
    var isRotating = false
    var isRotated = false

    val mMatrix = FloatArray(16)
    val mMVPMatrix = FloatArray(16)

    var isCorner = false
    var isEdge = false

    constructor(minX : Float, minY : Float, minZ : Float, sideLength : Float, world : World, id :Int, isCorner:Boolean, isEdge:Boolean) {
        this.world = world
        this.id = id

        this.isCorner = isCorner
        this.isEdge = isEdge

        Matrix.setIdentityM(mTransformMatrix, 0)
        Matrix.setIdentityM(mAnimationMatrix, 0)

        val leftBottomBack = world.addVertex(Vertex(minX, minY, minZ))
        val rightBottomBack = world.addVertex(Vertex(minX + sideLength, minY, minZ))
        val leftTopBack = world.addVertex(Vertex(minX, minY + sideLength, minZ))
        val rightTopBack = world.addVertex(Vertex(minX + sideLength, minY + sideLength, minZ))
        val leftBottomFront = world.addVertex(Vertex(minX, minY, minZ + sideLength))
        val rightBottomFront = world.addVertex(Vertex(minX + sideLength, minY, minZ + sideLength))
        val leftTopFront = world.addVertex(Vertex(minX, minY + sideLength, minZ + sideLength))
        val rightTopFront = world.addVertex(Vertex(minX + sideLength, minY + sideLength, minZ + sideLength))

        // down tile
        tiles.add(
            Tile(
                arrayOf(leftBottomBack, leftBottomFront, rightBottomBack, rightBottomFront),
                Color.YELLOW,
                Direction.DOWN
            )
        )
        // front
        tiles.add(
            Tile(
                arrayOf(leftBottomFront, leftTopFront, rightBottomFront, rightTopFront),
                Color.GREEN,
                Direction.FRONT
            )
        )
        // left
        tiles.add(
            Tile(
                arrayOf(leftBottomBack, leftTopBack, leftBottomFront, leftTopFront),
                Color.ORANGE,
                Direction.LEFT
            )
        )
        // right
        tiles.add(
            Tile(
                arrayOf(rightBottomBack, rightBottomFront, rightTopBack, rightTopFront),
                Color.RED,
                Direction.RIGHT
            )
        )
        // back
        tiles.add(
            Tile(
                arrayOf(leftBottomBack, rightBottomBack, leftTopBack, rightTopBack),
                Color.BLUE,
                Direction.BACK
            )
        )
        // top
        tiles.add(
            Tile(
                arrayOf(leftTopBack, rightTopBack, leftTopFront, rightTopFront),
                Color.WHITE,
                Direction.UP
            )
        )

        centerPoint = Vertex(
            (rightTopFront.x + leftBottomBack.x) / 2f,
            (rightTopFront.y + leftBottomBack.y) / 2f,
            (rightTopFront.z + leftBottomBack.z) / 2f
        )

        //check if cubie is center
        if ((centerPoint.x == 0f && centerPoint.z == 0f)
            || (centerPoint.y == 0f && centerPoint.z == 0f)
            || (centerPoint.x == 0f && centerPoint.y == 0f)
        ) {
            this.isCorner = false
            this.isEdge = false
        }

        val byteBuf = ByteBuffer.allocateDirect(6 * 4 * 3 * BYTES_PER_FLOAT)
        byteBuf.order(ByteOrder.nativeOrder())
        mVertexBuffer = byteBuf.asFloatBuffer()
    }

    fun getDirOfColor(color: Char): Char {
        for (tile in tiles) {
            if (tile.color.charNotation == color)
                return tile.direction.charName
        }
        return 'A'
    }

    fun getColors() : ArrayList<Tile>{
        return tiles
    }

    fun verticalFace(x: Float, y: Float): Char {
        return if (isEdge) {
            if (x == -2.1f)
                'L'
            else if (x == 0f) {
                if (y == -2.1f) {
                    'F'
                } else
                    'B'
            } else
                'R'
        } else 'A'

    }

    fun endAnimation() {
        //stop the rotation
        isRotating = false

        //calculate new center point
        var mat = FloatArray(16)
        Matrix.setIdentityM(mat, 0)
        var arr = FloatArray(4)
        arr[0] = centerPoint.x
        arr[1] = centerPoint.y
        arr[2] = centerPoint.z
        arr[3] = 0.0f
        Matrix.rotateM(mat, 0, rotationAngle, -rotationX, -rotationY, -rotationZ)
        Matrix.multiplyMV(arr, 0, mat, 0, arr, 0)

        centerPoint.x = Vertex.RoundFloat(arr[0])
        centerPoint.y = Vertex.RoundFloat(arr[1])
        centerPoint.z = Vertex.RoundFloat(arr[2])

        for(tile in tiles){
            if(tile.isActive){
                var normalVec = Direction.getVectorByDirection(tile.direction)
                Matrix.multiplyMV(normalVec, 0, mat, 0, normalVec, 0)
                normalVec[0] = Vertex.RoundFloat(normalVec[0])
                normalVec[1] = Vertex.RoundFloat(normalVec[1])
                normalVec[2] = Vertex.RoundFloat(normalVec[2])

                tile.direction = Direction.getDirectionByVector(normalVec[0], normalVec[1], normalVec[2])
            }
        }

        isRotated = true
    }

    fun rotate(angle: Float, rotationAxis: Axis){
        currentRotation = 0.0f
        rotationAngle = angle

        var vec = Axis.getRotationVector(rotationAxis)
        rotationX = vec[0]
        rotationY = vec[1]
        rotationZ = vec[2]

        while (rotationAngle >= 360f) rotationAngle -= 360.0f
        isRotating = true
    }

    fun animateTransform() {
        if (isRotating) {
            if (Math.abs(currentRotation) >= Math.abs(rotationAngle)) {
                endAnimation()
                currentRotation = 0.0f
            } else {
                if(rotationAngle < 0){
                    currentRotation -= 5.0f
                    Matrix.rotateM(mAnimationMatrix, 0, -5.0f, rotationX, rotationY, rotationZ)
                }
                else{
                    currentRotation += 5.0f
                    Matrix.rotateM(mAnimationMatrix, 0, 5.0f, rotationX, rotationY, rotationZ)
                }
                Matrix.transposeM(mTransformMatrix, 0, mAnimationMatrix, 0)
            }
        }
    }

    fun deactiveTiles(direction: Direction){
        for(tile in tiles){
            if(!tile.isActive && tile.direction == direction){
                tile.isActive = true
            }
        }
    }

    fun resetVertexBuffer() {
        for (tile in tiles) {
            for (point in tile.coordinates) {
                mVertexBuffer.put(point.x)
                mVertexBuffer.put(point.y)
                mVertexBuffer.put(point.z)
            }
        }
    }


    fun draw(projectionMatrix: FloatArray, viewMatrix: FloatArray, programId: Int) {
        animateTransform()
        Matrix.multiplyMM(mMatrix, 0, viewMatrix, 0, mTransformMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMatrix, 0)
        GLES20.glUseProgram(programId)
        // примитивы
        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        mVertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glEnableVertexAttribArray(aPositionLocation)
        // цвет
        uColorLocation = GLES20.glGetUniformLocation(programId, "u_Color")
        // матрица
        this.uMatrixLocation = GLES20.glGetUniformLocation(programId, "u_Matrix")
        GLES20.glUniformMatrix4fv(this.uMatrixLocation, 1, false, mMVPMatrix, 0)
        var k = 0

        for (tile in tiles) {
            GLES20.glUniform4f(uColorLocation, tile.color.redComponent, tile.color.greenComponent, tile.color.blueComponent, 0.0f)
            // Draw Triangles
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, k * 4, 4)
            k++
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }
}