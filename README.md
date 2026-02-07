# 🚀 CodeJudge - Online Judge System

A simple online judge system where you can solve programming problems and get your code automatically evaluated. Think of it like LeetCode or HackerRank, but running on your own computer!

![CodeJudge Homepage](src/main/resources/static/screenshot-placeholder.png)

---

## 📖 What is an Online Judge?

An **online judge** is a system that:
1. Shows you a programming problem (like "add two numbers")
2. Lets you write code to solve it
3. Runs your code against test cases
4. Tells you if your solution is correct ✅ or wrong ❌

---

## 🎯 What Can You Do With This?

- ✅ Browse programming problems (Easy, Medium difficulty)
- ✅ Write code in **Python, Java, C++, JavaScript, or C**
- ✅ Submit your code and see instant results
- ✅ See which test cases passed or failed
- ✅ Practice coding in a beautiful dark-themed editor

---

## 🛠️ Requirements

Before you start, make sure you have these installed:

### Must Have:
| Software | Why You Need It | How to Check |
|----------|-----------------|--------------|
| **Java 17+** | Runs the server | `java -version` |
| **Maven** | Builds the project | `mvn -version` |

### For Running Code (at least one):

**Option A: Local Mode (easier to set up)**
| Language | Command to Install (Ubuntu/Debian) |
|----------|-----------------------------------|
| Python 3 | `sudo apt install python3` |
| Java | Already installed with Java 17 |
| C/C++ | `sudo apt install gcc g++` |
| Node.js | `sudo apt install nodejs` |

**Option B: Docker Mode (more secure)**
| Software | How to Install |
|----------|----------------|
| Docker | [Install Docker](https://docs.docker.com/get-docker/) |

---

## 🚀 Quick Start (5 minutes)

### Step 1: Open Terminal
Navigate to the project folder:
```bash
cd /path/to/online-judge
```

### Step 2: Run the Application
```bash
mvn spring-boot:run
```

Wait until you see:
```
Started OnlineJudgeApplication in X seconds
Initialized 5 sample problems
```

### Step 3: Open Your Browser
Go to: **http://localhost:8081**

### Step 4: Solve a Problem!
1. Click on any problem (like "Two Sum")
2. Write your code in the editor
3. Click **Submit**
4. See if you got **ACCEPTED** ✅

---

## ⚙️ Configuration

The main configuration file is `src/main/resources/application.yml`.

### Changing the Execution Mode

Find this section in the file:

```yaml
executor:
  mode: local    # Change to 'docker' for Docker mode
```

| Mode | When to Use |
|------|-------------|
| `local` | **Default.** Code runs directly on your computer. Faster but less secure. |
| `docker` | Code runs inside Docker containers. Slower but isolated and safer. |

### Changing the Port

The app runs on port **8081** by default. To change it:

```yaml
server:
  port: 8081    # Change this number
```

---

## 📁 Project Structure (Simplified)

```
online-judge/
├── src/main/java/com/onlinejudge/
│   ├── OnlineJudgeApplication.java  # Main entry point
│   ├── controller/                   # Handles web requests
│   ├── service/                      # Business logic
│   │   ├── LocalCodeExecutor.java   # Runs code locally
│   │   └── DockerCodeExecutor.java  # Runs code in Docker
│   ├── model/                        # Data structures
│   └── repository/                   # Database access
│
├── src/main/resources/
│   ├── application.yml               # Configuration
│   └── static/                       # Frontend files
│       ├── index.html               # Homepage
│       ├── problem.html             # Problem page
│       └── css/styles.css           # Dark theme styling
│
└── pom.xml                           # Project dependencies
```

---

## 🧪 Sample Problems Included

The app comes with 5 practice problems:

| # | Problem | Difficulty | Description |
|---|---------|------------|-------------|
| 1 | Two Sum | Easy | Add two numbers |
| 2 | Palindrome Check | Easy | Check if a string reads the same forwards and backwards |
| 3 | FizzBuzz | Easy | Classic programming exercise |
| 4 | Factorial | Medium | Calculate n! |
| 5 | Prime Number Check | Medium | Determine if a number is prime |

---

## 💻 Supported Languages

| Language | Version | File Naming |
|----------|---------|-------------|
| Python | 3.x | `solution.py` |
| Java | 17 | `Main.java` (class must be named `Main`) |
| C++ | GCC | `solution.cpp` |
| JavaScript | Node.js | `solution.js` |
| C | GCC | `solution.c` |

---

## 🔒 Security Notes

### Local Mode
- ⚠️ Code runs directly on your machine
- ⚠️ Not recommended for untrusted code
- ✅ Great for personal practice

### Docker Mode
- ✅ Code runs in isolated containers
- ✅ Network is disabled (code can't access internet)
- ✅ Memory and CPU limits applied
- ✅ Safer for running untrusted code

---

## ❓ Troubleshooting

### "Port 8081 already in use"
```bash
# Kill the process using port 8081
fuser -k 8081/tcp

# Then try again
mvn spring-boot:run
```

### "python3: command not found"
```bash
# Install Python
sudo apt install python3
```

### "Docker: permission denied"
```bash
# Add yourself to docker group
sudo usermod -aG docker $USER
# Then log out and log back in
```

### "Compilation Error" when submitting Java
Make sure your class is named `Main`:
```java
public class Main {  // ✅ Correct
    public static void main(String[] args) {
        // your code
    }
}
```

---

## 🤝 How It Works (Simple Explanation)

```
You write code    →    Server receives it    →    Code runs in sandbox
      ↑                                                    ↓
      └────────────────  Result sent back  ←──────────────┘
```

1. **You submit code** through the web interface
2. **Server saves** your submission to the database
3. **Code executor** (local or Docker) runs your code
4. Your code gets **test inputs** and produces **outputs**
5. **Outputs are compared** with expected results
6. You see **ACCEPTED** if all tests pass, or an error otherwise

---

## 📚 Learning Resources

If you're new to these technologies:

- [Spring Boot Tutorial](https://spring.io/guides/gs/spring-boot/)
- [Java Basics](https://dev.java/learn/)
- [Docker Getting Started](https://docs.docker.com/get-started/)
- [HTML/CSS Basics](https://developer.mozilla.org/en-US/docs/Learn)

---

## 📝 License

This is a learning project. Feel free to use, modify, and share!

---

## 🙋 Need Help?

If something doesn't work:
1. Check the **Troubleshooting** section above
2. Look at the terminal for error messages
3. Make sure all requirements are installed

Happy Coding! 🎉
