package com.example.kotlinapp.Rubik.Enums

enum class Direction(val charName : Char) {
    LEFT('L'),
    RIGHT('R'),
    UP('U'),
    DOWN('D'),
    BACK('B'),
    FRONT('F'),
    NON('N');

    companion object {
        fun getDirectionByVector(x:Float, y:Float, z:Float) : Direction{
            if(x == 1f && y == 0f && z == 0f){
                return RIGHT
            }
            if(x == -1f && y == 0f && z == 0f){
                return LEFT
            }
            if(x == 0f && y == 1f && z == 0f){
                return UP
            }
            if(x == 0f && y == -1f && z == 0f){
                return DOWN
            }
            if(x == 0f && y == 0f && z == 1f){
                return FRONT
            }
            if(x == 0f && y == 0f && z == -1f){
                return BACK
            }
            return NON
        }

        fun getVectorByDirection(direction: Direction) : FloatArray{
            var vec = FloatArray(4)
            vec[0] = 0f
            vec[1] = 0f
            vec[2] = 0f
            vec[3] = 0f
            when(direction){
                FRONT->{
                    vec[2] = 1f
                }
                BACK->{
                    vec[2] = -1f
                }
                RIGHT->{
                    vec[0] = 1f
                }
                LEFT->{
                    vec[0] = -1f
                }
                UP->{
                    vec[1] = 1f
                }
                DOWN->{
                    vec[1] = -1f
                }
            }
            return vec
        }
    }
}