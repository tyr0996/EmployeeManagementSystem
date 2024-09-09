package hu.martin.ems.UITests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AccessManagementUITests {

    @Test
    public void checkLogin(){
        WebDriverManager.edgedriver().setup();
        EdgeDriver driver = new EdgeDriver();
        try{
            driver.get("localhost:8080");
            Duration d = Duration.ofSeconds(2);
            WebDriverWait webDriverWait = new WebDriverWait(driver, d);
            WebElement we = driver.findElement(By.partialLinkText("Register"));
            assertEquals("Register", we.getText(), "Nemjó");
        }
        finally {
            driver.quit();
        }
    }

}
