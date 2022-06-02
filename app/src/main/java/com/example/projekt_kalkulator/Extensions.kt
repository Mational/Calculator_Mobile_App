import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt


fun AppCompatActivity.registerForResult(onResult: (resultCode: Int, data: Intent?) -> Unit):
            ActivityResultLauncher<Intent>{
    return this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        onResult(result.resultCode, result.data)
    }
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun decpow(base: BigDecimal, exponent: BigDecimal): BigDecimal {
    var result = BigDecimal.ZERO
    val signOf2 = exponent.signum()

    // Perform X^(A+B)=X^A*X^B (B = remainder)
    val dn1 = base.toDouble()
    // Compare the same row of digits according to context
    val n2 = exponent.multiply(BigDecimal(signOf2)) // n2 is now positive
    val remainderOf2 = n2.remainder(BigDecimal.ONE)
    val n2IntPart = n2.subtract(remainderOf2)
    // Calculate big part of the power using context -
    // bigger range and performance but lower accuracy
    val intPow = base.pow(n2IntPart.intValueExact())
    val doublePow = BigDecimal(dn1.pow(remainderOf2.toDouble()))
    result = intPow.multiply(doublePow)

    // Fix negative power
    if (signOf2 == -1) result = BigDecimal.ONE.divide(result, RoundingMode.HALF_UP)
    return result
} /* from www.java2s.com */

fun decsqrt(value: BigDecimal): BigDecimal {
    val x = BigDecimal(sqrt(value.toDouble()))
    return x.add(BigDecimal(value.subtract(x.multiply(x)).toDouble() / (x.toDouble() * 2.0)))
} /* from https://stackoverflow.com/questions/13649703/square-root-of-bigdecimal-in-java */

/** log10 from http://www.java2s.com/example/java-utility-method/bigdecimal-log/log10-bigdecimal-b-f3d12.html **/
val SCALE = 18

fun declog(b: BigDecimal): BigDecimal {
    var b = b
    val NUM_OF_DIGITS: Int = SCALE + 2
    // need to add one to get the right number of dp
    //  and then add one again to get the next number
    //  so I can round it correctly.
    val mc = MathContext(NUM_OF_DIGITS, RoundingMode.HALF_EVEN)
    //special conditions:
    // log(-x) -> exception
    // log(1) == 0 exactly;
    // log of a number lessthan one = -log(1/x)
    if (b.signum() <= 0) {
        throw ArithmeticException("log of a negative number! (or zero)")
    } else if (b.compareTo(BigDecimal.ONE) == 0) {
        return BigDecimal.ZERO
    } else if (b.compareTo(BigDecimal.ONE) < 0) {
        return declog(BigDecimal.ONE.divide(b, mc)).negate()
    }
    val sb = StringBuilder()
    //number of digits on the left of the decimal point
    var leftDigits = b.precision() - b.scale()

    //so, the first digits of the log10 are:
    sb.append(leftDigits - 1).append(".")

    //this is the algorithm outlined in the webpage
    var n = 0
    while (n < NUM_OF_DIGITS) {
        b = b.movePointLeft(leftDigits - 1).pow(10, mc)
        leftDigits = b.precision() - b.scale()
        sb.append(leftDigits - 1)
        n++
    }
    var ans = BigDecimal(sb.toString())

    //Round the number to the correct number of decimal places.
    ans = ans.round(MathContext(ans.precision() - ans.scale() + SCALE, RoundingMode.HALF_EVEN))
    return ans
}
/*****************************************************************/

/** LN function from http://www.java2s.com/example/java/java.math/compute-the-natural-logarithm-of-bigdecimal-x-to-a-given-scale-x-0.html **/
fun decln(x: BigDecimal, scale: Int): BigDecimal {
    // Check that x > 0.
    require(x.signum() > 0) { "x <= 0" }

    // The number of digits to the left of the decimal point.
    val magnitude = x.toString().length - x.scale() - 1
    return if (magnitude < 3) {
        lnNewton(x, scale)
    } else {

        // x^(1/magnitude)
        val root: BigDecimal = intRoot(x, magnitude.toLong(), scale)

        // ln(x^(1/magnitude))
        val lnRoot: BigDecimal = lnNewton(root, scale)

        // magnitude*ln(x^(1/magnitude))
        BigDecimal.valueOf(magnitude.toLong()).multiply(lnRoot)
            .setScale(scale, BigDecimal.ROUND_HALF_EVEN)
    }
}

fun lnNewton(x: BigDecimal, scale: Int): BigDecimal {
    var x = x
    val sp1 = scale + 1
    val n = x
    var term: BigDecimal

    // Convergence tolerance = 5*(10^-(scale+1))
    val tolerance = BigDecimal.valueOf(5).movePointLeft(sp1)

    // Loop until the approximations converge
    // (two successive approximations are within the tolerance).
    do {

        // e^x
        val eToX: BigDecimal = exp(x, sp1)

        // (e^x - n)/e^x
        term = eToX.subtract(n)
            .divide(eToX, sp1, BigDecimal.ROUND_DOWN)

        // x - (e^x - n)/e^x
        x = x.subtract(term)
        Thread.yield()
    } while (term.compareTo(tolerance) > 0)
    return x.setScale(scale, BigDecimal.ROUND_HALF_EVEN)
}

