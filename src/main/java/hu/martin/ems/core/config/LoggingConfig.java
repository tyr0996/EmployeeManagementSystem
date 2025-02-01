package hu.martin.ems.core.config;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileWriter;
import java.io.IOException;

@Configuration
//@AllArgsConstructor
@NoArgsConstructor
public class LoggingConfig {

    @Value("${database.logpath}")
    private String databaseLogPath;

    public void resetHibernateLog() throws IOException {
        clearLogFile();
        LogNumberingConverter.resetCounter();
    }

    private void clearLogFile(){
        try {
            FileWriter writer = new FileWriter(databaseLogPath, false);
            writer.close();
        } catch (IOException e) {
            System.err.println("Hiba történt a fájl kiürítése közben: " + e.getMessage());
        }
    }
}
