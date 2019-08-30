package com.example.kotlinapp.Rubik

class Solver(var cube: Cube) {

    fun makeCube() {
        /*var scramble = "F2 D' B U' D L2 B2 R B L'"
        cube.scramble(scramble)*/

        var sunflower = cube.makeWhiteCross()
        System.out.print(sunflower)

        var ss = cube.finishWhiteLayer()
        System.out.print(ss)

        var sss = cube.finishTwoLayers()
        System.out.print(sss)

        var ssss = cube.makeYellowCross()
        System.out.print(ssss)

        var sssss = cube.swapYellowEdgesTopLayer()
        System.out.print(sssss)

        cube.findRightOrientedYellowCubie()

        var ssssss = cube.finishSolvingYellowCorners()
        System.out.print(ssssss)
    }
}