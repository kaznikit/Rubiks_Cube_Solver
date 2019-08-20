package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Rubik.Enums.Axis
import com.example.kotlinapp.Rubik.Enums.Color
import com.example.kotlinapp.Rubik.Enums.Direction

class DirectionsControl() {
    val directions = arrayListOf<Direction>()

    init {
        createDirections()
    }

    fun createDirections(){
        directions.add(Direction('L', Color.ORANGE))
        directions.add(Direction('R', Color.RED))
        directions.add(Direction('U', Color.WHITE))
        directions.add(Direction('D', Color.YELLOW))
        directions.add(Direction('B', Color.BLUE))
        directions.add(Direction('F', Color.GREEN))
        directions.add(Direction('N', Color.BLACK))
    }

    fun getDirectionByCharName(charName : Char) : Direction{
        return directions.filter { x -> x.charName == charName }.single()
    }

    fun getColorByDirection(charName : Char) : Color{
        for(direction in directions){
            if(charName == direction.charName){
                return direction.color
            }
        }
        return Color.BLACK
    }

    fun getDirectionByColor(color: Color) : Char{
        for(direction in directions){
            if(direction.color == color){
                return direction.charName
            }
        }
        return 'N'
    }

    fun updateDirectionsAfterRotation(degrees : Float, axis: Axis) {
        if(degrees == 180f) {
            if (axis == Axis.zAxis || axis == Axis.zMinusAxis) {
                for (direction in directions) {
                    if (direction.charName == 'L') {
                        if (direction.color == Color.ORANGE) {
                            direction.changeColor(Color.RED)
                        } else {
                            direction.changeColor(Color.ORANGE)
                        }
                    } else if (direction.charName == 'R') {
                        if (direction.color == Color.RED) {
                            direction.changeColor(Color.ORANGE)
                        } else {
                            direction.changeColor(Color.RED)
                        }
                    } else if (direction.charName == 'U') {
                        if (direction.color == Color.WHITE) {
                            direction.changeColor(Color.YELLOW)
                        } else {
                            direction.changeColor(Color.WHITE)
                        }
                    } else if (direction.charName == 'D') {
                        if (direction.color == Color.YELLOW) {
                            direction.changeColor(Color.WHITE)
                        } else {
                            direction.changeColor(Color.YELLOW)
                        }
                    }
                }
            }
            if (axis == Axis.yAxis || axis == Axis.yMinusAxis) {
                for (direction in directions) {
                    if (direction.charName == 'L') {
                        if (direction.color == Color.ORANGE) {
                            direction.changeColor(Color.RED)
                        } else {
                            direction.changeColor(Color.ORANGE)
                        }
                    } else if (direction.charName == 'R') {
                        if (direction.color == Color.RED) {
                            direction.changeColor(Color.ORANGE)
                        } else {
                            direction.changeColor(Color.RED)
                        }
                    } else if (direction.charName == 'B') {
                        if (direction.color == Color.BLUE) {
                            direction.changeColor(Color.GREEN)
                        } else {
                            direction.changeColor(Color.BLUE)
                        }
                    } else if (direction.charName == 'F') {
                        if (direction.color == Color.GREEN) {
                            direction.changeColor(Color.BLUE)
                        } else {
                            direction.changeColor(Color.GREEN)
                        }
                    }
                }
            }
        }
        else if(degrees == 90f){
            if(axis == Axis.yAxis) {
                //when yellow is down
                if (directions.filter { x -> x.color == Color.YELLOW }.single().charName == 'D') {
                    for (direction in directions) {
                        if (direction.charName == 'L') {
                            if (direction.color == Color.ORANGE) {
                                direction.changeColor(Color.GREEN)
                            } else if (direction.color == Color.GREEN) {
                                direction.changeColor(Color.RED)
                            }else if(direction.color == Color.RED){
                                direction.changeColor(Color.BLUE)
                            }else{
                                direction.changeColor(Color.ORANGE)
                            }
                        } else if (direction.charName == 'R') {
                            if (direction.color == Color.RED) {
                                direction.changeColor(Color.BLUE)
                            } else if(direction.color == Color.BLUE){
                                direction.changeColor(Color.ORANGE)
                            }else if(direction.color == Color.ORANGE){
                                direction.changeColor(Color.GREEN)
                            }else{
                                direction.changeColor(Color.RED)
                            }
                        } else if (direction.charName == 'B') {
                            if (direction.color == Color.BLUE) {
                                direction.changeColor(Color.ORANGE)
                            } else if(direction.color == Color.ORANGE){
                                direction.changeColor(Color.GREEN)
                            } else if(direction.color == Color.GREEN){
                                direction.changeColor(Color.RED)
                            } else {
                                direction.changeColor(Color.BLUE)
                            }
                        } else if (direction.charName == 'F') {
                            if (direction.color == Color.GREEN) {
                                direction.changeColor(Color.RED)
                            } else if(direction.color == Color.RED){
                                direction.changeColor(Color.BLUE)
                            } else if(direction.color == Color.BLUE){
                                direction.changeColor(Color.ORANGE)
                            } else{
                                direction.changeColor(Color.GREEN)
                            }
                        }
                    }
                }
                //yellow is up
                else {
                    for (direction in directions) {
                        if (direction.charName == 'L') {
                            if (direction.color == Color.ORANGE) {
                                direction.changeColor(Color.BLUE)
                            } else if (direction.color == Color.BLUE) {
                                direction.changeColor(Color.RED)
                            }else if(direction.color == Color.RED){
                                direction.changeColor(Color.GREEN)
                            }else{
                                direction.changeColor(Color.ORANGE)
                            }
                        } else if (direction.charName == 'R') {
                            if (direction.color == Color.RED) {
                                direction.changeColor(Color.GREEN)
                            } else if(direction.color == Color.GREEN){
                                direction.changeColor(Color.ORANGE)
                            }else if(direction.color == Color.ORANGE){
                                direction.changeColor(Color.BLUE)
                            }else{
                                direction.changeColor(Color.RED)
                            }
                        } else if (direction.charName == 'B') {
                            if (direction.color == Color.BLUE) {
                                direction.changeColor(Color.RED)
                            } else if(direction.color == Color.RED){
                                direction.changeColor(Color.GREEN)
                            } else if(direction.color == Color.GREEN){
                                direction.changeColor(Color.ORANGE)
                            } else {
                                direction.changeColor(Color.BLUE)
                            }
                        } else if (direction.charName == 'F') {
                            if (direction.color == Color.GREEN) {
                                direction.changeColor(Color.ORANGE)
                            } else if(direction.color == Color.ORANGE){
                                direction.changeColor(Color.BLUE)
                            } else if(direction.color == Color.BLUE){
                                direction.changeColor(Color.RED)
                            } else{
                                direction.changeColor(Color.GREEN)
                            }
                        }
                    }
                }
            }
        }
        else if(degrees == -90f){
            if(axis == Axis.yAxis) {
                //when yellow is down
                if (directions.filter { x -> x.color == Color.YELLOW }.single().charName == 'D') {
                    for (direction in directions) {
                        if (direction.charName == 'L') {
                            if (direction.color == Color.ORANGE) {
                                direction.changeColor(Color.BLUE)
                            } else if (direction.color == Color.BLUE) {
                                direction.changeColor(Color.RED)
                            }else if(direction.color == Color.RED){
                                direction.changeColor(Color.GREEN)
                            }else{
                                direction.changeColor(Color.ORANGE)
                            }
                        } else if (direction.charName == 'R') {
                            if (direction.color == Color.RED) {
                                direction.changeColor(Color.GREEN)
                            } else if(direction.color == Color.GREEN){
                                direction.changeColor(Color.ORANGE)
                            }else if(direction.color == Color.ORANGE){
                                direction.changeColor(Color.BLUE)
                            }else{
                                direction.changeColor(Color.RED)
                            }
                        } else if (direction.charName == 'B') {
                            if (direction.color == Color.BLUE) {
                                direction.changeColor(Color.RED)
                            } else if(direction.color == Color.RED){
                                direction.changeColor(Color.GREEN)
                            } else if(direction.color == Color.GREEN){
                                direction.changeColor(Color.ORANGE)
                            } else {
                                direction.changeColor(Color.BLUE)
                            }
                        } else if (direction.charName == 'F') {
                            if (direction.color == Color.GREEN) {
                                direction.changeColor(Color.ORANGE)
                            } else if(direction.color == Color.ORANGE){
                                direction.changeColor(Color.BLUE)
                            } else if(direction.color == Color.BLUE){
                                direction.changeColor(Color.RED)
                            } else{
                                direction.changeColor(Color.GREEN)
                            }
                        }
                    }
                }
                //yellow is up
                else {
                    for (direction in directions) {
                        if (direction.charName == 'L') {
                            if (direction.color == Color.ORANGE) {
                                direction.changeColor(Color.GREEN)
                            } else if (direction.color == Color.GREEN) {
                                direction.changeColor(Color.RED)
                            }else if(direction.color == Color.RED){
                                direction.changeColor(Color.BLUE)
                            }else{
                                direction.changeColor(Color.ORANGE)
                            }
                        } else if (direction.charName == 'R') {
                            if (direction.color == Color.RED) {
                                direction.changeColor(Color.BLUE)
                            } else if(direction.color == Color.BLUE){
                                direction.changeColor(Color.ORANGE)
                            }else if(direction.color == Color.ORANGE){
                                direction.changeColor(Color.GREEN)
                            }else{
                                direction.changeColor(Color.RED)
                            }
                        } else if (direction.charName == 'B') {
                            if (direction.color == Color.BLUE) {
                                direction.changeColor(Color.ORANGE)
                            } else if(direction.color == Color.ORANGE){
                                direction.changeColor(Color.GREEN)
                            } else if(direction.color == Color.GREEN){
                                direction.changeColor(Color.RED)
                            } else {
                                direction.changeColor(Color.BLUE)
                            }
                        } else if (direction.charName == 'F') {
                            if (direction.color == Color.GREEN) {
                                direction.changeColor(Color.RED)
                            } else if(direction.color == Color.RED){
                                direction.changeColor(Color.BLUE)
                            } else if(direction.color == Color.BLUE){
                                direction.changeColor(Color.ORANGE)
                            } else{
                                direction.changeColor(Color.GREEN)
                            }
                        }
                    }
                }
            }
        }
    }

}