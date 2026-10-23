import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 1. ENUMS
enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
enum Status { OPEN, IN_PROGRESS, RESOLVED, CLOSED }
enum Category { HARDWARE, SOFTWARE, NETWORK, ACCESS }

// 2. TICKET CLASS
class Ticket {
    private int id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private Status status;
    private String assignee;
    private String createdAt;
    private String resolutionNotes;

    public Ticket(int id, String title, String description, Category category, Priority priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = Status.OPEN;
        this.assignee = "Unassigned";
        this.resolutionNotes = "None";
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public void setResolutionNotes(String notes) { this.resolutionNotes = notes; }

    @Override
    public String toString() {
        return String.format("[ID: %d] %s | Category: %s | Priority: %s | Status: %s | Assignee: %s\n   Description: %s\n   Resolution: %s\n   Created: %s",
                id, title, category, priority, status, assignee, description, resolutionNotes, createdAt);
    }
    
    // Used for file saving
    public String toCSV() {
        return id + "," + title.replace(",", " ") + "," + description.replace(",", " ") + "," + 
               category + "," + priority + "," + status + "," + assignee.replace(",", " ") + "," + 
               createdAt + "," + resolutionNotes.replace(",", " ");
    }
}

// 3. CUSTOM EXCEPTIONS
class TicketNotFoundException extends Exception {
    public TicketNotFoundException(String msg) { super(msg); }
}
class InvalidStatusChangeException extends Exception {
    public InvalidStatusChangeException(String msg) { super(msg); }
}

// 4. TICKET SERVICE (Business Logic)
class TicketService {
    private Map<Integer, Ticket> tickets = new HashMap<>();
    private int nextId = 1;
    private final String FILE_NAME = "tickets.csv";

    public void createTicket(String title, String desc, Category cat, Priority pri) {
        Ticket t = new Ticket(nextId++, title, desc, cat, pri);
        tickets.put(t.getId(), t);
        System.out.println("Success: Ticket created with ID " + t.getId());
    }

    public Ticket getTicket(int id) throws TicketNotFoundException {
        if (!tickets.containsKey(id)) throw new TicketNotFoundException("Ticket ID " + id + " not found.");
        return tickets.get(id);
    }

    public void assignTicket(int id, String engineer) throws TicketNotFoundException, InvalidStatusChangeException {
        Ticket t = getTicket(id);
        if (t.getStatus() == Status.RESOLVED || t.getStatus() == Status.CLOSED) {
            throw new InvalidStatusChangeException("Cannot assign a resolved/closed ticket.");
        }
        t.setAssignee(engineer);
        t.setStatus(Status.IN_PROGRESS);
        System.out.println("Success: Ticket " + id + " assigned to " + engineer);
    }

    public void resolveTicket(int id, String notes) throws TicketNotFoundException {
        Ticket t = getTicket(id);
        t.setStatus(Status.RESOLVED);
        t.setResolutionNotes(notes);
        System.out.println("Success: Ticket " + id + " resolved.");
    }

    public void viewAllTickets() {
        if (tickets.isEmpty()) { System.out.println("No tickets found."); return; }
        for (Ticket t : tickets.values()) System.out.println(t + "\n-------------------");
    }

    public void filterByPriority(Priority p) {
        System.out.println("--- Filtering by Priority: " + p + " ---");
        tickets.values().stream().filter(t -> t.getPriority() == p).forEach(t -> System.out.println(t + "\n---"));
    }

    public void printSummary() {
        long open = tickets.values().stream().filter(t -> t.getStatus() == Status.OPEN || t.getStatus() == Status.IN_PROGRESS).count();
        long resolved = tickets.values().stream().filter(t -> t.getStatus() == Status.RESOLVED || t.getStatus() == Status.CLOSED).count();
        System.out.println("\n=== SUMMARY REPORT ===");
        System.out.println("Total Tickets: " + tickets.size());
        System.out.println("Open/In-Progress: " + open);
        System.out.println("Resolved/Closed: " + resolved);
        System.out.println("======================\n");
    }

    // File I/O
    public void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Ticket t : tickets.values()) bw.write(t.toCSV() + "\n");
            System.out.println("Data saved to file.");
        } catch (IOException e) { System.out.println("Error saving data: " + e.getMessage()); }
    }

    public void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    int id = Integer.parseInt(parts[0]);
                    Ticket t = new Ticket(id, parts[1], parts[2], Category.valueOf(parts[3]), Priority.valueOf(parts[4]));
                    t.setStatus(Status.valueOf(parts[5]));
                    t.setAssignee(parts[6]);
                    t.setResolutionNotes(parts[8]);
                    tickets.put(id, t);
                    if (id >= nextId) nextId = id + 1;
                }
            }
            System.out.println("Data loaded from previous session.");
        } catch (Exception e) { System.out.println("Error loading data."); }
    }
}

// 5. MAIN CLASS (Console Interface)
public class Main {
    public static void main(String[] args) {
        TicketService service = new TicketService();
        service.loadFromFile();
        Scanner sc = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n=== IT HELPDESK SYSTEM ===");
            System.out.println("1. Create Ticket\n2. View All Tickets\n3. Assign Ticket\n4. Resolve Ticket\n5. Filter by Priority\n6. Summary Report\n7. Save & Exit");
            System.out.print("Choose an option: ");
            
            String choice = sc.nextLine();
            
            try {
                switch (choice) {
                    case "1":
                        System.out.print("Title: "); String title = sc.nextLine();
                        System.out.print("Description: "); String desc = sc.nextLine();
                        System.out.print("Category (HARDWARE/SOFTWARE/NETWORK/ACCESS): "); Category cat = Category.valueOf(sc.nextLine().toUpperCase());
                        System.out.print("Priority (LOW/MEDIUM/HIGH/CRITICAL): "); Priority pri = Priority.valueOf(sc.nextLine().toUpperCase());
                        service.createTicket(title, desc, cat, pri);
                        break;
                    case "2":
                        service.viewAllTickets();
                        break;
                    case "3":
                        System.out.print("Enter Ticket ID to assign: "); int aId = Integer.parseInt(sc.nextLine());
                        System.out.print("Engineer Name: "); String eng = sc.nextLine();
                        service.assignTicket(aId, eng);
                        break;
                    case "4":
                        System.out.print("Enter Ticket ID to resolve: "); int rId = Integer.parseInt(sc.nextLine());
                        System.out.print("Resolution Notes: "); String notes = sc.nextLine();
                        service.resolveTicket(rId, notes);
                        break;
                    case "5":
                        System.out.print("Enter Priority to filter (LOW/MEDIUM/HIGH/CRITICAL): "); 
                        service.filterByPriority(Priority.valueOf(sc.nextLine().toUpperCase()));
                        break;
                    case "6":
                        service.printSummary();
                        break;
                    case "7":
                        service.saveToFile();
                        System.out.println("Exiting system. Goodbye!");
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Input Error: Please type exactly as shown (e.g., HARDWARE, HIGH).");
            } catch (TicketNotFoundException | InvalidStatusChangeException e) {
                System.out.println("Operation Failed: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Invalid input format.");
            }
        }
    }
}