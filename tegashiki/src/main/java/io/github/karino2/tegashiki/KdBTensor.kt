package io.github.karino2.tegashiki

class KdBTensor(baseShape: Shape) {
    var shape = baseShape.clone()
    val dataArray = BooleanArray(shape.elementNum)

    val countTrue
    get() = dataArray.fold(0) { acc, b -> if(b) acc+1 else acc }

    operator fun get(vararg ranges: Int) : Boolean {
        return dataArray[shape.toIndex(*ranges)]
    }

}
