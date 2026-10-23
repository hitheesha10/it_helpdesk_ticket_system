# IT Helpdesk Ticket Management System

This is a console-based Java application I built to simulate how an internal IT support team tracks and resolves technical issues. 

## Why I built this
I wanted to build something that demonstrates core Java concepts (OOP, Collections, File I/O) while solving a real-world problem: keeping track of support requests. Instead of just doing a basic CRUD app, I wanted to include a proper lifecycle (Open -> In Progress -> Resolved) and enforce some basic business rules.

## Features
- **Create Tickets:** Log issues with a title, description, category (Hardware, Software, Network, Access), and priority (Low to Critical).
- **Auto-generated IDs:** Every ticket gets a unique ID and a timestamp.
- **Ticket Assignment:** Assign tickets to support engineers. Once assigned, the status automatically changes to "In Progress".
- **Status Tracking:** Enforce status transitions (you can't resolve a ticket that hasn't been assigned).
- **Search & Filter:** Filter the ticket list by Priority or view a full summary report.
- **Data Persistence:** Tickets are saved to a local CSV file (`tickets.csv`) when you exit, so you don't lose your data between sessions.

## How to Run
You don't need any external libraries. Just a standard Java environment (JDK 8 or higher).
1. Clone this repo or download `Main.java`.
2. Compile and run it:
   ```bash
   javac Main.java
   java Main
