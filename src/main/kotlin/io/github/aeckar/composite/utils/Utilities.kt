package io.github.aeckar.composite.utils

import io.github.aeckar.composite.Int128
import io.github.aeckar.composite.Rational

// ------------------------------ arithmetic ------------------------------

/**
 * The most significant 32 bits of this value.
 */
internal val Long.high inline get() = (this ushr 32).toInt()

/**
 * The least significant 32 bits of this value.
 */
internal val Long.low inline get() = this.toInt()

internal fun Int.widen() = this.toUInt().toLong()

/**
 * Returns true if the sum is the result of a signed integer overflow.
 */
internal fun addValueOverflows(x: Int, y: Int, sum: Int = x + y): Boolean {
    val isNegative = x < 0
    return isNegative == (y < 0) && isNegative xor (sum  < 0)
}

// ------------------------------ exception handling ------------------------------

/**
 * @throws ArithmeticException always
 */
internal fun raiseUndefined(message: String): Nothing = throw ArithmeticException(message)

/**
 * The name of the expected result may be inferred from the composite number receiver type or companion.
 * For specific operations, an explicit result name may be preferable.
 *
 * @throws ArithmeticException always
 */
internal fun Any.raiseOverflow(
    type: String = receiver(),
    additionalInfo: String? = null,
    cause: Throwable? = null
): Nothing {
    val info = additionalInfo?.let { " ($it)" } ?: ""
    throw ArithmeticException("$type overflows$info").initCause(cause)
}

/**
 * The name of the expected result may be inferred from the composite number receiver type or companion.
 * For specific operations, an explicit result name may be preferable.
 *
 * @throws ArithmeticException always
 */
internal fun Any.raiseIncorrectFormat(
    reason: String,
    type: String = receiver(),
    cause: Throwable? = null
): Nothing {
    val e = NumberFormatException("String does not contain a ${type.lowercase()} in the correct format ($reason)")
    throw e.initCause(cause)
}

private fun Any.receiver() = when (this) {
    is Rational, is Rational.Companion -> "Rational number"
    is Int128, is Int128.Companion -> "128-bit integer"
    else -> throw IllegalArgumentException("Receiver is not a CompositeNumber or companion of one")
}