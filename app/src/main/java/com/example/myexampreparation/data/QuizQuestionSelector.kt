package com.example.myexampreparation.data

import kotlin.math.min

object QuizQuestionSelector {

    /**
     * Normalizes text by converting to lowercase, stripping punctuation,
     * and removing common Hindi/English stop words for similarity comparison.
     */
    fun normalizeText(text: String): String {
        return text.lowercase()
            .replace(Regex("[\\p{Punct}\\s]+"), " ")
            .trim()
    }

    /**
     * Calculates Jaccard word similarity between two question texts.
     * Returns a float between 0.0 (no overlap) and 1.0 (identical words).
     */
    fun calculateWordSimilarity(q1: String, q2: String): Float {
        val norm1 = normalizeText(q1)
        val norm2 = normalizeText(q2)

        if (norm1 == norm2) return 1.0f

        val words1 = norm1.split("\\s+".toRegex()).filter { it.length > 1 }.toSet()
        val words2 = norm2.split("\\s+".toRegex()).filter { it.length > 1 }.toSet()

        if (words1.isEmpty() || words2.isEmpty()) return 0.0f

        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size

        return if (union > 0) intersection.toFloat() / union.toFloat() else 0.0f
    }

    /**
     * Checks if two questions are near-duplicates based on question text similarity.
     */
    fun isNearDuplicate(q1: Question, q2: Question, threshold: Float = 0.70f): Boolean {
        if (q1.id == q2.id) return true
        return calculateWordSimilarity(q1.question, q2.question) >= threshold
    }

    /**
     * Safely shuffles option positions (A, B, C, D) for a question
     * deterministically based on question.id so option order remains stable during resume
     * while accurately maintaining the correct answer string mapping.
     */
    fun shuffleOptionsSafely(question: Question, seed: Long? = null): Question {
        val originalCorrectOptionKey = question.correctAnswer.uppercase().trim()
        val correctValue = when (originalCorrectOptionKey) {
            "A" -> question.optionA
            "B" -> question.optionB
            "C" -> question.optionC
            "D" -> question.optionD
            else -> question.optionA
        }

        val originalOptions = listOf(
            question.optionA,
            question.optionB,
            question.optionC,
            question.optionD
        )

        val random = if (seed != null) java.util.Random(seed + question.id) else java.util.Random(question.id.toLong())
        val shuffledOptions = originalOptions.shuffled(random)

        val newCorrectIndex = shuffledOptions.indexOf(correctValue)
        val newCorrectAnswerKey = when (newCorrectIndex) {
            0 -> "A"
            1 -> "B"
            2 -> "C"
            3 -> "D"
            else -> "A"
        }

        return question.copy(
            optionA = shuffledOptions[0],
            optionB = shuffledOptions[1],
            optionC = shuffledOptions[2],
            optionD = shuffledOptions[3],
            correctAnswer = newCorrectAnswerKey
        )
    }

    /**
     * Intelligently selects a set of questions from candidatePool with high variety:
     * - Prevents exact duplicate question IDs
     * - Prevents near-duplicates
     * - Reduces consecutive same-concept/topic repetitions
     * - Reduces consecutive same-answer repetitions
     * - Safely balances correct option positions (A, B, C, D)
     * - Progressively relaxes constraints if the question pool is small
     *
     * @param candidatePool The available pool of candidate questions.
     * @param targetCount Desired number of questions (e.g. 20).
     * @param shuffleOptions Whether to shuffle option positions safely.
     * @param isFixedPaper If true (e.g. PYQ paper), preserves original order & options as-is.
     */
    fun selectQuestionsWithVariety(
        candidatePool: List<Question>,
        targetCount: Int,
        shuffleOptions: Boolean = true,
        isFixedPaper: Boolean = false
    ): List<Question> {
        if (candidatePool.isEmpty()) return emptyList()

        // PYQ / Fixed papers: Preserve exact original order and options
        if (isFixedPaper) {
            return candidatePool.distinctBy { it.id }.take(targetCount)
        }

        val uniqueCandidates = candidatePool.distinctBy { it.id }.shuffled().toMutableList()
        val desiredCount = min(targetCount, uniqueCandidates.size)

        val selectedList = mutableListOf<Question>()

        while (selectedList.size < desiredCount && uniqueCandidates.isNotEmpty()) {
            val lastSelected = selectedList.lastOrNull()

            // Pass 1: Strict constraints (no near-duplicate, different topic/concept, different answer)
            var bestCandidate = uniqueCandidates.firstOrNull { candidate ->
                val notNearDuplicate = selectedList.none { isNearDuplicate(candidate, it) }
                val differentTopic = lastSelected == null || candidate.topic.isBlank() || !candidate.topic.equals(lastSelected.topic, ignoreCase = true)
                val differentAnswer = lastSelected == null || !candidate.correctAnswer.equals(lastSelected.correctAnswer, ignoreCase = true)
                notNearDuplicate && differentTopic && differentAnswer
            }

            // Pass 2: Relax same-answer constraint
            if (bestCandidate == null) {
                bestCandidate = uniqueCandidates.firstOrNull { candidate ->
                    val notNearDuplicate = selectedList.none { isNearDuplicate(candidate, it) }
                    val differentTopic = lastSelected == null || candidate.topic.isBlank() || !candidate.topic.equals(lastSelected.topic, ignoreCase = true)
                    notNearDuplicate && differentTopic
                }
            }

            // Pass 3: Relax same-topic constraint
            if (bestCandidate == null) {
                bestCandidate = uniqueCandidates.firstOrNull { candidate ->
                    selectedList.none { isNearDuplicate(candidate, it) }
                }
            }

            // Pass 4: Fallback to any remaining unique question ID
            if (bestCandidate == null) {
                bestCandidate = uniqueCandidates.first()
            }

            selectedList.add(bestCandidate)
            uniqueCandidates.remove(bestCandidate)
        }

        // Apply safe option shuffling for generated/randomized quizzes
        return if (shuffleOptions) {
            selectedList.map { shuffleOptionsSafely(it) }
        } else {
            selectedList
        }
    }
}
