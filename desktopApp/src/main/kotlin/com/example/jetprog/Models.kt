package com.example.jetprog

// Базовий абстрактний клас
abstract class Person(
    val id: Int,
    var name: String,
    var email: String
) {
    // Абстрактний метод, який ОБОВ'ЯЗКОВО мають реалізувати нащадки
    abstract fun getRoleName(): String

    // Віртуальний метод, який МОЖНА перевизначити
    open fun getDisplayInfo(): String {
        return "ID: $id | $name ($email)"
    }
}

// Наслідування (Студент)
class Student(
    id: Int,
    name: String,
    email: String,
    var course: Int,
    var group: String
) : Person(id, name, email) {
    override fun getRoleName(): String = "Студент"
    override fun getDisplayInfo(): String =
        "[${getRoleName()}] ${super.getDisplayInfo()} | Курс: $course, Група: $group"
}

// Наслідування (Викладач)
class Teacher(
    id: Int,
    name: String,
    email: String,
    var subject: String,
    var salary: Double
) : Person(id, name, email) {
    override fun getRoleName(): String = "Викладач"
    override fun getDisplayInfo(): String =
        "[${getRoleName()}] ${super.getDisplayInfo()} | Предмет: $subject, З/П: $salary"
}