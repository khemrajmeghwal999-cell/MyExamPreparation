package com.example.myexampreparation.data

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvQuestionImporter {

    fun importFromCsv(
        context: Context,
        uri: Uri
    ): ImportResult {

        return try {

            val inputStream =
                context.contentResolver.openInputStream(uri)
                    ?: return ImportResult(
                        success = false,
                        message = "CSV file open नहीं हो सकी।"
                    )

            val reader = BufferedReader(
                InputStreamReader(
                    inputStream,
                    Charsets.UTF_8
                )
            )

            val lines = reader.readLines()

            reader.close()

            if (lines.size <= 1) {
                return ImportResult(
                    success = false,
                    message = "CSV file में questions नहीं मिले।"
                )
            }

            val questions =
                mutableListOf<Question>()

            // First row = Header
            for (index in 1 until lines.size) {

                val line =
                    lines[index].trim()

                if (line.isEmpty()) {
                    continue
                }

                val columns =
                    parseCsvLine(line)

                // Expected 16 columns
                if (columns.size < 16) {
                    continue
                }

                // 0 = ID
                val id =
                    columns[0]
                        .toIntOrNull()
                        ?: continue

                // 14 = Year
                val year =
                    columns[14]
                        .toIntOrNull()

                // 3 = Quiz Set
                val quizSetId =
                    columns[3]
                        .removePrefix("Set ")
                        .trim()
                        .toIntOrNull()
                        ?: 0

                val question =
                    Question(

                        // 0 = ID
                        id = id,

                        // 6 = Question
                        question = columns[6],

                        // 7-10 = Options
                        optionA = columns[7],
                        optionB = columns[8],
                        optionC = columns[9],
                        optionD = columns[10],

                        // 11 = Correct Answer
                        correctAnswer = columns[11],

                        // 12 = Explanation
                        explanation = columns[12],

                        // 1 = Subject
                        subject = columns[1],

                        // 2 = Topic
                        topic = columns[2],

                        // 3 = Quiz Set
                        quizSetId = quizSetId,

                        // 4 = Quiz Set Title
                        quizSetTitle = columns[4],

                        // 5 = Quiz Set Subtitle
                        quizSetSubtitle = columns[5],

                        // 13 = Exam
                        exam = columns[13],

                        // 14 = Year
                        year = year,

                        // 15 = Difficulty
                        difficulty = columns[15]
                    )

                questions.add(question)
            }

            if (questions.isEmpty()) {

                return ImportResult(
                    success = false,
                    message = "Valid questions नहीं मिले।"
                )
            }

            // Save imported questions
            QuestionStorage.saveQuestions(
                context,
                questions
            )

            ImportResult(
                success = true,
                message =
                    "${questions.size} questions successfully imported."
            )

        } catch (e: Exception) {

            ImportResult(
                success = false,
                message =
                    "Import error: ${e.message}"
            )
        }
    }

    private fun parseCsvLine(
        line: String
    ): List<String> {

        val result =
            mutableListOf<String>()

        var current =
            StringBuilder()

        var insideQuotes =
            false

        for (character in line) {

            when {

                character == '"' -> {
                    insideQuotes =
                        !insideQuotes
                }

                character == ',' &&
                        !insideQuotes -> {

                    result.add(
                        current
                            .toString()
                            .trim()
                    )

                    current =
                        StringBuilder()
                }

                else -> {
                    current.append(
                        character
                    )
                }
            }
        }

        result.add(
            current
                .toString()
                .trim()
        )

        return result
    }
}

data class ImportResult(
    val success: Boolean,
    val message: String
)