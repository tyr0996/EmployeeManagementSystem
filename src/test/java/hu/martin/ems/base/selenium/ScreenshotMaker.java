package hu.martin.ems.base.selenium;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ScreenshotMaker {
    static int screenshotId = 0;

    @Value("${selenium.screenshot.folder}")
    String screenshotFolder;

    public void takeScreenshot(WebDriver driver) {
        File f = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try{
            String path = screenshotFolder + "/" + screenshotId + ".png";
            FileUtils.copyFile(f, new File(path));
            screenshotId++;
        } catch (IOException e) {
            throw new RuntimeException("Saving screenshot failed! " + e);
        }
    }
}
