package com.example.kotlinapp.Recognition

import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Tile
import com.example.kotlinapp.MainActivity



class RubikFace {
    // A Rubik Face can exist in the following states:
    enum class FaceRecognitionStatusEnum {
        UNKNOWN,
        INSUFFICIENT, // Insufficient Provided Rhombi to attempt solution
        BAD_METRICS, // Metric Calculation did not produce reasonable results
        INCOMPLETE, // Rhombi did not converge to proper solution
        INADEQUATE, // We require at least one Rhombus in each row and column
        BLOCKED, // Attempt to improve Rhombi layout in face was blocked: incorrect move direction reported
        INVALID_MATH, // LMS algorithm result in invalid math.
        UNSTABLE, // Last Tile move resulted in a increase in the overall error (LMS).
        SOLVED                   // Full and proper solution obtained.
    }

    lateinit var rotationAxis: Axis

    var ColorDetectionCount = 0
    var averageColorArray =
        Array(15) { arrayOfNulls<Color>(9) }

    var faceRecognitionStatus = FaceRecognitionStatusEnum.UNKNOWN

    // A 3x3 matrix of Logical Tiles.  All elements must be non-null for an appropriate Face solution.
    // The rotation of this array is the output of the Face Recognizer as per the current spatial
    // rotation of the cube.
    var observedTileArray = Array(3) { arrayOfNulls<Tile>(3) }

    // A 3x3 matrix of rectangles elements. This array will be sorted to achieve
    // final correct position arrangement of available Rhombus objects.  Some elements can be null.
    @Transient
    var faceRectanglesArray = Array(3) { arrayOfNulls<Tile>(3) }

    // A 3x3 matrix of Logical Tiles.  All elements must be non-null for an appropriate Face solution.
    // The rotation of this array has been adjusted so that, in the final cube state, the faces are read
    // and rendered correctly with respect to the "unfolded cube layout convention."
    var transformedTileArray = Array(3) { arrayOfNulls<Tile>(3) }

    //temporary list with tile colors
    var tempTileList = Array(3) { arrayOfNulls<Tile>(3) }

    // public List<Rectangle> rectanglesList = new LinkedList<Rectangle>();

    // Record actual RGB colors measured at the center of each tile.
    var measuredColorArray = Array(3) { Array(3) { DoubleArray(4) } }

    // Least Means Square Result
    @Transient
    /*var lmsResult = LeastMeansSquare(
        800, // X origin of Rubik Face (i.e. center of tile {0,0})
        200, // Y origin of Rubik Face (i.e. center of tile {0,0})
        50, // Length of Alpha Lattice
        null,
        314, // Sigma Error (i.e. RMS of know Rhombus to Tile centers)
        true
    )// Put some dummy data here.
    // Allow these dummy results to be display even though they are false*/

    // Angle of Alpha-Axis (N) stored in radians.
    var alphaAngle = 0.0

    // Angle of Beta-Axis (M) stored in radians.
    var betaAngle = 0.0

    // Length in pixels of Alpha Lattice (i.e. a tile size)
    var alphaLatticLength = 0.0

    // Length in pixels of Beta Lattice (i.e. a tile size)
    var betaLatticLength = 0.0

    // Ratio of Beta Lattice to Alpha Lattice
    var gammaRatio = 0.0

    // Number of rhombus that were moved in order to obtain better LMS fit.
    var numRhombusMoves = 0

    // Face Designation: i.e., Up, Down, ....
    lateinit var faceNameEnum: LayerEnum

    // Angle of Alpha-Axis (N) stored in radians.
    var angle = 0.0

    // This is a proprietary hash code and NOT that of function hashCode().  This hash code is
    // intended to be unique and repeatable for any given set of colored tiles in a specified set
    // of locations on a Rubik Face.  It is used to determine if an identical Rubik Face is being
    // observed multiple times. Note, if a tiles color designation is changed due to a change in
    // lighting conditions, the calculated hash code will be different.  A more robust strategy
    // would be to require that only 8 or 9 tiles match in order to determine if an
    // identical face is being presented.
    var myHashCode = 0
    var isObservedFilled = false

    /*fun createObservedTilesArray() {
        for (i in 0..2) {
            for (j in 0..2) {
                observedTileArray[i][j] =
                    Tile(MainActivity.currentState.activeRubikFace!!.faceNameEnum, i, j)
                transformedTileArray[i][j] =
                    Tile(MainActivity.currentState.activeRubikFace!!.faceNameEnum, i, j)
                tempTileList[i][j] =
                    Tile(MainActivity.currentState.activeRubikFace!!.faceNameEnum, i, j)
                isObservedFilled = true
            }
        }
    }*/
}