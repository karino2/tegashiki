package io.github.karino2.tegashiki

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun myassert(cond: Boolean) {
    if(!cond) throw Exception("Assertion fail")
}

// only support [x1, y1, x1, x2, y2, z3, x3, y3, z3...] order.
class KdFTensor(val size: Int) {
    companion object {
        fun arange(end: Int) : KdFTensor {
            val res = KdFTensor(end)
            repeat(end) { res.floatArray[it] = it.toFloat() }
            return res
        }

        fun zeros(size: Int) = KdFTensor(size)
    }

    constructor(values: List<Float>) : this(values.size){
        values.forEachIndexed { index, fl -> this.floatArray[index] = fl }
    }

    constructor(baseShape: Shape) : this(baseShape.elementNum) {
        this.shape = baseShape.clone()
    }

    fun readToArray(byteBuf: ByteBuffer) {
        byteBuf.rewind()
        repeat(size) {
            floatArray[it] = byteBuf.float
        }
    }

    fun createMirrorBuf() : ByteBuffer = ByteBuffer.allocateDirect(4*size).apply { order(ByteOrder.nativeOrder()) }

    val floatArray by lazy { FloatArray(size)}

    // element wize multiply must be match to size.
    // default [size].
    var shape = Shape(size)

    fun guessShape(wholeSize: Int, shapes: IntArray) : IntArray{
        val negativeNum = shapes.filter { it == -1 }.count()
        if(negativeNum == 0)
            return shapes
        if(negativeNum != 1)
            throw Exception("More than one -1 in reshape. ($negativeNum)")
        val determined = shapes.filter { it != -1 }.toList()
        val determinedElemNum = determined.fold(1, {acc, i-> acc*i})
        val guessed = wholeSize/determinedElemNum
        if(determinedElemNum*guessed != wholeSize)
            throw Exception("reshape not compatible, $determinedElemNum x $guessed != $wholeSize")
        return shapes.map {
            if(it == -1)
                guessed
            else
                it
        }.toIntArray()
    }


    fun reshape(vararg newShape: Int) : KdFTensor {
        val newGuessedShape = Shape(*guessShape(shape.elementNum, newShape))
        myassert(shape.elementNum == newGuessedShape.elementNum)
        shape = newGuessedShape
        return this
    }

    fun subTensor(indices: Indices) : KdFTensor {
        val values = indices.indices.map { floatArray[it] }
        val res = KdFTensor(values)
        res.shape = indices.shape
        return res
    }

    operator fun get(flagTensor: KdBTensor) : KdFTensor {
        if(flagTensor.shape.size > this.shape.size)
            throw Exception("Incompatible bool tensor input for indexer")
        flagTensor.shape.dimArray.indices.forEach {
            if(flagTensor.shape[it] != shape[it]) {
                throw Exception("Incompatible shape for bool tensor indexer. $it ${flagTensor.shape[it]} != ${shape[it]}")
            }
        }

        val firstDim = flagTensor.countTrue
        if(firstDim == 0)
            Log.d("Tegashiki", "boolean mask indexer with no true index. result shape becomes 0. It's valid but not yet tested.")

        val resShapeList = mutableListOf(firstDim)
        if(flagTensor.shape.size != shape.size)
            resShapeList.addAll(shape.dimArray.slice(flagTensor.shape.size until shape.size))

        val resData = ArrayList<Float>()

        val restAllArgs = (0 until shape.size-flagTensor.shape.size).map { AllIndex }

        fun traverse(shapeIndexArr: ArrayList<Int>, curCol: Int) {
            if(curCol == flagTensor.shape.size-1) {
                repeat(flagTensor.shape[curCol]) {
                    shapeIndexArr.add(it)
                    if(flagTensor.dataArray[flagTensor.shape.toIndex(*shapeIndexArr.toIntArray())]) {
                        val shapeidx = shapeIndexArr.map { NumberIndex(it) } + restAllArgs
                        resData.addAll( this.get(*shapeidx.toTypedArray()).floatArray.toList())
                    }
                    shapeIndexArr.removeAt(shapeIndexArr.size-1)
                }
            } else {
                repeat(flagTensor.shape[curCol]) {
                    shapeIndexArr.add(it)
                    traverse(shapeIndexArr, curCol+1)
                    shapeIndexArr.removeAt(shapeIndexArr.size-1)
                }
            }
        }
        traverse(ArrayList<Int>(), 0)

        val resShape = Shape(*resShapeList.toIntArray())
        myassert(resShape.elementNum == resData.size)
        val resTensor = KdFTensor(resData)
        resTensor.shape = resShape
        return resTensor
    }

    operator fun get(vararg ranges: ShapeIndex) : KdFTensor {
        val indices = shape.toIndices(*ranges)
        return subTensor(indices)
    }

    operator fun get(vararg ranges: Int) : Float {
        return floatArray[shape.toIndex(*ranges)]
    }

    operator fun set(vararg ranges: ShapeIndex, right: KdFTensor) {
        val indices = shape.toIndices(*ranges)
        myassert(indices.indices.size == right.size)
        indices.indices.forEachIndexed{ index, destIndex->this.floatArray[destIndex] = right.floatArray[index] }
    }


    fun max() = floatArray.max()!!
    fun min() = floatArray.min()!!


    // broad cast.
    operator fun set(vararg ranges: ShapeIndex, right: Float) {
        val indices = shape.toIndices(*ranges)
        indices.indices.forEach{ this.floatArray[it] = right }
    }

    operator fun set(vararg ranges: Int, right: Float)  {
        floatArray[shape.toIndex(*ranges)] = right
    }

    operator fun times(scale: Float) : KdFTensor {
        val res = KdFTensor(floatArray.map { it * scale }.toList())
        res.shape = shape.clone()
        return res
    }

    operator fun minus(delta: Float) : KdFTensor {
        val res = KdFTensor(floatArray.map { it - delta }.toList())
        res.shape = shape.clone()
        return res
    }

    fun scalar_equal(right: Float) : KdBTensor {
        val res = KdBTensor(shape)
        floatArray.forEachIndexed { index, fl ->  res.dataArray[index] = (floatArray[index] == right) }
        return res
    }


    val rowSize
    get() = shape[1]

    val rowNum
    get() = size/rowSize

    // I assume dimArray is [seqnum, vocabsize] for a while.
    fun column(colIdx: Int) = sequence {
        repeat(rowNum) {ridx->
            yield(floatArray[ridx*rowSize+colIdx])
        }
    }.toList()

    fun row(rowIdx: Int) = sequence {
        val rowOffset = rowIdx*rowSize
        repeat(rowSize) { yield(floatArray[rowOffset+it]) }

    }.toList()

    val rows
    get() = sequence {
        repeat(rowNum) {
            yield(row(it))
        }
    }

    val List<Float>.argMax : Int
    get() = this.indices.fold(0) {bestIdx, cur->
            if(this[bestIdx] < this[cur]) cur else bestIdx
    }

    // I assume dimArray is [seqnum, vocabsize] for a while.
    // return [seqnum] array.
    val argMaxEachRaw
    get() = rows.map { it.argMax }


}

object TensorDSL {
    val all = AllIndex
    fun n(i: Int) = NumberIndex(i)
    fun r(beg: Int, end: Int) = RangeIndex(beg, end)
    fun zeros(size: Int) = KdFTensor.zeros(size)
    fun arange(end: Int) = KdFTensor.arange(end)
    fun tensor(flist: List<Float>) = KdFTensor(flist)
}

fun tensor_ns(block: TensorDSL.()->Unit): Unit {
    TensorDSL.block()
}
