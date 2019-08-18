package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Rubik.Enums.Axis
import com.example.kotlinapp.Rubik.Enums.Direction

class Solver(var cube: Cube) {

    fun makeCube() {
        //cube.rotateCube(180f, Axis.zAxis)

            /*while(!cube.permutationAllowed){
            Thread.sleep(50)
        }
        cube.rotateCube(-180f, Axis.zAxis)*/

        var scramble = "F2 D' B U' D L2 B2 R B L'"
        cube.scramble(scramble)

        var sunflower = cube.makeWhiteCross()
        System.out.print(sunflower)


        var ss = cube.finishWhiteLayer()
        System.out.print(ss)

        var sss = cube.finishTwoLayers()
        System.out.print(sss)

    }
}