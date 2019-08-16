package com.example.kotlinapp.Rubik

class Solver(var cube: Cube) {

    fun makeCube() {
        var scramble = "F2 D' B U' D L2 B2 R B L' B2 L2 B2 D' R2 F2 D' R2 U'"
        cube.scramble(scramble);

        var sunflower = cube.makeWhiteCross()//.makeSunflower()
        System.out.print(sunflower)
    }
}