import java.time.LocalDateTime;

public class Slot {
    private final int id;
    private final SlotType type;
    private boolean occupied;
    private LocalDateTime occupiedAt;
    private Vehicle parkedVehicle;
    private String occupiedByUsername;

    public enum SlotType {
        CAR("Car"),
        BIKE("Bike"), 
        EV("Electric Vehicle");

        private final String displayName;

        SlotType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public Slot(int id, SlotType type) {
        this.id = id;
        this.type = type;
        this.occupied = false;
    }

    public int getId() {
        return id;
    }

    public SlotType getType() {
        return type;
    }

    public boolean isAvailable() {
        return !occupied;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void occupy(String username, Vehicle vehicle) {
        this.occupied = true;
        this.occupiedAt = LocalDateTime.now();
        this.parkedVehicle = vehicle;
        this.occupiedByUsername = username;
    }

    public void free() {
        this.occupied = false;
        this.occupiedAt = null;
        this.parkedVehicle = null;
        this.occupiedByUsername = null;
    }

    public LocalDateTime getOccupiedAt() {
        return occupiedAt;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public String getOccupiedByUsername() {
        return occupiedByUsername;
    }

    public boolean isSuitableFor(Vehicle vehicle) {
        String vehicleType = vehicle.getType();

        switch (type) {
            case CAR:
                return "Car".equals(vehicleType) || "EV".equals(vehicleType);
            case BIKE:
                return "Bike".equals(vehicleType);
            case EV:
                return "EV".equals(vehicleType);
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Slot %d (%s) - %s", id, type, occupied ? "OCCUPIED" : "AVAILABLE");
    }
}
