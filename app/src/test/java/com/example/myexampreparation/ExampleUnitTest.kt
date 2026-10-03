package com.example.myexampreparation

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class ExampleUnitTest {

    @Test
    fun generateAndValidateMaster1150Csv() {
        val master700File = File("C:/Users/Asus/Downloads/My_Exam_Preparation_Master_Questions_700.csv")
        if (!master700File.exists()) {
            val masterAssetsFile = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/app/src/main/assets/My_Exam_Preparation_Master_Questions_1150.csv")
            assertTrue("Assets 1150 Master CSV file must exist", masterAssetsFile.exists())
            return
        }

        val pyq04FebFile = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/app/src/main/assets/pyq_rajasthan_cet_2023_04feb_shift2.csv")
        val pyq05FebShift1File = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/app/src/main/assets/pyq_rajasthan_cet_2023_shift1.csv")
        val pyq05FebShift2File = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/app/src/main/assets/pyq_rajasthan_cet_2023_shift2.csv")

        val master700Lines = master700File.readLines().filter { it.isNotBlank() }
        val header = master700Lines.first().removePrefix("\uFEFF")

        // 1. Process Master 700 lines (renumber IDs to 1..700)
        val part1Rows = mutableListOf<String>()
        for (i in 1 until master700Lines.size) {
            val line = master700Lines[i]
            val firstComma = line.indexOf(',')
            if (firstComma != -1) {
                val restOfLine = line.substring(firstComma)
                part1Rows.add("$i$restOfLine")
            }
        }
        assertEquals(700, part1Rows.size)

        // 2. Process PYQ 04 Feb Shift 2 (IDs 701..850)
        val pyq04FebLines = pyq04FebFile.readLines().filter { it.isNotBlank() }.drop(1)
        assertEquals(150, pyq04FebLines.size)

        // 3. Process PYQ 05 Feb Shift 1 (IDs 851..1000)
        val pyq05Feb1Lines = pyq05FebShift1File.readLines().filter { it.isNotBlank() }.drop(1)
        assertEquals(150, pyq05Feb1Lines.size)

        // 4. Process PYQ 05 Feb Shift 2 (IDs 1001..1150)
        val pyq05Feb2Lines = pyq05FebShift2File.readLines().filter { it.isNotBlank() }.drop(1)
        assertEquals(150, pyq05Feb2Lines.size)

        // Combine all rows
        val allRows = part1Rows + pyq04FebLines + pyq05Feb1Lines + pyq05Feb2Lines
        assertEquals(1150, allRows.size)

        // Build complete CSV string
        val sb = StringBuilder()
        sb.append(header).append("\n")
        allRows.forEach { row ->
            sb.append(row).append("\n")
        }

        val finalCsvContent = sb.toString()

        // Write to root and assets folder
        val rootOutputFile = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/My_Exam_Preparation_Master_Questions_1150.csv")
        rootOutputFile.writeText(finalCsvContent)

        val assetsOutputFile = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/app/src/main/assets/My_Exam_Preparation_Master_Questions_1150.csv")
        assetsOutputFile.writeText(finalCsvContent)

        assertTrue(rootOutputFile.exists())
        assertTrue(assetsOutputFile.exists())
    }

    @Test
    fun verifyLocalMaster1150Integration() {
        val masterAssetsFile = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/app/src/main/assets/My_Exam_Preparation_Master_Questions_1150.csv")
        assertTrue("Assets 1150 Master CSV file must exist", masterAssetsFile.exists())

        val lines = masterAssetsFile.readLines().filter { it.isNotBlank() }
        assertEquals("Total lines must be 1151 (1 header + 1150 questions)", 1151, lines.size)

        val header = lines.first().removePrefix("\uFEFF")
        val expectedHeader = "id,subject,topic,quizSetId,quizSetTitle,quizSetSubtitle,question,optionA,optionB,optionC,optionD,correctAnswer,explanation,exam,year,difficulty"
        assertEquals("Header must match exact 16-column schema", expectedHeader, header)

        val ids = mutableSetOf<Int>()
        var pyqCount = 0
        var nonPyqCount = 0

        for (i in 1 until lines.size) {
            val line = lines[i]
            val firstComma = line.indexOf(',')
            assertTrue("Line $i must contain comma after id", firstComma != -1)

            val id = line.substring(0, firstComma).toIntOrNull()
            assertNotNull("ID on line $i must be a valid integer", id)
            assertTrue("ID must be in range 1..1150", id!! in 1..1150)
            assertFalse("ID $id must be unique", ids.contains(id))
            ids.add(id)

            if (id in 701..1150) {
                assertTrue("PYQ line must contain 'Rajasthan CET Senior Secondary'", line.contains("Rajasthan CET Senior Secondary"))
                assertTrue("PYQ line must contain '2023'", line.contains("2023"))
                pyqCount++
            } else {
                nonPyqCount++
            }
        }

        assertEquals("Exactly 1150 unique IDs must be present", 1150, ids.size)
        assertEquals("IDs 1..700 non-PYQ count must be 700", 700, nonPyqCount)
        assertEquals("IDs 701..1150 PYQ count must be 450", 450, pyqCount)
        assertEquals("Min ID must be 1", 1, ids.minOrNull())
        assertEquals("Max ID must be 1150", 1150, ids.maxOrNull())
    }

    @Test
    fun createAndValidateMyExamPreparationMasterQuestionsCsv() {
        val master1150File = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/My_Exam_Preparation_Master_Questions_1150.csv")
        assertTrue("Source 1150 file must exist", master1150File.exists())

        val masterContent = master1150File.readText()

        val rootTarget = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/My_Exam_Preparation_Master_Questions.csv")
        rootTarget.writeText(masterContent)

        val assetsTarget = File("C:/Users/Asus/AndroidStudioProjects/MyExamPreparation/app/src/main/assets/My_Exam_Preparation_Master_Questions.csv")
        assetsTarget.writeText(masterContent)

        assertTrue(rootTarget.exists())
        assertTrue(assetsTarget.exists())
        assertEquals("Content must be 100% identical", masterContent, rootTarget.readText())
        assertEquals("Content must be 100% identical", masterContent, assetsTarget.readText())
    }

    @Test
    fun testNaturalOrderComparator() {
        val sets = listOf(
            com.example.myexampreparation.data.QuizSet(10, "Set 10", "10 Qs", emptyList()),
            com.example.myexampreparation.data.QuizSet(2, "Set 2", "10 Qs", emptyList()),
            com.example.myexampreparation.data.QuizSet(1, "Set 1", "10 Qs", emptyList()),
            com.example.myexampreparation.data.QuizSet(11, "Set 11", "10 Qs", emptyList()),
            com.example.myexampreparation.data.QuizSet(3, "Set 3", "10 Qs", emptyList())
        )

        val sorted = NaturalOrderComparator.sortQuizSets(sets)
        val sortedTitles = sorted.map { it.title }

        val expectedTitles = listOf("Set 1", "Set 2", "Set 3", "Set 10", "Set 11")
        assertEquals(expectedTitles, sortedTitles)
    }
}
