package io.github.aeckar.composite.utils

/**
 * Thrown when a [table][Table] is initialized with invalid dimensions.
 */
internal class InvalidDimensionsException(
    rowCount: Int,
    columnCount: Int
) : Exception("${rowCount}x$columnCount table has 0 rows or 0 columns")

/**
 * A table of entries.
 *
 * This class is implemented as a 2-dimensional array of elements of type [E]`?`.
 * Tables cannot have 0 rows, 0 columns, nor have a non-rectangular shape.
 * If a table is initialized with an invalid number of rows or columns, an [InvalidDimensionsException] is thrown.
 *
 * Access to entries involves casting them to their non-nullable form.
 * If an entry is not initialized, that is to say it is null, a [NoSuchElementException] will be thrown upon access.
 *
 * This class also provides to iterate over entries, rows, columns idiomatically.
 *
 * Instances are mutable.
 */
@JvmInline
internal value class Table<E : Any> private constructor(private val backingArray: Array<Array<E?>>) {
    init {
        if (backingArray.isEmpty() || backingArray[0].isEmpty()) {
            val rowCount = backingArray.size
            val columnCount = if (backingArray.isNotEmpty()) backingArray[0].size else 0
            throw InvalidDimensionsException(rowCount, columnCount)
        }
    }

    @Suppress("UNCHECKED_CAST")
    constructor(
        rowCount: Int,
        columnCount: Int
    ) : this(Array<Array<Any?>>(rowCount) { Array<Any?>(columnCount) { null } } as Array<Array<E?>>)

    interface Index {
        operator fun component1(): Int
        operator fun component2(): Int
    }

    @JvmInline
    value class Row<E : Any>(private val entries: Array<E>) {
        inline fun byColumn(action: (E) -> Unit) = entries.forEach(action)

        inline fun byColumnIndexed(action: (Int, E) -> Unit) = entries.forEachIndexed(action)
    }

    // ------------------------------ access and modification ------------------------------

    operator fun get(rowIndex: Int, columnIndex: Int): E {
        return try {
            backingArray[rowIndex][columnIndex] as E
        } catch (e: NullPointerException) {
            throw NoSuchElementException("Attempted access of uninitialized table element", e)
        } catch (_: ArrayIndexOutOfBoundsException) {
            raiseInvalidIndex(rowIndex, columnIndex)
        }
    }

    operator fun set(rowIndex: Int, columnIndex: Int, entry: E) {
        try {
            backingArray[rowIndex][columnIndex] = entry
        } catch (e: ArrayIndexOutOfBoundsException) {
            raiseInvalidIndex(rowIndex, columnIndex)
        }
    }

    /**
     * @throws NoSuchElementException always
     */
    private fun raiseInvalidIndex(rowIndex: Int, columnIndex: Int): Nothing {
        throw NoSuchElementException(
            "Index [$rowIndex, $columnIndex] lies outside the bounds of the table " +
                    "(rows = ${countRows()}, columns = ${countColumns()})"
        )
    }

    // ------------------------------ iteration ------------------------------

    fun countRows() = backingArray.size
    fun countColumns() = backingArray[0].size

    @Suppress("UNCHECKED_CAST")
    inline fun byRow(action: (Row<E>) -> Unit) = backingArray.forEach { action(Row(it as Array<E>)) }

    @Suppress("UNCHECKED_CAST")
    inline fun byRowIndexed(action: (Int, Row<E>) -> Unit) = backingArray.forEachIndexed { index, row ->
        action(index, Row(row as Array<E>))
    }

    inline fun byEntry(action: (E) -> Unit) {
        var rowIndex = 0
        var columnIndex = 0
        do {
            (backingArray[rowIndex][columnIndex] as E).apply(action)
            if (columnIndex == backingArray[rowIndex].lastIndex) {
                ++rowIndex
                columnIndex = 0
            } else {
                ++columnIndex
            }
        } while (rowIndex < backingArray.size)
    }

    inline fun byEntryIndexed(action: (Index, E) -> Unit) {
        val index = object : Index {
            var rowIndex: Int = 0
            var columnIndex: Int = 0

            override fun component1() = rowIndex
            override fun component2() = columnIndex
        }
        while (index.rowIndex < backingArray.size) with(index) {
            action(index, backingArray[rowIndex][columnIndex] as E)
            if (columnIndex == backingArray[rowIndex].lastIndex) {
                ++rowIndex
                columnIndex = 0
            } else {
                ++columnIndex
            }
        }
    }
}