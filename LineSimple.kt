
data class PointD(val x: Double, val y: Double)

object LineSimple {
    private fun perpendicularDistance(
        pt: PointD,
        lineStart: PointD,
        lineEnd: PointD): Double {

        val dx0 = lineEnd.x - lineStart.x
        val dy0 = lineEnd.y - lineStart.y

        val mag: Double = Math.hypot(dx0, dy0)
        val dx = if (mag > 0f) { dx0 / mag } else { dx0 }
        val dy = if (mag > 0f) { dy0 / mag } else { dy0 }

        val pvx = pt.x - lineStart.x
        val pvy = pt.y - lineStart.y

        val pvdot = dx * pvx + dy * pvy
 
        val ax = pvx - pvdot * dx
        val ay = pvy - pvdot * dy
 
        return Math.hypot(ax, ay)
    }

    private fun ramerDouglasPeucker(
        pointList: List<PointD>,
        epsilon: Double,
        outputPointList: MutableList<PointD>){

        val pointListSize = pointList.size
        if (pointListSize < 2){
            outputPointList.addAll( pointList )
        } else {
            val firstPoint = pointList.first()
            val lastPoint  = pointList.last()

            val dList     = 1.until(pointListSize).map {
                perpendicularDistance(pointList[it], firstPoint, lastPoint)
            } 
            val indexList = 1.until(pointListSize).map { it }
    
            val result: Pair<Double, Int> = dList.zip(indexList).fold(Pair<Double,Int>(0.toDouble(), 0)) { acc, next->
                val dmax = acc.first
                val d    = next.first
                if( d > dmax ){
                    val index = next.second
                    Pair<Double,Int>(d, index)
                } else {
                    acc
                }
            }
    
            val dmax = result.first
            val index = result.second

            if ( (dmax > epsilon) && ((index+1) < pointListSize)) {
                val recResults1 = mutableListOf<PointD>()
                val recResults2 = mutableListOf<PointD>()
    
                val firstLine = pointList.subList(0, index + 1)
                val lastLine  = pointList.subList(index, pointListSize)
    
                   ramerDouglasPeucker(firstLine, epsilon, recResults1)
                   ramerDouglasPeucker(lastLine,  epsilon, recResults2)
    
                outputPointList.addAll(recResults1.subList(0, recResults1.size - 1))
                outputPointList.addAll(recResults2)
                if (outputPointList.size < 2){
                    outputPointList.addAll( pointList )
                }
            } else {
                outputPointList.clear()
                outputPointList.add(pointList.first())
                outputPointList.add(pointList.last())
            }
        }
    }

    fun convertPts(pts: List<Float>, epsilon: Double): List<Float> {
        val lenHalf = pts.size/2
        val pointList = 0.until(lenHalf).map { i->
            val pointer = i*2
            PointD(pts[pointer].toDouble(), pts[pointer+1].toDouble())
        }
    
        val outputPointList = mutableListOf<PointD>()
        LineSimple.ramerDouglasPeucker(pointList, epsilon, outputPointList)
    
        return outputPointList.map {
            listOf(it.x.toFloat(), it.y.toFloat())
        }.flatten()
    }

    fun convertPts(pts: FloatArray, epsilon: Double): FloatArray {
        return convertPts(pts.toList(), epsilon).toFloatArray()
    }
}
