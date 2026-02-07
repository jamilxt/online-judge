# 🐳 Docker Setup Guide for Beginners

This guide will help you set up Docker to run code safely in isolated containers. Even if you've never used Docker before, just follow these steps!

---

## 📖 What is Docker?

Think of Docker like a **virtual machine**, but much lighter and faster. When you run code in Docker:

- ✅ The code runs in a separate "container" (like a mini computer inside your computer)
- ✅ The code can't access your files or network
- ✅ If something goes wrong, it only affects the container, not your system
- ✅ It's the safest way to run untrusted code

---

## 📥 Step 1: Install Docker

### 🪟 Windows

1. **Download Docker Desktop**
   - Go to: https://www.docker.com/products/docker-desktop/
   - Click **"Download for Windows"**

2. **Run the installer**
   - Double-click `Docker Desktop Installer.exe`
   - Follow the installation wizard
   - When asked, make sure **"Use WSL 2"** is checked ✅

3. **Restart your computer**
   - Docker needs a restart to complete installation

4. **Start Docker Desktop**
   - Look for the Docker whale icon 🐳 in your system tray
   - Wait until it says **"Docker is running"**

5. **Verify installation**
   - Open Command Prompt (search "cmd" in Start menu)
   - Type: `docker --version`
   - You should see something like: `Docker version 24.0.6`

### 🐧 Linux (Ubuntu/Debian)

Open your terminal and run these commands one by one:

```bash
# 1. Update package list
sudo apt update

# 2. Install required packages
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# 3. Add Docker's official GPG key
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 4. Add Docker repository
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. Install Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io

# 6. Add yourself to docker group (so you don't need sudo)
sudo usermod -aG docker $USER

# 7. Log out and log back in, or run:
newgrp docker

# 8. Verify installation
docker --version
```

### 🍎 macOS

1. **Download Docker Desktop**
   - Go to: https://www.docker.com/products/docker-desktop/
   - Click **"Download for Mac"**
   - Choose **Apple Silicon** (M1/M2/M3 Macs) or **Intel Chip**

2. **Install**
   - Open the `.dmg` file
   - Drag Docker to your Applications folder

3. **Start Docker**
   - Open Docker from Applications
   - Wait for the whale icon in your menu bar to stop animating

4. **Verify installation**
   - Open Terminal
   - Type: `docker --version`

---

## ✅ Step 2: Test Docker is Working

Run this command to test Docker:

```bash
docker run hello-world
```

**Expected output:**
```
Hello from Docker!
This message shows that your installation appears to be working correctly.
...
```

If you see this message, Docker is working! 🎉

---

## 🖼️ Step 3: Pull the Required Images

The Online Judge uses these Docker images for different programming languages. Let's download them now (this only needs to be done once):

```bash
# Python
docker pull python:3.9-slim

# Java
docker pull openjdk:17-slim

# C/C++
docker pull gcc:latest

# JavaScript
docker pull node:18-slim
```

**Tip:** This downloads about 1.5 GB total. You only need to do this once!

---

## ⚙️ Step 4: Configure CodeJudge to Use Docker

1. **Open the configuration file:**
   ```
   online-judge/src/main/resources/application.yml
   ```

2. **Find this section and change `local` to `docker`:**
   ```yaml
   executor:
     mode: docker    # Change from 'local' to 'docker'
   ```

3. **Save the file**

4. **Restart the application:**
   ```bash
   mvn spring-boot:run
   ```

5. **Look for this in the logs:**
   ```
   JudgeService initialized with executor: DOCKER
   ```
   This confirms Docker mode is active! ✅

---

## 🧪 Step 5: Test Code Execution

1. Open http://localhost:8081 in your browser
2. Click on any problem (like "Two Sum")
3. Submit the default Python code
4. Check the result - it should show **ACCEPTED** ✅

**Behind the scenes, your code just ran inside a Docker container!**

---

## 🔍 Troubleshooting

### "Docker is not running"

**Windows:**
- Look for the Docker whale 🐳 in your system tray
- If it's not there, search "Docker Desktop" in Start menu and open it
- Wait for it to say "Docker is running"

**Linux:**
```bash
# Start Docker service
sudo systemctl start docker

# Make it start automatically on boot
sudo systemctl enable docker
```

**macOS:**
- Open Docker from your Applications folder
- Wait for the whale icon in your menu bar

---

### "Permission denied" (Linux)

```bash
# Add yourself to docker group
sudo usermod -aG docker $USER

# Then either restart your system, or run:
newgrp docker
```

---

### "Cannot connect to Docker daemon"

**Windows:**
- Make sure Docker Desktop is running
- Try restarting Docker Desktop

**Linux:**
```bash
# Check if Docker is running
sudo systemctl status docker

# If not running, start it
sudo systemctl start docker
```

---

### "Image not found" error

Pull the required images manually:

```bash
docker pull python:3.9-slim
docker pull openjdk:17-slim
docker pull gcc:latest
docker pull node:18-slim
```

---

### "WSL 2 installation incomplete" (Windows)

1. Open PowerShell as Administrator
2. Run: `wsl --install`
3. Restart your computer
4. Open Docker Desktop again

---

## 📊 How Much Resources Does Docker Use?

| Resource | Usage | Notes |
|----------|-------|-------|
| Disk Space | ~1.5 GB | For all language images |
| Memory | ~2 GB | While Docker is running |
| CPU | Low | Only when executing code |

**Tip:** You can close Docker Desktop when not using the Online Judge to save memory.

---

## 🔄 Switching Back to Local Mode

If you want to stop using Docker:

1. Edit `application.yml`
2. Change `mode: docker` to `mode: local`
3. Restart the application

---

## 📚 Learn More About Docker

If you want to learn more about Docker:
- [Docker Getting Started](https://docs.docker.com/get-started/)
- [Docker Crash Course (YouTube)](https://www.youtube.com/watch?v=pg19Z8LL06w)
- [Play with Docker (free online environment)](https://labs.play-with-docker.com/)

---

## 🎉 You're All Set!

Now you have Docker running and your code executes in secure, isolated containers. This is the same technology used by:
- LeetCode
- HackerRank
- Real interview platforms

Happy coding! 🚀
