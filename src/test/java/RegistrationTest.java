import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class RegistrationTest {

    @Test
    void shouldSuccessfulFormSubmission() {

        // Открываем страницу через Selenide
        Selenide.open("http://localhost:9999");

        // Генерируем дату (минимум +4 дня от текущей)
        LocalDate deliveryDate = LocalDate.now().plusDays(4);
        String formattedDate = deliveryDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        // 1. Заполняем город
        $("[data-test-id=city] input").setValue("Москва");

        // 2. Работаем с полем даты
        SelenideElement dateField = $("[data-test-id=date]");

        // Кликаем по иконке календаря чтобы активировать поле
        dateField.$(".icon-button").click();

        // Ждем появления поля ввода
        $(".calendar-input__custom-control input").shouldBe(Condition.enabled);

        // Очищаем поле и вводим новую дату
        SelenideElement dateInput = $(".calendar-input__custom-control input");
        dateInput.click();

        // Удаляем текст
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
        LocalDate weekLater = LocalDate.now().plusWeeks(1);
        String weekLaterFormatted = weekLater.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        int targetDay = weekLater.getDayOfMonth();

        // Открываем календарь
        $("[data-test-id=date] .icon-button").click();

        // Ждем появления календаря
        $(".calendar").shouldBe(visible, Duration.ofSeconds(3));

        // Получаем день недели (для клика по нужной ячейке)
        int dayOfMonth = weekLater.getDayOfMonth();

        // Ищем ячейку с нужной датой в календаре

        $$(".calendar__day").findBy(text(String.valueOf(dayOfMonth)))
                .shouldBe(visible)
                .click();

        // Проверяем что дата установилась
        $("[data-test-id=date] .input__control").shouldHave(value(weekLaterFormatted));

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
                .shouldHave(text("Встреча успешно забронирована на " + weekLaterFormatted));
    }
}