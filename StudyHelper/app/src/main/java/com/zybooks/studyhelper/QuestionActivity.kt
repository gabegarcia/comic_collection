package com.zybooks.studyhelper

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zybooks.studyhelper.model.Question
import com.zybooks.studyhelper.model.Subject
import com.zybooks.studyhelper.viewmodel.QuestionListViewModel
import androidx.lifecycle.ViewModelProvider
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar


class QuestionActivity : AppCompatActivity() {

    private lateinit var questionListViewModel: QuestionListViewModel
    private lateinit var subject: Subject
    private lateinit var questionList: List<Question>
    private lateinit var answerLabelTextView: TextView
    private lateinit var answerTextView: TextView
    private lateinit var answerButton: Button
    private lateinit var questionTextView: TextView
    private lateinit var showQuestionLayout: ViewGroup
    private lateinit var noQuestionLayout: ViewGroup
    private var currentQuestionIndex = 0
    private lateinit var deletedQuestion: Question

    companion object {
        const val EXTRA_SUBJECT_ID = "com.zybooks.studyhelper.subject_id"
        const val EXTRA_SUBJECT_TEXT = "com.zybooks.studyhelper.subject_text"
    }

    private val questionListViewModel: QuestionListViewModel by lazy {
        ViewModelProvider(this).get(QuestionListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        questionTextView = findViewById(R.id.question_text_view)
        answerLabelTextView = findViewById(R.id.answer_label_text_view)
        answerTextView = findViewById(R.id.answer_text_view)
        answerButton = findViewById(R.id.answer_button)
        showQuestionLayout = findViewById(R.id.show_question_layout)
        noQuestionLayout = findViewById(R.id.no_question_layout)

        // Add click callbacks
        answerButton.setOnClickListener { toggleAnswerVisibility() }
        findViewById<Button>(R.id.add_question_button).setOnClickListener { addQuestion() }

        // SubjectActivity should provide the subject ID and text
        val subjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, 0)
        val subjectText = intent.getStringExtra(EXTRA_SUBJECT_TEXT)
        subject = Subject(subjectId, subjectText!!)

        // Get all questions for this subject
       // questionListViewModel = QuestionListViewModel(application)
        //questionList = questionListViewModel.getQuestions(subjectId)

        // Display question
        //updateUI()

        questionList = emptyList()
        questionListViewModel.loadQuestions(subjectId)
        questionListViewModel.questionListLiveData.observe(
            this, { questionList ->
                this.questionList = questionList
                updateUI()
            })
    }

    private fun updateUI() {
        showQuestion(currentQuestionIndex)

        if (questionList.isEmpty()) {
            updateAppBarTitle()
            displayQuestion(false)
        } else {
            displayQuestion(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.question_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        //  Determine which app bar item was chosen
        return when (item.itemId) {
            R.id.previous -> {
                showQuestion(currentQuestionIndex - 1)
                true
            }
            R.id.next -> {
                showQuestion(currentQuestionIndex + 1)
                true
            }
            R.id.add -> {
                addQuestion()
                true
            }
            R.id.edit -> {
                editQuestion()
                true
            }
            R.id.delete -> {
                deleteQuestion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayQuestion(display: Boolean) {
        if (display) {
            showQuestionLayout.visibility = View.VISIBLE
            noQuestionLayout.visibility = View.GONE
        } else {
            showQuestionLayout.visibility = View.GONE
            noQuestionLayout.visibility = View.VISIBLE
        }
    }

    private fun updateAppBarTitle() {

        // Display subject and number of questions in app bar
        val title = resources.getString(R.string.question_number, subject.text,
            currentQuestionIndex + 1, questionList.size)
        setTitle(title)
    }

    private val addQuestionResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            // Display the added question, which will appear at end of list
            currentQuestionIndex = questionList.size

            Toast.makeText(this, R.string.question_added, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addQuestion() {
        val intent = Intent(this, QuestionEditActivity::class.java)
        intent.putExtra(EXTRA_SUBJECT_ID, subject.id)
        addQuestionResultLauncher.launch(intent)
    }

    private val editQuestionResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, R.string.question_updated, Toast.LENGTH_SHORT).show()
        }
    }

    private fun editQuestion() {
        val intent = Intent(this, QuestionEditActivity::class.java)
        intent.putExtra(QuestionEditActivity.EXTRA_QUESTION_ID, questionList[currentQuestionIndex].id)
        editQuestionResultLauncher.launch(intent)
    }

    private fun deleteQuestion() {
        if (currentQuestionIndex >= 0) {
            val question = questionList[currentQuestionIndex]
            questionListViewModel.deleteQuestion(question)

            Toast.makeText(this, R.string.question_deleted, Toast.LENGTH_SHORT).show()

            // Save question in case user wants to undo delete
            deletedQuestion = question

            // Show delete message with Undo button
            val snackbar = Snackbar.make(
                findViewById(R.id.coordinator_layout),
                R.string.question_deleted, Snackbar.LENGTH_LONG
            )
            snackbar.setAction(R.string.undo) {
                // Add question back
                questionListViewModel.addQuestion(deletedQuestion)
            }
            snackbar.show()
        }
    }

    private fun showQuestion(questionIndex: Int) {

        // Show question at the given index
        if (questionList.isNotEmpty()) {
            var newQuestionIndex = questionIndex

            if (questionIndex < 0) {
                newQuestionIndex = questionList.size - 1
            } else if (questionIndex >= questionList.size) {
                newQuestionIndex = 0
            }

            currentQuestionIndex = newQuestionIndex
            updateAppBarTitle()

            val question = questionList[currentQuestionIndex]
            questionTextView.text = question.text
            answerTextView.text = question.answer
        } else {
            // No questions yet
            currentQuestionIndex = -1
        }
    }

    private fun toggleAnswerVisibility() {
        if (answerTextView.visibility == View.VISIBLE) {
            answerButton.setText(R.string.show_answer)
            answerTextView.visibility = View.INVISIBLE
            answerLabelTextView.visibility = View.INVISIBLE
        } else {
            answerButton.setText(R.string.hide_answer)
            answerTextView.visibility = View.VISIBLE
            answerLabelTextView.visibility = View.VISIBLE
        }
    }
}