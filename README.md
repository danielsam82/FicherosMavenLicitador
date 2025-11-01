# Licitador

This project is a Java Swing application designed to help bidders prepare their documentation for a tender. It allows users to fill in their data, select the lots they want to participate in, upload the required files, and generate a compressed file with all the necessary documentation.

## Setup

To run this project, you need to have Java 8 or higher installed. You can compile and run the application from the command line or using an IDE like IntelliJ IDEA or Eclipse.

### Command Line

1.  Compile the source code:
    ```
    javac -d out src/main/java/com/licitador/ui/MainWindow.java
    ```
2.  Run the application:
    ```
    java -cp out com.licitador.ui.MainWindow
    ```

### IDE

1.  Open the project in your IDE.
2.  Locate the `MainWindow.java` file in `src/main/java/com/licitador/ui`.
3.  Run the `main` method.

## Usage

When you run the application, you will be prompted to either load an existing session or start a new one.

### New Session

If you start a new session, you will need to fill in your data, such as your company name, tax ID, and contact information. If the tender has lots, you will also need to select the lots you want to participate in.

### Loading Files

Once you have started a session, you can load the required files for the tender. The application will guide you through the process of uploading common files and offer files for each lot.

### Confidentiality

If a file is confidential, you can mark it as such and provide a justification. The application will generate a separate text file with the confidentiality declaration.

### Compressing Files

When you have uploaded all the necessary files, you can compress them into a single ZIP file. The application will verify that all mandatory files have been loaded and will generate a ZIP file with the correct folder structure.

### Saving and Loading Sessions

You can save your progress at any time by clicking the "Save Session" button. This will create a `.dat` file with all the data you have entered. You can then load this file later to continue your work.
