public abstract class Vehicle {
    protected String type;
    protected double baseRate;
    
    public Vehicle(String type, double baseRate) {
        this.type = type;
        this.baseRate = baseRate;
    }
    
    public String getType() {
        return type;
    }
    
    public double getBaseRate() {
        return baseRate;
    }
    
    public abstract String getDisplayName();
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    public static class Car extends Vehicle {
        public Car() {
            super("Car", 5.0);
        }
        
        @Override
        public String getDisplayName() {
            return "Car";
        }
    }
    
    public static class Bike extends Vehicle {
        public Bike() {
            super("Bike", 2.0);
        }
        
        @Override
        public String getDisplayName() {
            return "Bike";
        }
    }
    
    public static class EVVehicle extends Vehicle implements Chargeable {
        private static final double CHARGING_RATE = 3.0;
        
        public EVVehicle() {
            super("EV", 6.0);
        }
        
        @Override
        public String getDisplayName() {
            return "Electric Vehicle";
        }
        
        @Override
        public double getChargingRate() {
            return CHARGING_RATE;
        }
        
        @Override
        public String getChargingDescription() {
            return "EV Charging";
        }
    }
}

interface Chargeable {
    double getChargingRate();
    String getChargingDescription();
}
