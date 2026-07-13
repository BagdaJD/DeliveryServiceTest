package org.example;

import org.junit.jupiter.api.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.DynamicTest;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование сервиса доставки DeliveryService")
@Tag("regression")
class DeliveryServiceTest {

    private DeliveryService deliveryService;

    @BeforeAll
    static void initAll() {
        System.out.println("--- Старт всех тестов ---");
    }

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService();
    }

    @AfterEach
    void tearDown() {
        System.out.println("Очистка контекста после завершения одиночного теста");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("--- Все тесты пройдены ---");
    }

    // БЛОК ТЕСТОВ ДЛЯ МЕТОДА calculateCost
    @Nested
    @DisplayName("Расчет стоимости доставки (calculateCost)")
    @Tag("cost")
    class CostCalculationTests {

        @Test
        @DisplayName("Должен возвращать минимальную стоимость (400.0), если расчетная сумма меньше")
        void shouldReturnMinimalCostIfCalculatedIsLow() {
            double cost = deliveryService.calculateCost(1.0, false, false, DeliveryService.Status.NORMAL);
            assertEquals(400.0, cost, 0.001);
        }

        @Test
        @DisplayName("Корректный расчет для большого груза при высоком спросе")
        void shouldCalculateCorrectlyForLargeAndHighStatus() {
            double cost = deliveryService.calculateCost(20.0, true, false, DeliveryService.Status.HIGH);
            assertEquals(560.0, cost, 0.001);
        }

        //демонстрация @Disabled
        @Test
        @Disabled("Тест отключен: временное изменение тарифов для маленьких грузов, ждем апдейта ТЗ")
        @DisplayName("Проверка стоимости для маленького груза")
        void shouldCalculateForSmallSize() {
            double cost = deliveryService.calculateCost(10.0, false, false, DeliveryService.Status.NORMAL);
            assertEquals(400.0, cost, 0.001);
        }
    }

    // БЛОК ТЕСТОВ ДЛЯ МЕТОДА calculateTime
    @Nested
    @DisplayName("Расчет времени доставки (calculateTime)")
    @Tag("time")
    class TimeCalculationTests {
        private final LocalDate FIXED_MONDAY = LocalDate.of(2026, 7, 13);

        @Test
        @DisplayName("Обычная близкая доставка: дистанция <= 10 км, не хрупкое, NORMAL -> 1 день")
        void shouldCalculateTimeForShortDistanceAndNormalStatus() {
            LocalDate result = deliveryService.calculateTime(FIXED_MONDAY, 5.0, false, DeliveryService.Status.NORMAL);
            assertEquals(LocalDate.of(2026, 7, 14), result);
        }

        @Test
        @DisplayName("Максимальное начисление дней: дальняя дистанция (>10 км), хрупкое, спрос HIGH")
        void shouldAccumulateAllExtraDays() {
            LocalDate result = deliveryService.calculateTime(FIXED_MONDAY, 15.0, true, DeliveryService.Status.HIGH);
            assertEquals(LocalDate.of(2026, 7, 17), result);
        }

        @Test
        @DisplayName("Перенос даты на понедельник, если расчетный день прибытия — воскресенье")
        void shouldShiftDeliveryDateToMondayIfItIsSunday() {
            // Берем пятницу: 17 июля 2026 года
            LocalDate friday = LocalDate.of(2026, 7, 17);
            LocalDate result = deliveryService.calculateTime(friday, 15.0, false, DeliveryService.Status.NORMAL);

            assertEquals(LocalDate.of(2026, 7, 20), result);
            assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
        }
    }

    // БЛОК ТЕСТОВ С ИСКЛЮЧЕНИЯМИ
    @Nested
    @DisplayName("Тестирование валидации ограничений")
    @Tag("exceptions")
    class ValidationErrorTests {

        @Test
        @DisplayName("Выброс IllegalArgumentException при доставке хрупкого груза более чем на 30 км")
        void shouldThrowExceptionWhenFragileAndDistanceIsOverMax() {
            IllegalArgumentException costException = assertThrows(IllegalArgumentException.class, () ->
                    deliveryService.calculateCost(30.01, false, true, DeliveryService.Status.NORMAL)
            );

            IllegalArgumentException timeException = assertThrows(IllegalArgumentException.class, () ->
                    deliveryService.calculateTime(LocalDate.now(), 35.0, true, DeliveryService.Status.NORMAL)
            );

            assertAll(
                    () -> assertEquals("Хрупкие грузы нельзя возить дальше 30 км", costException.getMessage()),
                    () -> assertEquals("Хрупкие грузы нельзя возить дальше 30 км", timeException.getMessage())
            );
        }
    }

    //ДИНАМИЧЕСКИЕ ТЕСТЫ)
    @TestFactory
    @DisplayName("Динамическая фабрика тестов для проверки минимальной стоимости")
    @Tag("dynamic")
    Stream<DynamicTest> dynamicTestsForMinCostBoundary() {
        return Stream.of(1.0, 2.0, 3.0, 4.0, 5.0)
                .map(distance -> DynamicTest.dynamicTest(
                        "Проверка минимальной стоимости для дистанции: " + distance + " км",
                        () -> {
                            double cost = deliveryService.calculateCost(distance, false, false, DeliveryService.Status.NORMAL);
                            assertEquals(400.0, cost, 0.001);
                        }
                ));
    }
}