import java.time.Duration;
import java.time.LocalDateTime;

public class PaymentService {
    public static final double VIP_DISCOUNT_PERCENT = 20.0;
    private static final double BASE_COST = 10.0;       // Minimum base cost
    private static final double COST_PER_SECOND = 0.1;  // Adjustable rate per second

    public double calculateCost(LocalDateTime startTime, LocalDateTime endTime, Vehicle vehicle, boolean isVip) {
        if (startTime == null || endTime == null || vehicle == null) return 0.0;

        long seconds = Duration.between(startTime, endTime).getSeconds();
        if (seconds < 0) seconds = 0;

        double cost = BASE_COST + (COST_PER_SECOND * seconds);

        // Add charging rate if it's an EV
        if (vehicle instanceof Chargeable) {
            double chargingRatePerSec = ((Chargeable) vehicle).getChargingRate() / 3600.0;
            cost += chargingRatePerSec * seconds;
        }

        // Apply VIP discount
        if (isVip) {
            cost *= (1.0 - VIP_DISCOUNT_PERCENT / 100.0);
        }

        return round2(cost);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
