package com.example.kotlinapp.Enums

enum class SolvingPhaseEnum(val phaseName : String) {
    WhiteCross("White cross"),
    WhiteLayer("White layer"),
    TwoLayers("Two layers"),
    YellowCross("Yellow cross"),
    YellowEdges("Yellow edges"),
    YellowCornersOrient("Position N yellow corners"),
    YellowCorners("Solving N yellow corners"),
    Finish("Finish")
}