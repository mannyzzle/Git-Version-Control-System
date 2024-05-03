# Git-Version-Control-System

**Introduction**

This project implements a version control system similar to Git, allowing you to track changes in your codebase over time. It provides functionalities for:

- Creating a new Git repository
- Staging changes for commit
- Committing changes with descriptive messages
- Viewing commit history

**Features**

- **Version Tracking:** Captures snapshots of your codebase at specific points in time.
- **Change Tracking:** Identifies modifications made to files since the last commit.
- **Commit History:** Provides a record of all commits, including the author, date, and message.

**Installation and Usage**

**Prerequisites:**

- Java installed ([https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/))

**Building:**

1. Compile the source files using a Java compiler (e.g., `javac Main.java Repository.java Utils.java ...`).
2. (Optional) Create a JAR file for easier execution (e.g., `jar cvf gitlet.jar *.class`).

**Running:**

1. Navigate to the directory containing the compiled classes or JAR file.
2. Execute the main class:
   - Using compiled classes: `java Main <command> [arguments]`
   - Using JAR file: `java -jar gitlet.jar <command> [arguments]`
   - Replace `<command>` with a valid Gitlet command (e.g., `init`, `add`, `commit`, `log`).
   - Refer to the specific command documentation for required arguments.

**Example Usage**

1. Create a new Gitlet repository: `java Main init`
2. Add a file to the staging area: `java Main add filename.txt`
3. Commit the changes with a message: `java Main commit "Added filename.txt"`
4. View the commit history: `java Main log`

**Contributing**

If you'd like to contribute to this project, please follow these steps:

1. Fork the repository 
2. Clone your forked repository to your local machine.
3. Make changes and write unit tests for your modifications.
4. Create a pull request to merge your changes into the main repository.

This project is for educational purposes and may not be fully comprehensive compared to a professional Git implementation. Use it at your own risk for personal projects.
