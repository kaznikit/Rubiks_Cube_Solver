package com.example.kotlinapp.Rubik

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Abstract.ICubie
import com.example.kotlinapp.Rubik.LogicSolving.LogicCubie
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.abs

class Cubie : ICubie{
    private var mVertexBuffer: FloatBuffer

    private var aPositionLocation = 0
    private var uColorLocation = 0
    private var uMatrixLocation = 0
    private val BYTES_PER_FLOAT = 4

    //matrix of tiles
    override val tiles = arrayListOf<Tile>()

    override var centerPoint : Vertex
    //main gl matrix
    var mTransformMatrix = FloatArray(16)

    //matrix with info about rotations
    var mAnimationMatrix = FloatArray(16)

    override var id = 0

    override var currentRotation = 0.0f
    override var rotationAngle = 0.0f
    override var rotationX = 0.0f
    override var rotationY = 0.0f
    override var rotationZ = 0.0f
    override var isRotating = false
    override var isRotated = false

    private val mMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)

    override var isCorner = false
    override var isEdge = false

    var isHighlighted = false

    /**
     * if this value true, all tiles in cubie are scanned
     * 3 tiles for corner cubie, 2 tiles for edge cubie and 1 for center
     */
    var areTileColorsFilled = false
    private var numberOfFilledTiles = 0

    constructor(minX : Float, minY : Float, minZ : Float, sideLength : Float, id :Int, isCorner:Boolean, isEdge:Boolean) {
        this.id = id

        this.isCorner = isCorner
        this.isEdge = isEdge

        Matrix.setIdentityM(mTransformMatrix, 0)
        Matrix.setIdentityM(mAnimationMatrix, 0)

        val leftBottomBack = Vertex(minX, minY, minZ)
        val rightBottomBack = Vertex(minX + sideLength, minY, minZ)
        val leftTopBack = Vertex(minX, minY + sideLength, minZ)
        val rightTopBack = Vertex(minX + sideLength, minY + sideLength, minZ)
        val leftBottomFront = Vertex(minX, minY, minZ + sideLength)
        val rightBottomFront = Vertex(minX + sideLength, minY, minZ + sideLength)
        val leftTopFront = Vertex(minX, minY + sideLength, minZ + sideLength)
        val rightTopFront = Vertex(minX + sideLength, minY + sideLength, minZ + sideLength)

        // down tile
        tiles.add(
            Tile(
                arrayOf(leftBottomBack, leftBottomFront, rightBottomBack, rightBottomFront),
                Color.GRAY,
                'D',
                Axis.yMinusAxis
            )
        )
        // front
        tiles.add(
            Tile(
                arrayOf(leftBottomFront, leftTopFront, rightBottomFront, rightTopFront),
                //Color.GREEN,
                Color.GRAY,
                'F',
                Axis.zAxis
            )
        )
        // left
        tiles.add(
            Tile(
                arrayOf(leftBottomBack, leftTopBack, leftBottomFront, leftTopFront),
                //Color.ORANGE,
                Color.GRAY,
                'L',
                Axis.xMinusAxis
            )
        )
        // right
        tiles.add(
            Tile(
                arrayOf(rightBottomBack, rightBottomFront, rightTopBack, rightTopFront),
                //Color.RED,
                Color.GRAY,
                'R',
                Axis.xAxis
            )
        )
        // back
        tiles.add(
            Tile(
                arrayOf(leftBottomBack, rightBottomBack, leftTopBack, rightTopBack),
                //Color.BLUE,
                Color.GRAY,
                'B',
                Axis.zMinusAxis
            )
        )
        // top
        tiles.add(
            Tile(
                arrayOf(leftTopBack, rightTopBack, leftTopFront, rightTopFront),
                //Color.WHITE,
                Color.GRAY,
                'U',
                Axis.yAxis
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

    companion object{
        fun CloneCubie(qb : Cubie) : LogicCubie{
            var cloneCubie = LogicCubie(Vertex.CloneVertex(qb.centerPoint), qb.id)
            var cloneTiles = ArrayList<Tile>()
            for(tile in qb.tiles){
                cloneTiles.add(Tile.CloneTile(tile))
            }
            cloneCubie.tiles = cloneTiles
            cloneCubie.isEdge = qb.isEdge
            cloneCubie.isCorner = qb.isCorner
            return cloneCubie
        }
    }

    /**
     * called when one of tiles is scanned
     * check while cubie is not filled
     */
    fun checkIfCubieFilled(){
        numberOfFilledTiles++
        if(isCorner){
            if(numberOfFilledTiles == 3){
                areTileColorsFilled = true
            }
        }else if(isEdge){
            if(numberOfFilledTiles == 2){
                areTileColorsFilled = true
            }
        }else{
            areTileColorsFilled = true
        }
    }

    /**
     * check if any tile has such color
     */
    fun checkIfColorExist(color : Color) : Boolean{
        for(tile in tiles){
            if(tile.color == color){
                return true
            }
        }
        return false
    }

    override fun rotate(angle: Float, rotationAxis: Axis) {
        currentRotation = 0.0f
        rotationAngle = angle

        var vec = Axis.getRotationVector(rotationAxis)
        rotationX = vec[0]
        rotationY = vec[1]
        rotationZ = vec[2]

        while (rotationAngle >= 360f) rotationAngle -= 360.0f
        isRotating = true
    }

    override fun endAnimation() {
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
                var normalVec = Axis.getRotationVector(tile.normalAxis)
                Matrix.multiplyMV(normalVec, 0, mat, 0, normalVec, 0)
                normalVec[0] = Vertex.RoundFloat(normalVec[0])
                normalVec[1] = Vertex.RoundFloat(normalVec[1])
                normalVec[2] = Vertex.RoundFloat(normalVec[2])

                tile.normalAxis = Axis.getAxis(normalVec[0], normalVec[1], normalVec[2])
                tile.direction = LayerEnum.getDirectionByVector(normalVec[0], normalVec[1], normalVec[2])
            }
        }
        isRotated = true
    }

    fun animateTransform() {
        if (isRotating) {
            if (abs(currentRotation) >= abs(rotationAngle)) {
                endAnimation()
                currentRotation = 0.0f
            } else {
                if(rotationAngle < 0){
                    currentRotation -= 10.0f
                    Matrix.rotateM(mAnimationMatrix, 0, -10.0f, rotationX, rotationY, rotationZ)
                }
                else{
                    currentRotation += 10.0f
                    Matrix.rotateM(mAnimationMatrix, 0, 10.0f, rotationX, rotationY, rotationZ)
                }
                Matrix.transposeM(mTransformMatrix, 0, mAnimationMatrix, 0)
            }
        }
    }

    override fun deactivateTiles(direction: Direction){
        for(tile in tiles){
            if(!tile.isActive && tile.direction == direction.charName){
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
            GLES20.glUniform4f(uColorLocation, tile.color.redComponent, tile.color.greenComponent, tile.color.blueComponent, 1.0f)
            // Draw Triangles
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, k * 4, 4)
            k++
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(aPositionLocation)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }

    val pulseTimePeriod: Long = 3000
    private var pulseTime: Long = 0

    fun update(delta: Long) {
        pulseTime += delta
        while (pulseTime > pulseTimePeriod)
            pulseTime -= pulseTimePeriod

        val ratio = pulseTime / java.lang.Float.valueOf(pulseTimePeriod.toFloat())
        val `val` = (Math.cos(ratio.toDouble() * 3.14 * 2.0) * 0.5 + 0.5).toFloat()
        val coefficient = Math.pow(`val`.toDouble(), 2.0).toFloat() //power makes it more pulsey
    }
}