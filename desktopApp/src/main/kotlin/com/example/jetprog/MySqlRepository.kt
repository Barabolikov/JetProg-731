package com.example.jetprog

import java.sql.DriverManager
import java.sql.Statement

class MySqlRepository {
    // Налаштуйте під ваші параметри локального сервера
    private val url = "jdbc:mysql://localhost:3306/university_db?serverTimezone=UTC"
    private val user = "root"
    private val password = ""

    init {
        initDatabase()
    }

    private fun initDatabase() {
        DriverManager.getConnection(url, user, password).use { conn ->
            val statement = conn.createStatement()
            val createTableSql = """
                CREATE TABLE IF NOT EXISTS persons (
                    id INT PRIMARY KEY,
                    role VARCHAR(50) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    course INT NULL,
                    group_name VARCHAR(100) NULL,
                    subject VARCHAR(255) NULL,
                    salary DOUBLE NULL
                )
            """.trimIndent()
            statement.execute(createTableSql)
        }
    }

    fun loadAll(): List<Person> {
        val persons = mutableListOf<Person>()
        DriverManager.getConnection(url, user, password).use { conn ->
            val statement = conn.createStatement()
            val rs = statement.executeQuery("SELECT * FROM persons")
            while (rs.next()) {
                val id = rs.getInt("id")
                val name = rs.getString("name")
                val email = rs.getString("email")
                val role = rs.getString("role")

                if (role == "Student") {
                    persons.add(
                        Student(id, name, email, rs.getInt("course"), rs.getString("group_name"))
                    )
                } else if (role == "Teacher") {
                    persons.add(
                        Teacher(id, name, email, rs.getString("subject"), rs.getDouble("salary"))
                    )
                }
            }
        }
        return persons
    }

    fun addPerson(person: Person) {
        DriverManager.getConnection(url, user, password).use { conn ->
            val sql = if (person is Student) {
                "INSERT INTO persons (id, role, name, email, course, group_name) VALUES (?, 'Student', ?, ?, ?, ?)"
            } else {
                "INSERT INTO persons (id, role, name, email, subject, salary) VALUES (?, 'Teacher', ?, ?, ?, ?)"
            }

            val pstmt = conn.prepareStatement(sql)
            pstmt.setInt(1, person.id)
            pstmt.setString(2, person.name)
            pstmt.setString(3, person.email)

            if (person is Student) {
                pstmt.setInt(4, person.course)
                pstmt.setString(5, person.group)
            } else if (person is Teacher) {
                pstmt.setString(4, person.subject)
                pstmt.setDouble(5, person.salary)
            }
            pstmt.executeUpdate()
        }
    }
    fun deletePerson(id: Int) {
        DriverManager.getConnection(url, user, password).use { conn ->
            val pstmt = conn.prepareStatement("DELETE FROM persons WHERE id = ?")
            pstmt.setInt(1, id)
            pstmt.executeUpdate()
        }
    }
    fun updatePerson(person: Person) {
        DriverManager.getConnection(url, user, password).use { conn ->
            val sql = if (person is Student) {
                "UPDATE persons SET name = ?, email = ?, course = ?, group_name = ? WHERE id = ?"
            } else {
                "UPDATE persons SET name = ?, email = ?, subject = ?, salary = ? WHERE id = ?"
            }

            val pstmt = conn.prepareStatement(sql)
            pstmt.setString(1, person.name)
            pstmt.setString(2, person.email)

            if (person is Student) {
                pstmt.setInt(3, person.course)
                pstmt.setString(4, person.group)
            } else if (person is Teacher) {
                pstmt.setString(3, person.subject)
                pstmt.setDouble(4, person.salary)
            }
            // ID завжди останній параметр у нашому запиті
            pstmt.setInt(5, person.id)
            pstmt.executeUpdate()
        }
    }
}