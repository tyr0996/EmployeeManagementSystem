package hu.martin.ems.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hu.martin.ems.annotations.NeedCleanCoding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@NeedCleanCoding
public class JsonConfig {
    public JsonConfig(){}

    @Bean
    @Primary
    public Gson gson() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
//                .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return gson;
    }

}
