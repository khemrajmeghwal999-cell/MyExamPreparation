package com.example.myexampreparation.data
data class QuizSet(
    val id: Int,
    val title: String,
    val subtitle: String,
    val questions: List<Question>
)

// ============================================================
// SCIENCE - SET 1
// Topic: पदार्थ की अवस्था
// ============================================================

val scienceQuestionsSet1 = listOf(

    Question(
        id = 1,

        question = "एक ही प्रकार का परमाणु निम्न में किसमें मिलता है?",

        optionA = "खनिज यौगिक",
        optionB = "खनिज मिश्रण",
        optionC = "प्राकृतिक तत्व",
        optionD = "कोई नहीं",

        correctAnswer = "C",

        explanation = "तत्व ऐसे शुद्ध पदार्थ हैं जिनमें एक ही प्रकार के परमाणु पाए जाते हैं। उदाहरण के लिए हाइड्रोजन, ऑक्सीजन, तांबा और सोना।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 1,

        exam = "BPSC",
        year = 2011,

        difficulty = "Easy"
    ),


    Question(
        id = 2,

        question = "दो या दो से अधिक शुद्ध पदार्थों को निश्चित अनुपात में रासायनिक रूप से मिलाने से बना पदार्थ क्या कहलाता है?",

        optionA = "मिश्रण",
        optionB = "यौगिक",
        optionC = "तत्व",
        optionD = "धातु",

        correctAnswer = "B",

        explanation = "दो या दो से अधिक तत्व निश्चित अनुपात में रासायनिक रूप से मिलकर यौगिक बनाते हैं।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 1,

        exam = "General Science",
        year = null,

        difficulty = "Easy"
    ),


    Question(
        id = 3,

        question = "निम्न में से कौन-सा एक तत्व है?",

        optionA = "जल",
        optionB = "कार्बन डाइऑक्साइड",
        optionC = "ऑक्सीजन",
        optionD = "सोडियम क्लोराइड",

        correctAnswer = "C",

        explanation = "ऑक्सीजन एक रासायनिक तत्व है। जल, CO₂ और NaCl यौगिक हैं।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 1,

        exam = "General Science",
        year = null,

        difficulty = "Easy"
    ),


    Question(
        id = 4,

        question = "पानी का रासायनिक सूत्र क्या है?",

        optionA = "CO₂",
        optionB = "H₂O",
        optionC = "O₂",
        optionD = "H₂",

        correctAnswer = "B",

        explanation = "जल का रासायनिक सूत्र H₂O है। इसमें दो हाइड्रोजन परमाणु और एक ऑक्सीजन परमाणु होते हैं।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 1,

        exam = "General Science",
        year = null,

        difficulty = "Easy"
    ),


    Question(
        id = 5,

        question = "निम्न में से कौन-सा मिश्रण है?",

        optionA = "ऑक्सीजन",
        optionB = "सोना",
        optionC = "वायु",
        optionD = "हाइड्रोजन",

        correctAnswer = "C",

        explanation = "वायु विभिन्न गैसों का मिश्रण है, जिसमें मुख्य रूप से नाइट्रोजन और ऑक्सीजन होती हैं।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 1,

        exam = "General Science",
        year = null,

        difficulty = "Easy"
    )
)


// ============================================================
// SCIENCE - SET 2
// Topic: पदार्थ की अवस्था
// ============================================================

val scienceQuestionsSet2 = listOf(

    Question(
        id = 6,

        question = "पदार्थ की कितनी प्रमुख अवस्थाएँ मानी जाती हैं?",

        optionA = "दो",
        optionB = "तीन",
        optionC = "चार",
        optionD = "पाँच",

        correctAnswer = "B",

        explanation = "पदार्थ की तीन प्रमुख अवस्थाएँ ठोस, द्रव और गैस हैं।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 2,

        exam = "General Science",
        year = null,

        difficulty = "Easy"
    ),


    Question(
        id = 7,

        question = "ठोस पदार्थ की विशेषता क्या है?",

        optionA = "निश्चित आकार और निश्चित आयतन",
        optionB = "निश्चित आकार नहीं",
        optionC = "निश्चित आयतन नहीं",
        optionD = "इनमें से कोई नहीं",

        correctAnswer = "A",

        explanation = "ठोस पदार्थ का आकार और आयतन दोनों निश्चित होते हैं।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 2,

        exam = "General Science",
        year = null,

        difficulty = "Easy"
    ),


    Question(
        id = 8,

        question = "द्रव का अपना निश्चित आकार क्यों नहीं होता?",

        optionA = "कण बहुत दूर होते हैं",
        optionB = "द्रव पात्र का आकार ग्रहण करता है",
        optionC = "द्रव में कण नहीं होते",
        optionD = "द्रव गैस होता है",

        correctAnswer = "B",

        explanation = "द्रव का निश्चित आयतन होता है लेकिन वह जिस पात्र में रखा जाता है उसका आकार ग्रहण कर लेता है।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 2,

        exam = "General Science",
        year = null,

        difficulty = "Easy"
    ),


    Question(
        id = 9,

        question = "गैस के कणों के बीच कैसी दूरी होती है?",

        optionA = "बहुत कम",
        optionB = "निश्चित",
        optionC = "बहुत अधिक",
        optionD = "कोई दूरी नहीं",

        correctAnswer = "C",

        explanation = "गैसों में कणों के बीच बहुत अधिक रिक्त स्थान होता है।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 2,

        exam = "General Science",
        year = null,

        difficulty = "Medium"
    ),


    Question(
        id = 10,

        question = "बर्फ का पिघलना किस परिवर्तन का उदाहरण है?",

        optionA = "रासायनिक परिवर्तन",
        optionB = "भौतिक परिवर्तन",
        optionC = "परमाणु परिवर्तन",
        optionD = "कोई परिवर्तन नहीं",

        correctAnswer = "B",

        explanation = "बर्फ का पानी में बदलना भौतिक परिवर्तन है क्योंकि पदार्थ की रासायनिक संरचना नहीं बदलती।",

        subject = "Science",
        topic = "पदार्थ की अवस्था",
        quizSetId = 2,

        exam = "General Science",
        year = null,

        difficulty = "Easy"
    )
)


// ============================================================
// BACKWARD COMPATIBILITY
// Existing code में scienceQuestions अभी भी काम करेगा.
// ============================================================

val scienceQuestions = scienceQuestionsSet1


// ============================================================
// QUIZ SETS
// ============================================================

val scienceQuizSets = listOf(

    QuizSet(
        id = 1,
        title = "Set 1 - Basic Questions",
        subtitle = "Basic level - 5 Questions",
        questions = scienceQuestionsSet1
    ),

    QuizSet(
        id = 2,
        title = "Set 2 - Practice Questions",
        subtitle = "Practice level - 5 Questions",
        questions = scienceQuestionsSet2
    )
)