package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Enums.SolvingPhaseEnum
import com.example.kotlinapp.Recognition.RubikTile
import com.example.kotlinapp.Rubik.LogicSolving.LogicCube
import com.example.kotlinapp.Rubik.LogicSolving.LogicCubie
import com.example.kotlinapp.Rubik.LogicSolving.LogicLayer
import kotlin.math.log

//working with logic elements for solving
class Solver {
    var logicCube : LogicCube = LogicCube()

    var solvingPhase = SolvingPhaseEnum.WhiteCross

    private var stateChangedTime : Long = 0

    fun copyCube(cube: Cube){
        for(qb in cube.cubies){
            logicCube.cubies.add(Cubie.CloneCubie(qb))
        }

        for(layer in cube.layers){
            logicCube.layers.add(Layer.CloneLayer(layer, logicCube))
        }

        logicCube.directionsControl = DirectionsControl.CloneDirectionsControl(cube.directionsControl)
    }

    fun solveNextPhase() : ArrayList<String>{
        phaseDownLayerArray.clear()
        var solution = ArrayList<String>()
        when(solvingPhase){
            SolvingPhaseEnum.WhiteCross -> {
                solution = logicCube.makeWhiteCross()
                solvingPhase = SolvingPhaseEnum.WhiteLayer
            }
            SolvingPhaseEnum.WhiteLayer -> {
                solution = logicCube.finishWhiteLayer()
                solvingPhase = SolvingPhaseEnum.TwoLayers
            }
            SolvingPhaseEnum.TwoLayers -> {
                solution = logicCube.finishTwoLayers()
                solvingPhase = SolvingPhaseEnum.YellowCross
            }
            SolvingPhaseEnum.YellowCross -> {
                solution = logicCube.makeYellowCross()
                solvingPhase = SolvingPhaseEnum.YellowEdges
            }
            SolvingPhaseEnum.YellowEdges -> {
                solution = logicCube.swapYellowEdgesTopLayer()
                solvingPhase = SolvingPhaseEnum.YellowCornersOrient
            }
            SolvingPhaseEnum.YellowCornersOrient -> {
                solution = logicCube.findRightOrientedYellowCubie()
                solvingPhase = SolvingPhaseEnum.YellowCorners
            }
            SolvingPhaseEnum.YellowCorners -> {
                solution = logicCube.finishSolvingYellowCorners()
                solvingPhase = SolvingPhaseEnum.Finish
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

    /**
     * Compare obtained tiles with down layer
     */
    fun cubeSideRightRotated(faceColors : Array<Array<RubikTile?>>?, elementNumber : Int) : Boolean{
        if(phaseDownLayerArray.size != 0) {
            var currentLayerState = phaseDownLayerArray[elementNumber]

            //if previous state equals new state, rotate arrow 3 seconds before rotation
            if(stateChangedTime.toInt() != 0){
                if(elementNumber != 0) {
                    if (checkIfPreviousPhaseStateTheSame(elementNumber - 1)) {
                        if (System.currentTimeMillis() - stateChangedTime < 3000) {
                            return false
                        }
                    }
                }
            }

            if (faceColors != null) {
                var k = 0
                for (tiles in faceColors) {
                    for (tile in tiles) {
                        var currentCubieTile = currentLayerState?.get(k)?.tiles?.single { x -> x.isActive
                                && x.direction == 'D' }

                        //check if cubie has tile color
                        if (currentCubieTile?.color != tile?.tileColor ) {
                            return false
                        }
                        k++
                    }
                }
            }
        }
        stateChangedTime = System.currentTimeMillis()
        return true
    }

    /**
     * Check if two states are the same
     */
    private fun checkIfPreviousPhaseStateTheSame(index : Int) : Boolean{
        if(index + 1 < phaseDownLayerArray.size) {
            var currentStateTiles = phaseDownLayerArray.get(index)
            var nextStateTiles = phaseDownLayerArray.get(index + 1)

            if (currentStateTiles != null && nextStateTiles != null) {
                var k = 0
                for (currQb in currentStateTiles) {
                    if (!currQb.isEqual(nextStateTiles[k])) {
                        return false
                    }
                    k++
                }
                return true
            }
        }
        return false
    }

    /**
     * If current state is equal previous state
     */
    fun checkIfUserRotatedSideBackward(index : Int, faceColors : Array<Array<RubikTile?>>?) : Boolean {
        if(index >= 2) {
            var previousStateTiles = phaseDownLayerArray[index - 1]

            if(checkIfPreviousPhaseStateTheSame(index - 1)){
                return false
            }

            if (faceColors != null) {
                var k = 0
                for (tiles in faceColors) {
                    for (tile in tiles) {
                        var previousCubie = previousStateTiles?.get(k)

                        //check if cubie has tile color
                        if (!previousCubie?.tiles?.any { x -> x.isActive && x.direction == 'D'
                                    && x.color == tile?.tileColor }!!) {
                            return false
                        }
                        k++
                    }
                }
                return true
            }
        }
        return false
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