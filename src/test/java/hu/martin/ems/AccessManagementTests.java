package hu.martin.ems;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccessManagementTests extends TestBenchTestCase {

    @BeforeEach
    public void setUp() {
        setDriver(new ChromeDriver());
        getDriver().get("http://localhost:8080");
    }

    @Test
    public void testButtonClickUpdatesTextField() {
        // Megkeressük a gombot és a szövegmezőt
        ButtonElement button = $(ButtonElement.class).first();
        TextFieldElement textField = $(TextFieldElement.class).first();

        // Ellenőrizzük a kezdeti értéket
        assertEquals("", textField.getValue());

        // Gomb megnyomása
        button.click();

        // Ellenőrizzük, hogy a szövegmező frissült-e
        assertEquals("Button clicked!", textField.getValue());
    }
}
