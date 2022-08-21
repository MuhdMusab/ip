package storage;

import exception.DukeException;
import parser.DateTimeParser;
import task.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Storage {
    private String folderPath;
    private String filename;
    private String userDirectory = System.getProperty("user.dir");

    public Storage(String folderPath, String filename) {
        this.folderPath = folderPath;
        this.filename = filename;
    }

    /**
     * Returns an ArrayList of Tasks containing the saved Tasks, if they exist.
     *
     * @return ArrayList of Tasks
     * @throws DukeException if an error is encountered when the file
     * is unable to be read.
     */
    public ArrayList<Task> readSavedTasks() throws DukeException {
        Path dataDirectoryPath = Paths.get(userDirectory, folderPath);
        Path savedTasksPath = Paths.get(userDirectory, folderPath, filename);
        if (!hasDataDirectory(dataDirectoryPath)) {
            createDataDirectory(dataDirectoryPath);
            return new ArrayList<>();
        }
        if (!hasSavedTasks(savedTasksPath)) {
            return new ArrayList<>();
        } else {
            File tasksFile = getTasksFile(savedTasksPath);
            ArrayList<Task> tasks = new ArrayList<>();
            try {
                tasks = parseFileToTasks(tasksFile);
            } catch (FileNotFoundException e) {
                throw new DukeException(e.getMessage());
            } finally {
                return tasks;
            }
        }
    }

    private File getTasksFile(Path savedTasksPath) {
        File tasksFile = new File(getAbsolutePath(savedTasksPath));
        return tasksFile;
    }

    private ArrayList<Task> parseFileToTasks(File tasksFile) throws FileNotFoundException, DukeException {
        Scanner sc = new Scanner(tasksFile);
        ArrayList<Task> tasks = new ArrayList<>();
        while (sc.hasNext()) {
            String taskDescription = sc.nextLine();
            tasks.add(parseLineToTask(taskDescription));
        }
        return tasks;
    }

    private Task parseLineToTask(String line) throws DukeException {
        if (line.length() <= 7) {
            throw new DukeException("Eh something wrong happened");
        }
        char taskSymbol = line.charAt(1);
        boolean isDone = line.charAt(4) == 'X'
                ? true
                : false;
        switch (taskSymbol) {
        case 'T':
            return getTodoFromLine(line);
        case 'D':
            return getTaskFromLine(line, isDone, " \\(by: ");
        case 'E':
            return getTaskFromLine(line, isDone, " \\(at: ");
        default:
            throw new DukeException("Eh something wrong happened");
        }
    }

    private String trimInputDateString(String str) {
        return str.substring(0, str.length() - 1);
    }

    private Task getTaskFromLine(String line, boolean isDone, String regex) throws DukeException {
        switch (regex) {
        case " \\(at: ":
            String[] splittedEvent = line.substring(7).split(" \\(at: ", 3);
            String trimmedInputEventDateString = trimInputDateString(splittedEvent[1]);
            LocalDateTime eventDateTime= DateTimeParser.changeStringToReadingDateTime(trimmedInputEventDateString);
            return new Event(splittedEvent[0], isDone, eventDateTime);
        case " \\(by: ":
            String[] splittedDeadline = line.substring(7).split(" \\(by: ", 3);
            String trimmedInputDeadlineDateString = trimInputDateString(splittedDeadline[1]);
            LocalDateTime deadlineDateTime = DateTimeParser.changeStringToReadingDateTime(trimmedInputDeadlineDateString);
            return new Deadline(splittedDeadline[0], isDone, deadlineDateTime);
        }
        throw new DukeException("Eh something wrong happened");
    }

    private Todo getTodoFromLine(String line) {
        return new Todo(line.substring(7), false);
    }

    private String getAbsolutePath(Path path) {
        return path.toAbsolutePath().toString();
    }

    private void createDataDirectory(Path dataDirectoryPath) throws DukeException {
        try {
            Files.createDirectories(dataDirectoryPath);
        } catch (IOException e) {
            throw new DukeException(e.getMessage());
        }
    }

    private boolean hasDataDirectory(Path dataDirectoryPath) {
        return Files.exists(dataDirectoryPath);
    }

    private boolean hasSavedTasks(Path savedTasksPath) {
        return Files.exists(savedTasksPath);
    }

    private String generateTasksToAdd(TaskList tasks) {
        StringBuilder stringBuilder = new StringBuilder();
        int tasksSize = tasks.getSize();
        for (int i = 0; i < tasksSize; i++) {
            stringBuilder.append(tasks.taskStringAtIndex(i) + System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    private String relativePath() {
        Path path = Paths.get(userDirectory, "data", "duke.txt");
        return path.toAbsolutePath().toString();
    }

    /**
     * Writes the saved tasks to the output file.
     *
     * @param tasks TaskList to be written from.
     * @throws DukeException if an exception is encountered.
     */
    public void writeToFile(TaskList tasks) throws DukeException {
        try {
            FileWriter fw = new FileWriter(relativePath());
            fw.write(generateTasksToAdd(tasks));
            fw.close();
        } catch (IOException e) {
            throw new DukeException(e.getMessage());
        }
    }
}
