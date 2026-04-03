import java.util.ArrayList;
void main() {
    Task task1 = new Task("Wake up", "General", "Get out of bed", 1);
    Task task2 = new Task("Make breakfast", "General", 2);
    Task task3 = new Task("Go to School", "Going", "Car to school", 2);

    ToDos toDo = new ToDos();
    toDo.addTask(task1);
    toDo.addTask(task2);
    toDo.addTask(task3);

    toDo.sort("P");
    toDo.outputTasks();

    toDo.sort("L");
    toDo.outputTasks();

    toDo.removeTask("Go to School");
    toDo.outputTasks();

    toDo.sort("P");
    toDo.outputTasks();


}
public class Task {
    private String title;
    private String label;
    private String description;
    private Integer priority;

    // No description
    public Task(String title, String label, Integer priority) {
        this(title, label, "", priority);
    }

    // The "main" constructor
    public Task(String title, String label, String description, Integer priority) {
        this.title = title;
        this.label = label;
        this.description = description;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getLabel() {
        return label;
    }
    public Integer getPriority() {
        return priority;
    }
}

public class ToDos {
    ArrayList<Task> tasks = new ArrayList<>();
    String ordering = "P"; // P for priority, L for label alphabetically

    public void addTask(Task newTask) {
        tasks.add(newTask);
    }

    public void addTask(String title, String label, String description, Integer priority) {
        addTask(new Task(title,description, label, priority));
    }

    public void addTask(String title, String label, Integer priority) {
        addTask(new Task(title, label, "", priority));
    }

    public void removeTask(String title) { // removes first task with given title
        for (Task task : tasks) {
            if (task.getTitle().equalsIgnoreCase(title)) {
                tasks.remove(task);
                break;
            }
        }
    }

    public void sort(String order) {
        if (order.equalsIgnoreCase(ordering)) {
            return;
        }
        else {
            ordering = order;
            sortAlgorithm(ordering);
        }
    }

    private void sortAlgorithm(String sortBy) {
        int n = tasks.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                boolean shouldSwap;
                if (sortBy.equalsIgnoreCase("P")) {
                    shouldSwap = tasks.get(j).getPriority() > tasks.get(j + 1).getPriority();
                } else {
                    shouldSwap = tasks.get(j).getLabel().compareToIgnoreCase(tasks.get(j + 1).getLabel()) > 0;
                }
                if (shouldSwap) {
                    Task temp = tasks.get(j);
                    tasks.set(j, tasks.get(j + 1));
                    tasks.set(j + 1, temp);
                }
            }
        }
    }

    public void outputTasks() {
        for (Task t : tasks) {
            System.out.println(t.getTitle() + " - " + t.getLabel() + " - Priority: " + t.getPriority());
        }
        System.out.println("\n\n\n");
    }
}
