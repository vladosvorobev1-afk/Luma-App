package com.example.formamind.data.remote

import com.example.formamind.data.ExerciseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit-тесты для мапперов DTO → domain.
 */
class CourseDtoMapperTest {

    // ==================== COURSE ====================

    @Test
    fun `CourseDto maps all fields correctly to Course`() {
        val dto = CourseDto(
            id = "anxiety",
            title = "Тревога",
            description = "Дыхание, заземление",
            emoji = "🌊",
            exercisesCount = 5
        )

        val domain = dto.toDomain(realExercisesCount = 3)

        assertEquals("anxiety", domain.id)
        assertEquals("Тревога", domain.title)
        assertEquals("Дыхание, заземление", domain.description)
        assertEquals("🌊", domain.emoji)
        assertEquals(3, domain.exercisesCount)
    }

    @Test
    fun `CourseDto with default fields maps correctly`() {
        val dto = CourseDto(
            id = "test",
            title = "Test"
        )

        val domain = dto.toDomain(realExercisesCount = 0)

        assertEquals("", domain.description)
        assertEquals("", domain.emoji)
        assertEquals(0, domain.exercisesCount)
    }

    // ==================== EXERCISE ====================

    @Test
    fun `ExerciseDto maps all fields correctly to Exercise`() {
        val dto = ExerciseDto(
            id = "anxiety_1",
            courseId = "anxiety",
            title = "Дыхание по квадрату",
            description = "4-4-4-4",
            type = "BREATHING",
            durationSec = 300,
            instruction = "Сядь удобно...",
            orderIndex = 1
        )

        val domain = dto.toDomain()

        assertEquals("anxiety_1", domain.id)
        assertEquals("anxiety", domain.courseId)
        assertEquals("Дыхание по квадрату", domain.title)
        assertEquals("4-4-4-4", domain.description)
        assertEquals(ExerciseType.BREATHING, domain.type)
        assertEquals(300, domain.durationSec)
        assertEquals("Сядь удобно...", domain.instruction)
        assertEquals(1, domain.orderIndex)
    }

    @Test
    fun `ExerciseDto with TEXT type maps to TEXT enum`() {
        val dto = exerciseDto(type = "TEXT")
        assertEquals(ExerciseType.TEXT, dto.toDomain().type)
    }

    @Test
    fun `ExerciseDto with JOURNAL type maps to JOURNAL enum`() {
        val dto = exerciseDto(type = "JOURNAL")
        assertEquals(ExerciseType.JOURNAL, dto.toDomain().type)
    }

    @Test
    fun `ExerciseDto with TIMER type maps to TIMER enum`() {
        val dto = exerciseDto(type = "TIMER")
        assertEquals(ExerciseType.TIMER, dto.toDomain().type)
    }

    @Test
    fun `ExerciseDto with unknown type falls back to TEXT`() {
        val dto = exerciseDto(type = "MEDITATION")
        assertEquals(ExerciseType.TEXT, dto.toDomain().type)
    }

    @Test
    fun `ExerciseDto with lowercase type is parsed correctly`() {
        val dto = exerciseDto(type = "breathing")
        assertEquals(ExerciseType.BREATHING, dto.toDomain().type)
    }

    @Test
    fun `ExerciseDto with default fields maps correctly`() {
        val dto = ExerciseDto(
            id = "x",
            courseId = "y",
            title = "T",
            type = "TEXT"
        )

        val domain = dto.toDomain()

        assertEquals("", domain.description)
        assertEquals(0, domain.durationSec)
        assertEquals("", domain.instruction)
        assertEquals(0, domain.orderIndex)
    }

    // ==================== РЕСПОНС ЦЕЛИКОМ ====================

    @Test
    fun `toDomainCourses calculates real exercises count`() {
        val response = CoursesResponseDto(
            courses = listOf(
                CourseDto(id = "anxiety", title = "Тревога"),
                CourseDto(id = "sleep", title = "Сон")
            ),
            exercises = listOf(
                exerciseDto(id = "a1", courseId = "anxiety"),
                exerciseDto(id = "a2", courseId = "anxiety"),
                exerciseDto(id = "a3", courseId = "anxiety"),
                exerciseDto(id = "s1", courseId = "sleep")
            )
        )

        val courses = response.toDomainCourses()

        assertEquals(2, courses.size)
        assertEquals(3, courses.first { it.id == "anxiety" }.exercisesCount)
        assertEquals(1, courses.first { it.id == "sleep" }.exercisesCount)
    }

    @Test
    fun `toDomainCourses with no exercises returns zero counts`() {
        val response = CoursesResponseDto(
            courses = listOf(CourseDto(id = "empty", title = "Empty")),
            exercises = emptyList()
        )

        val courses = response.toDomainCourses()

        assertEquals(1, courses.size)
        assertEquals(0, courses.first().exercisesCount)
    }

    @Test
    fun `toDomainCourses with empty courses returns empty list`() {
        val response = CoursesResponseDto()
        val courses = response.toDomainCourses()
        assertTrue(courses.isEmpty())
    }

    @Test
    fun `toDomainExercises maps all exercises`() {
        val response = CoursesResponseDto(
            exercises = listOf(
                exerciseDto(id = "e1", courseId = "c1"),
                exerciseDto(id = "e2", courseId = "c1"),
                exerciseDto(id = "e3", courseId = "c2")
            )
        )

        val exercises = response.toDomainExercises()

        assertEquals(3, exercises.size)
        assertEquals("e1", exercises[0].id)
        assertEquals("e3", exercises[2].id)
    }

    // ==================== ХЕЛПЕРЫ ====================

    private fun exerciseDto(
        id: String = "id",
        courseId: String = "course",
        type: String = "TEXT"
    ): ExerciseDto = ExerciseDto(
        id = id,
        courseId = courseId,
        title = "Title",
        description = "Desc",
        type = type,
        durationSec = 0,
        instruction = "Instr",
        orderIndex = 0
    )
}