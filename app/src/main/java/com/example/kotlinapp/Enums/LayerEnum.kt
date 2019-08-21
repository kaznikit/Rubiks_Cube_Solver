package com.example.kotlinapp.Enums

//describe each layer location on the cube
enum class LayerEnum(val charName : Char, var rotationAxis: Axis, val centerPoint : Float) {

    LEFT('L', Axis.xMinusAxis, -2.1f),
    MIDDLE('M', Axis.xAxis, 0f),
    RIGHT('R', Axis.xAxis, 2.1f),
    BACK('B', Axis.zMinusAxis, -2.1f),
    STANDING('S', Axis.zAxis, 0f),
    FRONT('F', Axis.zAxis, 2.1f),
    DOWN('D', Axis.yMinusAxis, -2.1f),
    EQUATOR('E', Axis.yAxis, 0f),
    UP('U', Axis.yAxis, 2.1f);

    companion object {
        fun getRotationAxisByLayerName(layerEnum: LayerEnum) : Axis {
            when (layerEnum) {
                LEFT -> {
                    return LEFT.rotationAxis
                }
                MIDDLE -> {
                    return MIDDLE.rotationAxis
                }
                RIGHT -> {
                    return RIGHT.rotationAxis
                }
                BACK -> {
                    return BACK.rotationAxis
                }
                STANDING -> {
                    return STANDING.rotationAxis
                }
                FRONT -> {
                    return FRONT.rotationAxis
                }
                DOWN -> {
                    return DOWN.rotationAxis
                }
                EQUATOR -> {
                    return EQUATOR.rotationAxis
                }
                UP -> {
                    return UP.rotationAxis
                }
            }
        }

        fun getVectorByDirection(direction : Char) : FloatArray{
            when(direction){
                'L' -> {
                    return Axis.getRotationVector(Axis.xMinusAxis)
                }
                'R' -> {
                    return Axis.getRotationVector(Axis.xAxis)
                }
                'B' -> {
                    return Axis.getRotationVector(Axis.zMinusAxis)
                }
                'F' -> {
                    return Axis.getRotationVector(Axis.zAxis)
                }
                'D' -> {
                    return Axis.getRotationVector(Axis.yMinusAxis)
                }
                'U' -> {
                    return Axis.getRotationVector(Axis.yAxis)
                }
            }
            return Axis.getRotationVector(Axis.zAxis)
        }

        fun getDirectionByVector(x : Float, y : Float, z : Float) : Char{
            var ax = Axis.getAxis(x, y, z)
            when(ax){
                Axis.xAxis -> {
                    return 'R'
                }
                Axis.xMinusAxis -> {
                    return 'L'
                }
                Axis.zMinusAxis -> {
                    return 'B'
                }
                Axis.zAxis -> {
                    return 'F'
                }
                Axis.yAxis -> {
                    return 'U'
                }
                Axis.yMinusAxis -> {
                    return 'D'
                }
            }
        }
    }
}