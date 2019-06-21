package io.github.karino2.tegashiki

import org.junit.Test

import org.junit.Assert.*


typealias n = NumberIndex
typealias r = RangeIndex
typealias all = AllIndex

class ShapeTest {
    @Test
    fun getOperator_normalCase() {
        val shape = Shape(3, 5, 2)
        assertEquals(3, shape[0])
        assertEquals(5, shape[1])
        assertEquals(2, shape[2])
    }

    @Test
    fun toIndices_one_shapeOne() {
        val shape = Shape(5)
        val actual = shape.toIndices(n(3))

        assertEquals(1, actual.shape.size)
        assertEquals(1, actual.shape[0])
        assertEquals(3, actual.indices[0])
    }

    @Test
    fun toIndices_one_all() {
        val shape = Shape(3)
        val actual = shape.toIndices(all)

        assertEquals(1, actual.shape.size)
        assertEquals(3, actual.shape[0])
        assertEquals(0, actual.indices[0])
        assertEquals(1, actual.indices[1])
        assertEquals(2, actual.indices[2])
    }

    @Test
    fun toIndices_one_range() {
        val shape = Shape(5)
        val actual = shape.toIndices(r(1, 3))

        assertEquals(2, actual.shape[0])
        assertEquals(1, actual.indices[0])
        assertEquals(2, actual.indices[1])
    }

    @Test
    fun toIndices_two_shapeOneOne() {
        val shape = Shape(3, 5)
        val actual = shape.toIndices(n(2), n(4))

        assertEquals(1, actual.shape.size)
        assertEquals(1, actual.shape[0])
        assertEquals(14, actual.indices[0])
    }

    /*
    np.array(range(15)).reshape([3, 5])
    res[2, :]
     */
    @Test
    fun toIndices_two_shapeOneAll() {
        val shape = Shape(3, 5)
        val actual = shape.toIndices(n(2), all)

        assertEquals(1, actual.shape.size)
        assertEquals(5, actual.shape[0])
        assertEquals(10, actual.indices[0])
        assertEquals(11, actual.indices[1])
        assertEquals(12, actual.indices[2])
        assertEquals(13, actual.indices[3])
        assertEquals(14, actual.indices[4])
    }

    /*
    np.array(range(15)).reshape([3, 5])
    res[1, 2:4]
     */
    @Test
    fun toIndices_two_shapeOneRange() {
        val shape = Shape(3, 5)
        val actual = shape.toIndices(n(1), r(2, 4))

        assertEquals(1, actual.shape.size)
        assertEquals(2, actual.shape[0])
        assertEquals(7, actual.indices[0])
        assertEquals(8, actual.indices[1])
    }

    /*
    np.array(range(15)).reshape([3, 5])
    res[0:2, 1:4]
     */
    @Test
    fun toIndices_two_shapeRange2() {
        val shape = Shape(3, 5)
        val actual = shape.toIndices(r(0, 2), r(1, 4))

        assertEquals(2, actual.shape.size)
        assertEquals(2, actual.shape[0])
        assertEquals(3, actual.shape[1])
        assertEquals(1, actual.indices[0])
        assertEquals(2, actual.indices[1])
        assertEquals(3, actual.indices[2])
        assertEquals(6, actual.indices[3])
        assertEquals(7, actual.indices[4])
        assertEquals(8, actual.indices[5])
    }

    @Test
    fun equal_test() {
        val shape1 = Shape(2, 3)
        val shape2 = Shape(2, 3)
        val shape3 = Shape(3, 4)

        assertEquals(shape1, shape1)
        assertEquals(shape1, shape2)
        assertNotEquals(shape1, shape3)
    }
}
