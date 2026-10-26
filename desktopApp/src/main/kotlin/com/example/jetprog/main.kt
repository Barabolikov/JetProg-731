package com.example.jetprog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
enum class AppScreen { LIST, ADD_USER }

enum class UserRole(val title: String) {
    STUDENT("Студент"),
    TEACHER("Викладач")
}

fun main() = application {
    var currentScreen by remember { mutableStateOf(AppScreen.LIST) }

    // Стан для збереження об'єкта, який ми зараз редагуємо
    var personToEdit by remember { mutableStateOf<Person?>(null) }
    val JsonRepository = remember { JsonRepository() } // І
    val repository = remember { MySqlRepository() }
    val personsList = remember { mutableStateListOf<Person>().apply { addAll(repository.loadAll()) } }

    Window(onCloseRequest = ::exitApplication, title = "University Manager - MySQL CRUD") {
        MenuBar {
            Menu("Файл", mnemonic = 'Ф') {
                Item("Експортувати в JSON", onClick = {
                    // 1. Відкриваємо діалог вибору місця збереження
                    val fileToSave = showSaveFileDialog()

                    // 2. Якщо файл обрано, записуємо туди поточний список
                    if (fileToSave != null) {

                        JsonRepository.exportToFile(personsList, fileToSave)
                        println("Успішно збережено в: ${fileToSave.absolutePath}")
                    }
                })
                Separator()
                Item("Вихід", onClick = ::exitApplication, shortcut = KeyShortcut(Key.Q, ctrl = true))
            }
            Menu("Навігація", mnemonic = 'Н') {
                Item("Список користувачів", onClick = { currentScreen = AppScreen.LIST })
                Item("Додати користувача", onClick = {
                    personToEdit = null // При додаванні нового очищаємо форму
                    currentScreen = AppScreen.ADD_USER
                })
            }
        }

        MaterialTheme {
            when (currentScreen) {
                AppScreen.LIST -> PersonsListScreen(
                    persons = personsList,
                    onDelete = { person ->
                        repository.deletePerson(person.id)
                        personsList.remove(person)
                    },
                    onEdit = { person ->
                        personToEdit = person // Передаємо дані користувача у форму
                        currentScreen = AppScreen.ADD_USER
                    }
                )
                AppScreen.ADD_USER -> AddUserScreen(
                    initialPerson = personToEdit, // Передаємо об'єкт для редагування (якщо він є)
                    onUserSaved = { savedPerson ->
                        if (personToEdit == null) {
                            // Створення нового
                            repository.addPerson(savedPerson)
                            personsList.add(savedPerson)
                        } else {
                            // Оновлення існуючого
                            repository.updatePerson(savedPerson)
                            val index = personsList.indexOfFirst { it.id == savedPerson.id }
                            if (index != -1) personsList[index] = savedPerson
                        }
                        personToEdit = null
                        currentScreen = AppScreen.LIST
                    },
                    onCancel = {
                        personToEdit = null
                        currentScreen = AppScreen.LIST
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonsListScreen(
    persons: List<Person>,
    onDelete: (Person) -> Unit,
    onEdit: (Person) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Всі", "Студенти", "Викладачі")
    var personToDelete by remember { mutableStateOf<Person?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) }
                )
            }
        }

        val filteredPersons = when (selectedTabIndex) {
            1 -> persons.filterIsInstance<Student>()
            2 -> persons.filterIsInstance<Teacher>()
            else -> persons
        }

        if (filteredPersons.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("У цій категорії поки немає записів", color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPersons) { person ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = person.getDisplayInfo(), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Email: ${person.email}", style = MaterialTheme.typography.body2)
                            }
                            Row {
                                IconButton(onClick = { onEdit(person) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Редагувати", tint = Color.Blue)
                                }
                                IconButton(onClick = { personToDelete = person }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Видалити", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    personToDelete?.let { person ->
        AlertDialog(
            onDismissRequest = { personToDelete = null },
            title = { Text("Видалення користувача") },
            text = { Text("Ви дійсно хочете видалити ${person.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(person)
                    personToDelete = null
                }) {
                    Text("Видалити", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { personToDelete = null }) {
                    Text("Скасувати")
                }
            }
        )
    }
}

@Composable
fun AddUserScreen(
    initialPerson: Person? = null,
    onUserSaved: (Person) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialPerson?.name ?: "") }
    var email by remember { mutableStateOf(initialPerson?.email ?: "") }
    var selectedRole by remember {
        mutableStateOf(if (initialPerson is Teacher) UserRole.TEACHER else UserRole.STUDENT)
    }

    var course by remember { mutableStateOf((initialPerson as? Student)?.course?.toString() ?: "") }
    var group by remember { mutableStateOf((initialPerson as? Student)?.group ?: "") }
    var subject by remember { mutableStateOf((initialPerson as? Teacher)?.subject ?: "") }
    var salary by remember { mutableStateOf((initialPerson as? Teacher)?.salary?.toString() ?: "") }

    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val titleText = if (initialPerson == null) "Реєстрація користувача" else "Редагування користувача"
        Text(titleText, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
        Divider()

        Row(modifier = Modifier.selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
            Text("Тип:", modifier = Modifier.padding(end = 16.dp))
            UserRole.values().forEach { role ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (selectedRole == role),
                        onClick = { selectedRole = role },
                        enabled = initialPerson == null // Забороняємо змінювати тип при редагуванні
                    )
                    Text(role.title, modifier = Modifier.padding(end = 16.dp))
                }
            }
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("ПІБ") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())

        if (selectedRole == UserRole.STUDENT) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Курс (число)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = group, onValueChange = { group = it }, label = { Text("Група") }, modifier = Modifier.weight(1f))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Предмет викладання") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Зарплата") }, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onCancel) { Text("Скасувати") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                enabled = name.isNotBlank() && email.isNotBlank(),
                onClick = { showConfirmDialog = true }
            ) {
                Text("Зберегти")
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Підтвердження") },
            text = { Text("Зберегти зміни для $name?") },
            confirmButton = {
                TextButton(onClick = {
                    val idToSave = initialPerson?.id ?: (1000..9999).random()
                    val newPerson = if (selectedRole == UserRole.STUDENT) {
                        Student(idToSave, name, email, course.toIntOrNull() ?: 1, group)
                    } else {
                        Teacher(idToSave, name, email, subject, salary.toDoubleOrNull() ?: 0.0)
                    }
                    showConfirmDialog = false
                    onUserSaved(newPerson)
                }) { Text("Зберегти") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Відміна") }
            }
        )
    }
}

// Допоміжна функція для виклику нативного вікна збереження файлу
fun showSaveFileDialog(): File? {
    val dialog = FileDialog(null as Frame?, "Експорт у JSON", FileDialog.SAVE).apply {
        file = "university_data.json" // Назва файлу за замовчуванням
        isVisible = true
    }

    // Якщо користувач натиснув "Скасувати"
    if (dialog.directory == null || dialog.file == null) return null

    return File(dialog.directory, dialog.file)
}