package com.example.myexampreparation.data

object QuestionBank {

    private val questions = mutableListOf<Question>()

    fun loadQuestions(
        context: android.content.Context
    ) {
        questions.clear()

        val builtInQuestions = scienceQuizSets.flatMap { it.questions }
        var storedQuestions = QuestionStorage.loadQuestions(context)

        // Automatically preload the 1,150-question Master CSV from assets on first launch if storage is empty
        if (storedQuestions.isEmpty()) {
            try {
                val assetStream = context.assets.open("My_Exam_Preparation_Master_Questions_1150.csv")
                val csvContent = assetStream.bufferedReader().use { it.readText() }
                val syncResult = QuestionBankSyncManager.parseAndMergeMasterCsv(context, csvContent)
                if (syncResult.success) {
                    storedQuestions = QuestionStorage.loadQuestions(context)
                }
            } catch (e: Exception) {
                android.util.Log.e("QuestionBank", "Failed to load default Master CSV asset", e)
            }
        }

        val merged = (builtInQuestions + storedQuestions)
            .associateBy { it.id }
            .values

        questions.addAll(merged)
    }

    fun getAllQuestions(): List<Question> {
        return questions.toList()
    }

    fun addQuestions(newQuestions: List<Question>) {
        questions.addAll(newQuestions)
    }

    fun clearQuestions() {
        questions.clear()
    }

    fun getQuestionsBySubject(subject: String): List<Question> {
        return questions.filter {
            SubjectMatcher.matchesSubjectName(it.subject, subject)
        }
    }

    fun getQuestionsByTopic(topic: String): List<Question> {
        return questions.filter {
            it.topic.equals(topic, ignoreCase = true)
        }
    }

    fun getQuestionsByQuizSet(quizSetId: Int): List<Question> {
        return questions.filter {
            it.quizSetId == quizSetId
        }
    }

    fun getBookmarkedQuestions(): List<Question> {
        return questions.filter {
            it.isBookmarked
        }
    }

    fun getSubjects(): List<String> {
        return questions
            .map {
                it.subject
            }
            .filter {
                it.isNotBlank()
            }
            .distinct()
            .sorted()
    }

    fun getTopicsBySubject(
        subject: String
    ): List<String> {
        return questions
            .filter {
                SubjectMatcher.matchesSubjectName(it.subject, subject)
            }
            .map {
                it.topic
            }
            .filter {
                it.isNotBlank()
            }
            .distinct()
            .sorted()
    }
}