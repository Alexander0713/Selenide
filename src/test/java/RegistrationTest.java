import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class RegistrationTest {

    private String generateDate(int daysToAdd) {
        LocalDate date = LocalDate.now().plusDays(daysToAdd);
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }


    @Test
    void shouldSuccessfulFormSubmission() {

        // Открываем страницу через Selenide
        Selenide.open("http://localhost:9999");

        // Генерируем дату (минимум +4 дня от текущей)
        String formattedDate = generateDate(4);


        // 1. Заполняем город
        $("[data-test-id=city] input").setValue("Москва");

        // 2. Работаем с полем даты
        SelenideElement dateInput = $("[data-test-id=date] input");
        dateInput.shouldBe(Condition.enabled);

        // Очищаем поле и вводим новую дату
        dateInput.press(Keys.CONTROL + "a");
        dateInput.press(Keys.BACK_SPACE);
        dateInput.setValue(formattedDate);

        // 3. Заполняем имя
        $("[data-test-id=name] input").setValue("Иванов Иван");

        // 4. Заполняем телефон
        $("[data-test-id=phone] input").setValue("+79123456789");

        // 5. Отмечаем чекбокс
        $("[data-test-id=agreement]").click();

        // 6. Нажимаем кнопку
        $x("//button[.//span[text()='Забронировать']]").click();
        // 7. Проверяем уведомление
        $("[data-test-id=notification]")
                .shouldBe(visible, Duration.ofSeconds(15));

        // 8. Проверяем текст уведомления
        $("[data-test-id=notification] .notification__content")
                .shouldHave(text("Встреча успешно забронирована на " + formattedDate));
    }

    @Test
    void shouldSubmitFormWithCityAutocomplete() {
        // Задача №2: выбор города из выпадающего списка
        Selenide.open("http://localhost:9999");

        // 1. Вводим две буквы "мо" в поле города
        $("[data-test-id=city] input").setValue("мо");

        // 2. Ждем появления выпадающего списка
        $(".input__menu").shouldBe(visible, Duration.ofSeconds(3));

        // 3. Ищем в списке город "Москва" и кликаем по нему
        $$(".input__menu .menu-item").findBy(text("Москва")).click();

        // Проверяем что город выбран
        $("[data-test-id=city] input").shouldHave(value("Москва"));

        // 4. Выбираем дату через календарь (на неделю вперед)
        LocalDate currentDate = LocalDate.now();
        LocalDate targetDate = currentDate.plusWeeks(1);
        String targetDateFormatted = targetDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        int targetDay = targetDate.getDayOfMonth();
        String targetMonth = targetDate.format(DateTimeFormatter.ofPattern("LLLL", new Locale("ru")));

        // Открываем календарь
        $("[data-test-id=date] .icon-button").click();

        // Ждем появления календаря
        $(".calendar").shouldBe(visible, Duration.ofSeconds(3));

        // Проверяем текущий месяц в календаре
        String calendarMonth = $(".calendar__name").text().toLowerCase();

        // Добавляем счетчик итераций
        int maxAttempts = 12; // максимум на год вперед
        int attempts = 0;

        // Если месяц в календаре не соответствует целевому месяцу, переключаем на следующий месяц
        while (!calendarMonth.contains(targetMonth.toLowerCase()) && attempts < maxAttempts) {
            // Кликаем по кнопке переключения на следующий месяц
            $(".calendar__arrow.calendar__arrow_direction_right[data-step='1']").click();


            // Обновляем значение текущего месяца в календаре
            calendarMonth = $(".calendar__name").text().toLowerCase();
            attempts++;
        }

        // Ищем ячейку с нужной датой в календаре и кликаем
        $$(".calendar__day").findBy(text(String.valueOf(targetDay)))
                .shouldBe(visible)
                .click();

        // Проверяем что дата установилась
        $("[data-test-id=date] .input__control").shouldHave(value(targetDateFormatted));

        // 5. Заполняем остальные поля
        $("[data-test-id=name] input").setValue("Петров Петр");
        $("[data-test-id=phone] input").setValue("+79234567890");
        $("[data-test-id=agreement]").click();

        // 6. Отправляем форму
        $x("//button[.//span[text()='Забронировать']]").click();

        // 7. Проверяем результат
        $("[data-test-id=notification]")
                .shouldBe(visible, Duration.ofSeconds(15))
                .$(".notification__content")
                .shouldHave(text("Встреча успешно забронирована"));
    }
}
