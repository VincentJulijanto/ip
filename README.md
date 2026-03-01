# Bit – Personal Task Manager

```
 ____   ___  _____
| __ ) |_ _||_   _|
|  _ \  | |   | |
| |_) | | |   | |
|____/ |___|  |_|
```

Bit is a lightweight command-line task manager that helps users organise their tasks quickly and efficiently.
Bit is designed for users who prefer quick command-based task management.

It supports managing todos, deadlines, and events through simple commands, making it ideal for users who prefer fast keyboard-based interaction.

---

## Features

- Add **Todo** tasks
- Add **Deadline** tasks with date and time
- Add **Event** tasks with start and end time
- View all tasks using `list`
- Mark or unmark tasks as completed
- Delete tasks
- Update existing tasks
- Find tasks using keywords
- Tasks are automatically saved and loaded between runs

---

## Setting up in IntelliJ

Prerequisites: **JDK 17** and the latest version of **IntelliJ IDEA**.

1. Open IntelliJ
2. Click `File` → `Open`
3. Select the project folder
4. Configure the project to use **JDK 17**
5. Locate the file:

`src/main/java/bit/Launcher.java`

6. Right-click the file and select:

`Run Launcher.main()`

If the setup is correct, the program will start in the terminal window.

---

## Example Commands

```
todo read book
deadline submit report /by 2026-03-01 2359
event meeting /from 2026-03-02 1400 /to 2026-03-02 1600
list
mark 2
unmark 2
delete 3
update 1 new description
find book
bye
```

---

## Data Storage

Tasks are automatically saved to a local data file and will be loaded again when the program starts.

---

## Project Structure

```
src
 └─ main
     └─ java
         └─ bit
             ├─ task
             │   ├─ Task
             │   ├─ Todo
             │   ├─ Deadline
             │   └─ Event
             ├─ Storage
             ├─ Ui
             ├─ Bit
             ├─ Main
             └─ bit.Launcher
```

---

## Notes

- The `src/main/java` folder must remain the root directory for Java files.
- The project is built using **Java 17**.