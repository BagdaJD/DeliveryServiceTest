package org.example;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DeliveryService {
    private static final double MAX_FRAG_DISTANCE = 30;
    private static final double MIN_COST = 400;

    public enum Status {
        NORMAL(1.0),
        INCREASED(1.2),
        HIGH(1.4);

        private final double coef;
        Status(double coef) { this.coef = coef; }
        public double getCoef() { return coef; }
    }

    //Расчет времени доставки

    public LocalDate calculateTime(LocalDate currentDay, double distance, boolean fragility, Status status) {
        checkFragAndDistance(distance, fragility);

        int days = (distance <= 10) ? 1 : 2;
        if (fragility) days++;
        if (status == Status.HIGH) days++;

        // Прибавляем дни к ПЕРЕДАННОЙ дате заказа, а не к LocalDate.now()
        LocalDate estimatedDate = currentDay.plusDays(days);

        // Если день прибытия выпал на воскресенье — переносим на понедельник
        if (estimatedDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            estimatedDate = estimatedDate.plusDays(1);
        }

        return estimatedDate;
    }

    //Расчет стоимости доставки
    public double calculateCost(double distance, boolean isLargeSize, boolean fragility, Status status) {
        checkFragAndDistance(distance, fragility);

        double cost = isLargeSize ? ((distance * 10) + 200) * status.getCoef()
                : ((distance * 10) + 100) * status.getCoef();

        if (cost < MIN_COST) cost = MIN_COST;
        return cost;
    }


     //вспомогатлеьная проверка
    private void checkFragAndDistance(double distance, boolean fragility) {
        if (fragility && distance > MAX_FRAG_DISTANCE) {
            throw new IllegalArgumentException("Хрупкие грузы нельзя возить дальше 30 км");
        }
    }
}