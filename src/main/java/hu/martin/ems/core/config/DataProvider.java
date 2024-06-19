package hu.martin.ems.core.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Transactional
@Slf4j
public class DataProvider {
    private final EntityManager em;
    private final Logger logger = LoggerFactory.getLogger(DataProvider.class);
    private static List<File> loaded = new ArrayList<>();

    @Autowired
    public DataProvider(EntityManager em) throws IOException {
        this.em = em;
        loadAllJsonAndSave();
        loaded = null;
    }

    private void loadAllJsonAndSave() throws IOException {
        Path directory = Paths.get("src/main/resources/static");

        List<File> files = Files.walk(directory)
             .filter(Files::isRegularFile)
             .filter(path -> path.toString().toLowerCase().endsWith(".json"))
             .map(Path::toFile).collect(Collectors.toList());
        files.stream().filter(v -> !loaded.contains(v)).forEach(v -> loadRequiredJsonAndSave(files, v));
    }

    private void loadRequiredJsonAndSave(List<File> allFiles, File jsonFile){
        if (loaded.contains(jsonFile)) {
            return;
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonFile json = objectMapper.readValue(jsonFile, JsonFile.class);
                List<String> required = json.required == null ? new ArrayList<>() : json.required;
                if (json.required == null) {
                    logger.warn("The \"required\" field is misisng from json " + jsonFile.getName());
                }

                List<File> requiredFiles = allFiles.stream()
                        .filter(file -> required.contains(file.getName()))
                        .collect(Collectors.toList());
                requiredFiles.forEach(v -> loadRequiredJsonAndSave(allFiles, v));
                saveJsonToDatabase(jsonFile);
            } catch (JsonMappingException e) {
                logger.error("Hiba a json fájlban! " + jsonFile.getName());
            } catch (IOException e) {
                logger.error("HIBA: " + jsonFile.getName());
                //throw new RuntimeException(e);
            }
        }
    }

    private void saveJsonToDatabase(File jsonFile) {
        EntityTransaction entityTransaction = em.getTransaction();
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonFile json = objectMapper.readValue(jsonFile, JsonFile.class);
            String sql = json.toSQL();
            entityTransaction.begin();
            em.createNativeQuery(sql).executeUpdate();
            entityTransaction.commit();
            logger.info("Data loaded successfully from JSON! " + jsonFile.getName());
            loaded.add(jsonFile);
        } catch (JsonMappingException e){
            if (entityTransaction != null && entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            logger.error("Hiba a json fájlban! " + jsonFile.getName());
        } catch (IOException e) {
            if (entityTransaction != null && entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            logger.error("HIBA: " + jsonFile.getName());
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    static class JsonFile {
        List<Map<String, Object>> data;
        String object;
        List<String> required;

        protected String toSQL(){
            switch (object) {
                case "Order" : object = "orders"; break;
                default: break;
            }
            if(data.size() > 0){
                List<String> res = new ArrayList<>();
                List<String> keys = new ArrayList<>(data.get(0).keySet());
                String fieldNames = String.join(", ", keys);
                String baseSql = "INSERT INTO " + object + " (" + fieldNames + ") VALUES ";
                for(Map<String, Object> obj : data){
                    List<String> valuesList = new ArrayList<>();
                    for (String key : keys) {
                        Object value = obj.get(key);
                        if(value instanceof LinkedHashMap && ((LinkedHashMap<String, Object>) value).containsKey("refClass")){
                            LinkedHashMap<String, Object> valHashMap = (LinkedHashMap<String, Object>) value;
                            valuesList.add(generateSelectSQLQuerry(valHashMap, key));
                        }
                        else{
                            valuesList.add(value == null ? "NULL" : "'" + value.toString().replace("'", "\\u0027") + "'");
                        }
                    }
                    String values = String.join(", ", valuesList);
                    res.add("(" + values + ")");
                }
                return baseSql + String.join(", ", res);
            }
            else{
                return "";
            }
        }


        private static String generateSelectSQLQuerry(LinkedHashMap<String, Object> reference, String columnName) {
            String refClass = (String) reference.get("refClass");
            switch (refClass) {
                case "Order" : refClass = "orders"; break;
                default: break;
            }
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> filter = (LinkedHashMap<String, Object>) reference.get("filter");
            String sql = "(SELECT id as " + columnName + " FROM " + refClass + " WHERE ";

            sql += filter.entrySet().stream().map(v -> v.getKey() + " " + (v.getValue() instanceof String ? "ilike" : "=") + " '" + v.getValue() + "'").collect(Collectors.joining(" AND "));

            return sql + " LIMIT 1)";
        }
    }
}