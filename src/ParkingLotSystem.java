import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ParkingLotSystem {
    private static final int CAR_SLOTS = 8, BIKE_SLOTS = 4, EV_SLOTS = 3;
    private static final int TOTAL_SLOTS = CAR_SLOTS + BIKE_SLOTS + EV_SLOTS;
    private static final String RESET = "\u001B[0m", GREEN = "\u001B[32m", RED = "\u001B[31m", BLUE = "\u001B[34m", CYAN = "\u001B[36m", YELLOW = "\u001B[33m";
    
    private final Scanner scanner = new Scanner(System.in);
    private final List<Slot> slots = new ArrayList<>();
    private final Map<String, User> users = new HashMap<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final PaymentService paymentService = new PaymentService();
    private String lotteryWinner;

    static class User {
        final String name; final boolean vip;
        User(String name, boolean vip) { this.name = name; this.vip = vip; }
    }

    static class Reservation {
        final String username, id; final Vehicle vehicle; final LocalDateTime time; final int slotId;
        Reservation(String username, Vehicle vehicle, LocalDateTime time, int slotId) {
            this.username = username; this.vehicle = vehicle; this.time = time; this.slotId = slotId;
            this.id = "RES" + System.currentTimeMillis();
        }
    }

    public static void main(String[] args) {
        new ParkingLotSystem().start();
    }

    public void start() {
        init(); showMenu();mainLoopy();
    }

    private void init() {
        // Initialize slots
        int id = 1;
        for (int i = 0; i < CAR_SLOTS; i++) slots.add(new Slot(id++, Slot.SlotType.CAR));
        for (int i = 0; i < BIKE_SLOTS; i++) slots.add(new Slot(id++, Slot.SlotType.BIKE));
        for (int i = 0; i < EV_SLOTS; i++) slots.add(new Slot(id++, Slot.SlotType.EV));
        
        // Initialize users
        users.put("john", new User("John", true));
        users.put("jane", new User("Jane", false));
        users.put("bob", new User("Bob", true));
    }

    private void showMenu() {
        System.out.println(CYAN + "╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                        PARKING LOT SYSTEM                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);
    }

    private void mainLoop() {
        while (true) {
            printMenu();
            int choice = readInt("Choice: ");
            if (choice == 8) break;
            processChoice(choice);
        }
        System.out.println("Thank you for using Parking Lot System!");
    }

    private void printMenu() {
        System.out.println(CYAN + "\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                           MAIN MENU                            ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. View Slots          - Display parking grid               ║");
        System.out.println("║  2. Park Vehicle        - Park a vehicle in suitable slot   ║");
        System.out.println("║  3. Exit Vehicle        - Remove vehicle and calculate cost ║");
        System.out.println("║  4. Status              - Show occupancy statistics         ║");
        System.out.println("║  5. Register            - Add new user with VIP/Normal pass ║");
        System.out.println("║  6. Schedule            - Book future parking reservation   ║");
        System.out.println("║  7. View Reservations   - Show scheduled reservations       ║");
        System.out.println("║  8. Exit                - Close the system                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);
    }

    private void processChoice(int choice) {
        switch (choice) {
            case 1: showSlots(); break;
            case 2: parkVehicle(); break;
            case 3: exitVehicle(); break;
            case 4: showStatus(); break;
            case 5: registerUser(); break;
            case 6: scheduleSlot(); break;
            case 7: viewReservations(); break;
        }
    }

    private void showSlots() {
        System.out.println("\nParking Grid:");
        printGrid();
        promptEnter();
    }

    private void parkVehicle() {
        String username = readLine("Username: ");
        User user = users.get(username);
        if (user == null) { System.out.println(RED + "User not found." + RESET); return; }
        
        Vehicle vehicle = chooseVehicle();
        Slot slot = findSlot(vehicle);
        if (slot == null) { System.out.println(RED + "No slot available." + RESET); return; }
        
        slot.occupy(username, vehicle);
        System.out.println(GREEN + "Parked in slot " + slot.getId() + RESET);
        
        // Random lottery during parking
        if (lotteryWinner == null) {
            String[] names = {"john", "jane", "bob"};
            lotteryWinner = names[new Random().nextInt(names.length)];
            System.out.println(CYAN + "🎉 DAILY LOTTERY WINNER: " + users.get(lotteryWinner).name + " (Free parking today!)" + RESET);
        }
        
        if (username.equals(lotteryWinner)) {
            System.out.println(CYAN + "🎊 CONGRATULATIONS! You won the lottery - FREE PARKING!" + RESET);
        }
        promptEnter();
    }

        private void exitVehicle() {
        int slotId = readInt("Slot ID: ");
        Slot slot = getSlot(slotId);
        if (slot == null || slot.isAvailable()) {
            System.out.println(RED + "Invalid slot." + RESET);
            return;
        }

        User user = users.get(slot.getOccupiedByUsername());
        boolean free = slot.getOccupiedByUsername().equals(lotteryWinner);

        LocalDateTime loginTime = slot.getOccupiedAt();
        LocalDateTime logoutTime = LocalDateTime.now();

        double cost = free ? 0 : paymentService.calculateCost(loginTime, logoutTime, slot.getParkedVehicle(), user.vip);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println(GREEN + "Login Time : " + loginTime.format(formatter) + RESET);
        System.out.println(GREEN + "Logout Time: " + logoutTime.format(formatter) + RESET);
        System.out.println(GREEN + "Exit successful. Cost: Rs " + String.format("%.2f", cost) + RESET);
        slot.free();
        promptEnter();
    }

private void showStatus() {
    long available = slots.stream().filter(Slot::isAvailable).count();
    System.out.println(CYAN + "\n╔══════════════════════════════════════════════════════════════╗");
    System.out.println("║                      CURRENT SLOT OCCUPANCY                  ║");
    System.out.println("╠════╦═══════╦════════════╦══════════════╦═════════════════════╣");
    System.out.println("║ ID ║ Type  ║ Occupied By║ Vehicle Type ║ Time of Parking     ║");
    System.out.println("╠════╬═══════╬════════════╬══════════════╬═════════════════════╣");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    boolean anyOccupied = false;
    for (Slot slot : slots) {
        if (!slot.isAvailable()) {
            anyOccupied = true;
            System.out.printf("║ %2d ║ %-5s ║ %-10s ║ %-12s ║ %-19s ║%n",
                slot.getId(),
                getTypeLabel(slot.getType()),
                slot.getOccupiedByUsername(),
                slot.getParkedVehicle().getType(),
                slot.getOccupiedAt().format(formatter)
            );
        }
    }

    if (!anyOccupied) {
        System.out.println("║                   No active vehicle in any slot              ║");
    }

    System.out.println("╚════╩═══════╩════════════╩══════════════╩═════════════════════╝" + RESET);
    System.out.println(YELLOW + "Available slots: " + available + "/" + TOTAL_SLOTS + RESET);
    promptEnter();
}


    private void registerUser() {
        String username = readLine("Username: ");
        if (users.containsKey(username)) { System.out.println(RED + "Username exists." + RESET); return; }
        String name = readLine("Name: ");
        boolean vip = readLine("VIP? (y/n): ").equalsIgnoreCase("y");
        users.put(username, new User(name, vip));
        System.out.println(GREEN + "User registered." + RESET);
        promptEnter();
    }

    private void scheduleSlot() {
        String username = readLine("Username: ");
        User user = users.get(username);
        if (user == null) { System.out.println(RED + "User not found." + RESET); return; }
        
        Vehicle vehicle = chooseVehicle();
        System.out.println("Enter time (24h format):");
        int hour = readInt("Hour (0-23): "), minute = readInt("Minute (0-59): ");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = now.withHour(hour).withMinute(minute);
        if (scheduledTime.isBefore(now)) scheduledTime = scheduledTime.plusDays(1);
        
        Slot slot = findSlot(vehicle);
        if (slot == null) { System.out.println(RED + "No slot available for scheduling." + RESET); return; }
        
        Reservation reservation = new Reservation(username, vehicle, scheduledTime, slot.getId());
        reservations.add(reservation);
        
        System.out.println(GREEN + "Scheduled! ID: " + reservation.id + " Slot: " + slot.getId() + RESET);
        System.out.println("Time: " + scheduledTime.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm")));
        promptEnter();
    }

private void activateReservations() {
    LocalDateTime now = LocalDateTime.now();
    Iterator<Reservation> iterator = reservations.iterator();
    
    while (iterator.hasNext()) {
        Reservation res = iterator.next();
        Slot slot = getSlot(res.slotId);
        if (slot == null) {
            iterator.remove(); // Invalid slot, remove reservation
            continue;
        }
        
        if (!slot.isAvailable()) {
            // Slot already occupied, skip or handle conflict
            continue;
        }
        
        if (!res.time.isAfter(now)) { 
            slot.occupy(res.username, res.vehicle);
            System.out.println(GREEN + "Reservation " + res.id + " activated: Slot " + slot.getId() + " occupied by " + res.username + RESET);
            iterator.remove();  
        }
    }
}

private void mainLoopy() {
    while (true) {
        activateReservations();
        
        printMenu();
        int choice = readInt("Choice: ");
        if (choice == 8) break;
        processChoice(choice);
    }
    System.out.println("Thank you for using Parking Lot System!");
}


    private void viewReservations() {
        activateReservations(); 
        if (reservations.isEmpty()) {
            System.out.println(RED + "No scheduled reservations found." + RESET);
        } else {
            System.out.println(CYAN + "\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                        SCHEDULED RESERVATIONS                   ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);
            for (Reservation res : reservations) {
                System.out.println("ID: " + res.id + " | Username: " + res.username + " | Vehicle: " + res.vehicle.getType() + " | Time: " + res.time.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
            }
        }
        promptEnter();
    }

    private Vehicle chooseVehicle() {
        System.out.println("1.Car 2.Bike 3.EV");
        int choice = readInt("Vehicle: ");
        switch (choice) {
            case 1: return new Vehicle.Car();
            case 2: return new Vehicle.Bike();
            case 3: return new Vehicle.EVVehicle();
            default: return new Vehicle.Car();
        }
    }

    private Slot findSlot(Vehicle vehicle) {
        return slots.stream().filter(s -> s.isAvailable() && s.isSuitableFor(vehicle)).findFirst().orElse(null);
    }

    private Slot getSlot(int id) {
        return slots.stream().filter(s -> s.getId() == id).findFirst().orElse(null);
    }

    private void printGrid() {
        System.out.print("   ");
        for (int i = 0; i < 4; i++) System.out.print("___|");
        System.out.println();
        
        System.out.print("   ");
        int count = 0;
        for (Slot slot : slots) {
            String cell = slot.isAvailable() ? 
                (slot.getType() == Slot.SlotType.EV ? BLUE : GREEN) + getTypeLabel(slot.getType()) + RESET + "|" :
                RED + getVehicleLabel(slot.getParkedVehicle()) + RESET + "|";
            System.out.print(cell);
            count++;
            if (count % 4 == 0) {
                System.out.println();
                System.out.print("   ");
                for (int i = 0; i < 4; i++) System.out.print("___|");
                System.out.println();
                if (count < slots.size()) System.out.print("   ");
            }
        }
        if (count % 4 != 0) {
            System.out.println();
            System.out.print("   ");
            for (int i = 0; i < 4; i++) System.out.print("___|");
            System.out.println();
        }
    }

    private String getTypeLabel(Slot.SlotType type) {
        switch (type) {
            case CAR: return "CAR";
            case BIKE: return "BIK";
            case EV: return "EV ";
            default: return "   ";
        }
    }

    private String getVehicleLabel(Vehicle vehicle) {
        if (vehicle instanceof Vehicle.EVVehicle) return "EV ";
        if (vehicle instanceof Vehicle.Car) return "CAR";
        if (vehicle instanceof Vehicle.Bike) return "BIK";
        return "   ";
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private void promptEnter() {
        System.out.println("Press Enter...");
        scanner.nextLine();
    }
}
