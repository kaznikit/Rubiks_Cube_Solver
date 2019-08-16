package com.example.kotlinapp.Rubik.Enums

//describe each layer location on the cube
enum class LayerEnum(val rotationAxis: Axis, val centerPoint : Float, val direction: Direction) {

    LEFT(Axis.xMinusAxis, -2.1f, Direction.LEFT),
    MIDDLE(Axis.xAxis, 0f, Direction.NON),
    RIGHT(Axis.xAxis, 2.1f, Direction.RIGHT),
    BACK(Axis.zMinusAxis, -2.1f, Direction.BACK),
    STANDING(Axis.zAxis, 0f, Direction.NON),
    FRONT(Axis.zAxis, 2.1f, Direction.FRONT),
    DOWN(Axis.yMinusAxis, -2.1f, Direction.DOWN),
    EQUATOR(Axis.yAxis, 0f, Direction.NON),
    UP(Axis.yAxis, 2.1f, Direction.UP);

    companion object {
        fun getLayerNameByCenterPoint(rotationAxis: Axis, centerPoint: Float): LayerEnum? {
            when(rotationAxis) {
                Axis.xMinusAxis -> {
                    return LEFT
                }
                Axis.xAxis -> {
                    if(centerPoint == 0f) {
                        return MIDDLE
                    }
                    return RIGHT
                }
                Axis.yAxis -> {
                    if(centerPoint == 0f){
                        return EQUATOR
                    }
                    return UP
                }
                Axis.yMinusAxis -> {
                    return DOWN
                }
                Axis.zAxis -> {
                    if (centerPoint == 0f) {
                        return STANDING
                    }
                    return FRONT
                }
                Axis.zMinusAxis -> {
                    return BACK
                }
            }
            return null
        }
    }
}