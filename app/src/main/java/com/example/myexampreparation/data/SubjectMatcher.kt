package com.example.myexampreparation.data

data class Subject(
    val name: String,
    val englishName: String,
    val topics: List<String>
)

object SubjectMatcher {

    /**
     * Checks if a question's subject string matches a given Subject object.
     * Compares questionSubject against both subject.name and subject.englishName (trimmed, case-insensitive).
     */
    fun matchesSubject(questionSubject: String?, subject: Subject?): Boolean {
        if (questionSubject.isNullOrBlank() || subject == null) return false
        val qSub = questionSubject.trim()
        val nameMatch = subject.name.isNotBlank() && qSub.equals(subject.name.trim(), ignoreCase = true)
        val englishMatch = subject.englishName.isNotBlank() && qSub.equals(subject.englishName.trim(), ignoreCase = true)
        return nameMatch || englishMatch
    }

    /**
     * Checks if a question's subject string matches a target subject name/englishName string.
     */
    fun matchesSubjectName(sub1: String?, sub2: String?): Boolean {
        if (sub1.isNullOrBlank() || sub2.isNullOrBlank()) return false
        return sub1.trim().equals(sub2.trim(), ignoreCase = true)
    }

    /**
     * Resolves a subject string (Hindi name or English name) to the matching Subject object from a list.
     */
    fun resolveSubject(subjectNameOrEnglishName: String?, availableSubjects: List<Subject>): Subject? {
        if (subjectNameOrEnglishName.isNullOrBlank()) return null
        val target = subjectNameOrEnglishName.trim()
        return availableSubjects.firstOrNull { subject ->
            target.equals(subject.name.trim(), ignoreCase = true) ||
            (subject.englishName.isNotBlank() && target.equals(subject.englishName.trim(), ignoreCase = true))
        }
    }

    /**
     * Filters a list of questions belonging to a given Subject (matching either name or englishName).
     */
    fun filterQuestionsBySubject(questions: List<Question>, subject: Subject?): List<Question> {
        if (subject == null) return emptyList()
        return questions.filter { matchesSubject(it.subject, subject) }
    }

    /**
     * Filters a list of questions belonging to a given Subject AND Topic.
     */
    fun filterQuestionsBySubjectAndTopic(
        questions: List<Question>,
        subject: Subject?,
        topic: String?
    ): List<Question> {
        if (subject == null || topic.isNullOrBlank()) return emptyList()
        val cleanTopic = topic.trim()
        return questions.filter { q ->
            matchesSubject(q.subject, subject) && q.topic.trim().equals(cleanTopic, ignoreCase = true)
        }
    }
}