fun intRoot(x: BigDecimal, index: Long, scale: Int): BigDecimal {
    // Check that x >= 0.
    var x = x
    require(x.signum() >= 0) { "x < 0" }
    val sp1 = scale + 1
    val n = x
    val i = BigDecimal.valueOf(index)
    val im1 = BigDecimal.valueOf(index - 1)
    val tolerance = BigDecimal.valueOf(5).movePointLeft(sp1)
    var xPrev: BigDecimal

    // The initial approximation is x/index.
    x = x.divide(i, scale, BigDecimal.ROUND_HALF_EVEN)

    // Loop until the approximations converge
    // (two successive approximations are equal after rounding).
    do {
        // x^(index-1)
        val xToIm1: BigDecimal = intPower(x, index - 1, sp1)

        // x^index
        val xToI = x.multiply(xToIm1).setScale(
            sp1,
            BigDecimal.ROUND_HALF_EVEN
        )

        // n + (index-1)*(x^index)
        val numerator = n.add(im1.multiply(xToI)).setScale(
            sp1,
            BigDecimal.ROUND_HALF_EVEN
        )

        // (index*(x^(index-1))
        val denominator = i.multiply(xToIm1).setScale(
            sp1,
            BigDecimal.ROUND_HALF_EVEN
        )

        // x = (n + (index-1)*(x^index)) / (index*(x^(index-1)))
        xPrev = x
        x = numerator.divide(denominator, sp1, BigDecimal.ROUND_DOWN)
        Thread.yield()
    } while (x.subtract(xPrev).abs().compareTo(tolerance) > 0)
    return x
}

fun exp(x: BigDecimal, scale: Int): BigDecimal {
    // e^0 = 1
    if (x.signum() == 0) {
        return BigDecimal.valueOf(1)
    } else if (x.signum() == -1) {
        return BigDecimal.valueOf(1).divide(
            exp(x.negate(), scale),
            scale, BigDecimal.ROUND_HALF_EVEN
        )
    }

    // Compute the whole part of x.
    var xWhole = x.setScale(0, BigDecimal.ROUND_DOWN)

    // If there isn't a whole part, compute and return e^x.
    if (xWhole.signum() == 0) {
        return expTaylor(x, scale)
    }

    // Compute the fraction part of x.
    val xFraction = x.subtract(xWhole)

    // z = 1 + fraction/whole
    val z = BigDecimal.valueOf(1)
        .add(
            xFraction.divide(
                xWhole, scale,
                BigDecimal.ROUND_HALF_EVEN
            )
        )

    // t = e^z
    val t: BigDecimal = expTaylor(z, scale)
    val maxLong = BigDecimal.valueOf(Long.MAX_VALUE)
    var result = BigDecimal.valueOf(1)

    // Compute and return t^whole using intPower().
    // If whole > Long.MAX_VALUE, then first compute products
    // of e^Long.MAX_VALUE.
    while (xWhole.compareTo(maxLong) >= 0) {
        result = result.multiply(intPower(t, Long.MAX_VALUE, scale))
            .setScale(scale, BigDecimal.ROUND_HALF_EVEN)
        xWhole = xWhole.subtract(maxLong)
        Thread.yield()
    }
    return result.multiply(intPower(t, xWhole.toLong(), scale))
        .setScale(scale, BigDecimal.ROUND_HALF_EVEN)
}

fun intPower(x: BigDecimal, exponent: Long, scale: Int): BigDecimal {
    // If the exponent is negative, compute 1/(x^-exponent).
    var x = x
    var exponent = exponent
    if (exponent < 0) {
        return BigDecimal.valueOf(1).divide(
            intPower(x, -exponent, scale), scale,
            BigDecimal.ROUND_HALF_EVEN
        )
    }
    var power = BigDecimal.valueOf(1)

    // Loop to compute value^exponent.
    while (exponent > 0) {

        // Is the rightmost bit a 1?
        if (exponent and 1 == 1L) {
            power = power.multiply(x).setScale(
                scale,
                BigDecimal.ROUND_HALF_EVEN
            )
        }

        // Square x and shift exponent 1 bit to the right.
        x = x.multiply(x).setScale(scale, BigDecimal.ROUND_HALF_EVEN)
        exponent = exponent shr 1
        Thread.yield()
    }
    return power
}

fun expTaylor(x: BigDecimal, scale: Int): BigDecimal {
    var factorial = BigDecimal.valueOf(1)
    var xPower = x
    var sumPrev: BigDecimal?

    // 1 + x
    var sum = x.add(BigDecimal.valueOf(1))

    // Loop until the sums converge
    // (two successive sums are equal after rounding).
    var i = 2
    do {
        // x^i
        xPower = xPower.multiply(x).setScale(
            scale,
            BigDecimal.ROUND_HALF_EVEN
        )

        // i!
        factorial = factorial.multiply(BigDecimal.valueOf(i.toLong()))

        // x^i/i!
        val term = xPower.divide(
            factorial, scale,
            BigDecimal.ROUND_HALF_EVEN
        )

        // sum = sum + x^i/i!
        sumPrev = sum
        sum = sum.add(term)
        ++i
        Thread.yield()
    } while (sum.compareTo(sumPrev) != 0)
    return sum
}

/**************************************************************************/