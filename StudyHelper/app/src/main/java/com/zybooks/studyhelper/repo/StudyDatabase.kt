package com.zybooks.studyhelper.repo

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zybooks.studyhelper.model.Question
import com.zybooks.studyhelper.model.Subject

@Database(entities = [Question::class, Subject::class], version = 1)
abstract class StudyDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao
    abstract fun subjectDao(): SubjectDao
}