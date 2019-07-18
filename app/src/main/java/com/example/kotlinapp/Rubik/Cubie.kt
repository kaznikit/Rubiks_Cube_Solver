package com.example.kotlinapp.Rubik

class Cubie {
    var world : World

    //matrix of tiles
    val tiles = arrayListOf<Tile>()

    constructor(minX : Float, minY : Float, minZ : Float, sideLength : Float, world : World) {
        this.world = world

        val leftBottomBack = world.addVertex(Vertex(minX, minY, minZ))
        val rightBottomBack = world.addVertex(Vertex(minX + sideLength, minY, minZ))
        val leftTopBack = world.addVertex(Vertex(minX, minY + sideLength, minZ))
        val rightTopBack = world.addVertex(Vertex(minX + sideLength, minY + sideLength, minZ))
        val leftBottomFront = world.addVertex(Vertex(minX, minY, minZ + sideLength))
        val rightBottomFront = world.addVertex(Vertex(minX + sideLength, minY, minZ + sideLength))
        val leftTopFront = world.addVertex(Vertex(minX, minY + sideLength, minZ + sideLength))
        val rightTopFront = world.addVertex(Vertex(minX + sideLength, minY + sideLength, minZ + sideLength))

        //down tile
        tiles.add(Tile(arrayOf(leftBottomBack, leftBottomFront, rightBottomBack, rightBottomFront)))
        // front
        tiles.add(Tile(arrayOf(leftBottomFront, leftTopFront, rightBottomFront, rightTopFront)))
        // left
        tiles.add(Tile(arrayOf(leftBottomBack, leftTopBack, leftBottomFront, leftTopFront)))
        // right
        tiles.add(Tile(arrayOf(rightBottomBack, rightBottomFront, rightTopBack, rightTopFront)))
        // back
        tiles.add(Tile(arrayOf(leftBottomBack, rightBottomBack, leftTopBack, rightTopBack)))
        // top
        tiles.add(Tile(arrayOf(leftTopBack, rightTopBack, leftTopFront, rightTopFront)))
    }
}