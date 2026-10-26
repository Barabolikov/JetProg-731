package com.example.jetprog

import com.google.gson.GsonBuilder
import java.io.File

class JsonRepository {
    // Налаштовуємо Gson для красивого форматування (відступи, нові рядки)
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun exportToFile(persons: List<Person>, file: File) {
        // Завдяки поліморфізму, Gson автоматично серіалізує всі специфічні
        // поля класів-нащадків (course, salary тощо), навіть якщо список має тип List<Person>
        val jsonString = gson.toJson(persons)
        file.writeText(jsonString)
    }
}