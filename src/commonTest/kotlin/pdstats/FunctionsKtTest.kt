package pdstats

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.expect

internal class FunctionsKtTest {

    @Test
    fun testSplit4Years() {

        val expected = listOf(
            instant(2017, 4, 4, 14)..instant(2018, 1, 1),
            instant(2018, 1, 1)..instant(2019, 1, 1),
            instant(2019, 1, 1)..instant(2020, 1, 1),
            instant(2020, 1, 1)..instant(2020, 11, 30, 14),
        )

        expect(expected) {
            splitByYear(instant(2017, 4, 4, 14), instant(2020, 11, 30, 14))
        }
    }

    @Test
    fun testSplit3Years() {

        val expected = listOf(
            instant(2017, 4, 4, 14)..instant(2018, 1, 1),
            instant(2018, 1, 1)..instant(2019, 1, 1),
            instant(2019, 1, 1)..instant(2020, 1, 1),
        )

        expect(expected) {
            splitByYear(instant(2017, 4, 4, 14), instant(2020, 1, 1))
        }
    }

    @Test
    fun testSplit2Years() {

        val expected = listOf(
            instant(2019, 4, 4, 14)..instant(2020, 1, 1),
            instant(2020, 1, 1)..instant(2020, 11, 30, 14),
        )

        expect(expected) {
            splitByYear(instant(2019, 4, 4, 14), instant(2020, 11, 30, 14))
        }
    }

    @Test
    fun testSplit1Year() {

        val expected = listOf(
            instant(2020, 4, 4, 14)..instant(2020, 11, 30, 14),
        )

        expect(expected) {
            splitByYear(instant(2020, 4, 4, 14), instant(2020, 11, 30, 14))
        }
    }

    private fun instant(y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0) =
        LocalDateTime(y, m, d, h, min).toInstant(TimeZone.currentSystemDefault())


    @Test
    fun testSplitInsideShift() {
        val expected = listOf(
            instant(2020, 4, 4, 9)..instant(2020, 4, 4, 15),
        )

        expect(expected) {
            splitByShifts(instant(2020, 4, 4, 9), instant(2020, 4, 4, 15))
        }
    }

    @Test
    fun testSplitEqualShift() {
        val expected = listOf(
            instant(2020, 4, 4, SOB)..instant(2020, 4, 4, EOB),
        )

        expect(expected) {
            splitByShifts(instant(2020, 4, 4, SOB), instant(2020, 4, 4, EOB))
        }
    }

    @Test
    fun testSplitEqual2Shifts() {
        val expected = listOf(
            instant(2020, 4, 4, SOB)..instant(2020, 4, 4, EOB),
            instant(2020, 4, 4, EOB)..instant(2020, 4, 5, SOB),
        )

        expect(expected) {
            splitByShifts(instant(2020, 4, 4, SOB), instant(2020, 4, 5, SOB))
        }
    }

    @Test
    fun testSplitEqual4Shifts() {
        val expected = listOf(
            instant(2020, 4, 4, EOB)..instant(2020, 4, 5, SOB),
            instant(2020, 4, 5, SOB)..instant(2020, 4, 5, EOB),
            instant(2020, 4, 5, EOB)..instant(2020, 4, 6, SOB),
            instant(2020, 4, 6, SOB)..instant(2020, 4, 6, EOB),
        )

        expect(expected) {
            splitByShifts(instant(2020, 4, 4, EOB), instant(2020, 4, 6, EOB))
        }
    }

    @Test
    fun testSplitNonEqual4Shifts() {
        val expected = listOf(
            instant(2020, 4, 4, EOB + 1)..instant(2020, 4, 5, SOB),
            instant(2020, 4, 5, SOB)..instant(2020, 4, 5, EOB),
            instant(2020, 4, 5, EOB)..instant(2020, 4, 6, SOB),
            instant(2020, 4, 6, SOB)..instant(2020, 4, 6, EOB - 1),
        )

        expect(expected) {
            splitByShifts(instant(2020, 4, 4, EOB + 1), instant(2020, 4, 6, EOB - 1))
        }
    }


}
