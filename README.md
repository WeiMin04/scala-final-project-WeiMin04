# 🧾 Address Book Management System (ScalaFX)

## 📌 Project Overview
This project is a **GUI-based Address Book Management System** developed using **ScalaFX** as part of the Object-Oriented Programming (OOP) course.

The application allows users to manage personal records, including adding, editing, viewing, and deleting contact information through an intuitive graphical interface.

This project demonstrates the application of **OOP concepts**, **MVC architecture**, and **database integration**.

---

## 🎯 Objectives
- Apply **Object-Oriented Programming (OOP)** concepts:
  - Inheritance
  - Polymorphism
  - Encapsulation
- Implement **MVC (Model-View-Controller)** architecture
- Develop a **GUI application** using ScalaFX
- Integrate **database operations** using JDBC (ScalikeJDBC)

---

## 🛠️ Technologies Used
- **Scala 3**
- **ScalaFX**
- **JavaFX (FXML + Scene Builder)** :contentReference[oaicite:0]{index=0}
- **SBT (Scala Build Tool)**
- **ScalikeJDBC**
- **Apache Derby / H2 Database**
- **IntelliJ IDEA**


---

## ⚙️ Features

### 👤 Contact Management
- Add new person
- Edit existing person
- Delete selected person
- View detailed information

### 📊 Table View
- Display list of contacts using TableView
- Real-time update using ObservableList :contentReference[oaicite:1]{index=1}

### 🧾 Form Validation
- Input validation for all fields
- Error alerts for invalid input

### 🗄️ Database Integration
- Save data to database
- Update and delete records
- Auto table creation on first run :contentReference[oaicite:2]{index=2}

### 🎨 UI Styling
- Custom **Dark Theme** using CSS
- Styled buttons, tables, and dialogs

---

## 🧩 Key Concepts Applied

### 📌 MVC Architecture
- **Model** → `Person.scala`
- **View** → FXML files
- **Controller** → Handles UI logic

### 📌 OOP Principles
- Encapsulation via properties
- Modular design with multiple classes
- Separation of concerns

### 📌 Event-Driven Programming
- Button click handling
- Table selection listeners
- Dialog interactions :contentReference[oaicite:3]{index=3}

---

## ▶️ How to Run the Project

1. Clone the repository:
```bash
git clone https://github.com/your-username/your-repo-name.git
