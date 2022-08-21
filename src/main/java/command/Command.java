package command;

import exception.DukeException;
import parser.DateTimeParser;
import task.*;
import ui.Ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Command {
    private CommandType commandType;
    private TaskList tasks;
    private String input;
    private Ui ui;

    public Command(CommandType commandType, TaskList tasks, String input, Ui ui) {
        this.commandType = commandType;
        this.tasks = tasks;
        this.input = input;
        this.ui = ui;
    }

    private TaskList getMatchingTasks(String keyword) {
        TaskList newTaskList = new TaskList();
        for (int i = 0; i < tasks.getSize(); i++) {
            if (tasks.containsKeyword(i, keyword)) {
                newTaskList.add(tasks.get(i));
            }
        }
        return newTaskList;
    }

    private void addTask(Task input) {
        tasks.add(input);
    }

    private LocalDateTime stringToLocalDateTime(String deadline) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy hh:mm a");
        LocalDateTime deadlineDateTime = LocalDateTime.parse(deadline, formatter);
        return deadlineDateTime;
    }

    private boolean hasTaskIndex(int taskIndex) {
        return taskIndex - 1 < this.tasks.getSize() && taskIndex - 1 >= 0;
    }

    /**
     * Returns a boolean describing whether the CommandType is a "Bye" Command.
     *
     * @return boolean describing whether the CommandType is a "Bye" Command.
     */
    public boolean isExit() {
        return commandType == CommandType.BYE;
    }

    /**
     * Executes the Command based on the CommandType.
     *
     * @throws DukeException If an exception is encountered or if
     * an the input is invalid.
     */
    public void execute() throws DukeException {
        switch (this.commandType) {
        case BYE:
            break;
        case LIST:
            ui.printTasks(tasks, "Boss ah, this one your tasks:",
                    "Boss, you got no task yet ah");
            break;
        case TODO:
            String todoDescription = input.substring(4).trim();
            if (todoDescription.equals("")) {
                throw new DukeException("Eh your task cannot be empty lah!");
            } else {
                Todo todo = new Todo(todoDescription, false);
                this.addTask(todo);
                ui.printAddedTaskMessage(todo);
                ui.printTaskCountMessage(tasks);
            }
            break;
        case EVENT:
            String[] splittedEvent = input.substring(5).split(" /at ", 3);
            boolean isEmptyEvent = splittedEvent.length == 2 && splittedEvent[0].trim().equals("");
            boolean isIncorrectEventRange = splittedEvent.length != 2 || splittedEvent[0].trim().equals("")
                    || splittedEvent[1].trim().equals("");
            if (isEmptyEvent) {
                throw new DukeException("Eh you never added your task name");
            } else if (isIncorrectEventRange) {
                throw new DukeException("Eh you never added the event range");
            } else {
                LocalDateTime eventDateTime = DateTimeParser.changeStringToParsingDateTime(splittedEvent[1].trim());
                Event event = new Event(splittedEvent[0].trim(),false, eventDateTime);
                this.addTask(event);
                ui.printAddedTaskMessage(event);
                ui.printTaskCountMessage(tasks);
            }
            break;
        case DEADLINE:
            String[] splittedDeadline = input.substring(8).split(" /by ", 3);
            boolean isEmptyDeadline = splittedDeadline.length == 2 && splittedDeadline[0].trim().equals("");
            boolean isIncorrectDeadlineDate = splittedDeadline.length != 2 || splittedDeadline[0].trim().equals("")
                    || splittedDeadline[1].trim().equals("");
            if (isEmptyDeadline) {
                throw new DukeException("Eh you never added your task name");
            } else if (isIncorrectDeadlineDate) {
                throw new DukeException("Eh you never added a deadline");
            } else {
                try {
                    LocalDateTime deadlineDateTime = stringToLocalDateTime(splittedDeadline[1].trim());
                    Deadline deadline = new Deadline(splittedDeadline[0].trim(), false, deadlineDateTime);
                    this.addTask(deadline);
                    ui.printAddedTaskMessage(deadline);
                    ui.printTaskCountMessage(tasks);
                } catch (DateTimeParseException e) {
                    throw new DukeException("Eh you never add a proper deadline date! \n" +
                            "Your deadline date should be like this lah: Jan 21 2023 04:10 AM");
                }
            }
            break;
        case EMPTY:
            throw new DukeException("Eh you never type anything leh?");
        case MARK:
            String markIndexString = input.substring(4).trim();
            try {
                int taskIndex = Integer.parseInt(markIndexString);
                if (!hasTaskIndex(taskIndex)) {
                    throw new DukeException("Eh, you got that task number meh?");
                }
                if (this.tasks.get(taskIndex - 1).canChangeIsDone(true)) {
                    this.tasks.get(taskIndex - 1).changeIsDone(true);
                    ui.printMarkedMessage(this.tasks.get(taskIndex - 1));
                } else {
                    throw new DukeException("Eh, you done that task alr lah");
                }
            }
            catch (NumberFormatException e) {
                throw new DukeException("Eh, you enter your task number correctly anot?");
            }
            break;
        case UNMARK:
            String unmarkIndexString = input.substring(6).trim();
            try {
                int taskIndex = Integer.parseInt(unmarkIndexString);
                if (!hasTaskIndex(taskIndex)) {
                    throw new DukeException("Eh, you got that task number meh?");
                }
                if (this.tasks.get(taskIndex - 1).canChangeIsDone(false)) {
                    this.tasks.get(taskIndex - 1).changeIsDone(false);
                    ui.printUnmarkedMessage(this.tasks.get(taskIndex - 1));
                } else {
                    throw new DukeException("Eh, your task alr not done lah");
                }
            }
            catch (NumberFormatException e) {
                throw new DukeException("Eh, you enter your task number correctly anot?");
            }
            break;
        case DELETE:
            String taskIndexToDelete = input.substring(6).trim();
            try {
                int taskIndex = Integer.parseInt(taskIndexToDelete);
                if (!hasTaskIndex(taskIndex)) {
                    throw new DukeException("Eh, you got that task number meh?");
                } else {
                    ui.printDeletedTaskMessage(tasks.taskStringAtIndex(taskIndex - 1));
                    tasks.remove(taskIndex - 1);
                }
            }
            catch (NumberFormatException e) {
                throw new DukeException("Eh, you enter your task number correctly anot?");
            }
            break;
        case FIND:
            String keyword = input.substring(4).trim();
            TaskList newTaskList = getMatchingTasks(keyword);
            ui.printTasks(newTaskList, "Boss ah, this one all your tasks matching '" + keyword + "' : ",
                    "Boss, no matching tasks ah");
            if (keyword.equals("")) {
                throw new DukeException("Eh your keyword cannot be empty lah!");
            }

        default:
            throw new DukeException("What talking you");
        }
    }
}