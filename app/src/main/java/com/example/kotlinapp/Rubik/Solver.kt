package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Enums.SolvingPhaseEnum
import com.example.kotlinapp.Rubik.LogicSolving.LogicCube
import com.example.kotlinapp.Rubik.LogicSolving.LogicCubie
import com.example.kotlinapp.Rubik.LogicSolving.LogicLayer
import kotlin.math.log

//working with logic elements for solving
class Solver {
    var logicCube : LogicCube = LogicCube()

    var solvingPhase = SolvingPhaseEnum.WhiteCross

    fun copyCube(cube: Cube){
        for(qb in cube.cubies){
            logicCube.cubies.add(Cubie.CloneCubie(qb))
        }

        for(layer in cube.layers){
            logicCube.layers.add(Layer.CloneLayer(layer, logicCube))
        }

        logicCube.directionsControl = DirectionsControl.CloneDirectionsControl(cube.directionsControl)
    }

    fun solveNextPhase() : String{
        phaseDownLayerArray.clear()
        var solution = ""
        when(solvingPhase){
            SolvingPhaseEnum.WhiteCross -> {
                solution = logicCube.makeWhiteCross()
            }
            SolvingPhaseEnum.WhiteLayer -> {
                solution = logicCube.finishWhiteLayer()
            }
            SolvingPhaseEnum.TwoLayers -> {
                solution = logicCube.finishTwoLayers()
            }
            SolvingPhaseEnum.YellowCross -> {
                solution = logicCube.makeYellowCross()
            }
            SolvingPhaseEnum.YellowEdges -> {
                solution = logicCube.swapYellowEdgesTopLayer()
            }
            SolvingPhaseEnum.YellowCornersOrient -> {
                solution = logicCube.findRightOrientedYellowCubie()
            }
            SolvingPhaseEnum.YellowCorners -> {
                solution = logicCube.finishSolvingYellowCorners()
            }
        }
        return solution
    }

    companion object {
        //store cube state after each rotation
        var phaseDownLayerArray = ArrayList<List<LogicCubie>?>()

        fun addPhaseLayer(cubies : List<LogicCubie>?) {
            phaseDownLayerArray.add(cubies)
        }
    }



/*
    fun makeCube() {
        */
/*var scramble = "F2 D' B U' D L2 B2 R B L'"
        cube.scramble(scramble)*//*


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
*/
}