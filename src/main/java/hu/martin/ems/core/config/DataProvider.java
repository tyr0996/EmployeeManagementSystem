package hu.martin.ems.core.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.annotations.NeedCleanCoding;
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

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
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
@NeedCleanCoding
public class DataProvider {
    private final EntityManager em;
    private final Logger logger = LoggerFactory.getLogger(DataProvider.class);
    private static List<File> loaded = new ArrayList<>();

    private static final Path jsonsDirectory = Paths.get("src/main/resources/static");

    @Autowired
    public DataProvider(EntityManager em) throws IOException {
        this.em = em;
        loadAllJsonAndSave();
        loaded = new ArrayList<>();
    }

    private static LinkedHashMap<String, String> generateAllSqlsFromJsons() {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        try{
            List<File> files = Files.walk(jsonsDirectory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .map(Path::toFile).toList();
            files.forEach(v -> {
                result.put(v.getName().substring(0, v.getName().length()-5) + ".sql", generateSqlFromJson(v));
            });
        }
        catch (IOException e){
            log.error("IOException happened while creating sqls from the json files for database");
            e.printStackTrace();
        }
        return result;
    }

    public static void saveAllSqlsFromJsons(){
        LinkedHashMap<String, String> fileNameAndFileContent = generateAllSqlsFromJsons();
        File directory = new File(System.getProperty("user.dir") + "\\src\\test\\java\\hu\\martin\\ems\\sql");
        deleteFilesInDirectory(directory);
        fileNameAndFileContent.forEach(DataProvider::writeFile);
    }

    public static void deleteFilesInDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile() && !file.delete()) {
                    log.warn("Failed to delete file: " + file.getAbsolutePath());
                }
            }
        } else {
            log.warn("Provided path is not a directory: " + directory.getAbsolutePath());
        }
    }

    public static void writeFile(String fileName, String fileContent) {
        String projectRoot = System.getProperty("user.dir");

        File file = new File(projectRoot + "/src/test/java/hu/martin/ems/sql/" + fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateSqlFromJson(File jsonFile) {
        try{
            ObjectMapper om = new ObjectMapper();
            JsonFile json = om.readValue(jsonFile, JsonFile.class);
            return json.toSQL();
        } catch (IOException e) {
            log.warn("Error occured while reading json file: " + jsonFile.getName());
            return "";
        }
    }

    private void loadAllJsonAndSave() throws IOException {
        Path directory = Paths.get("src/main/resources/static");

        List<File> files = Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                .map(Path::toFile).collect(Collectors.toList());
        files.stream().filter(v -> !loaded.contains(v)).forEach(v -> loadRequiredJsonAndSave(files, v));
    }

    private void loadRequiredJsonAndSave(List<File> allFiles, File jsonFile) {
        if (loaded.contains(jsonFile)) {
            return;
        } else {
            ObjectMapper om = new ObjectMapper();
            try {
                JsonFile json = om.readValue(jsonFile, JsonFile.class);
                List<String> required;
                if(json.required == null) {
                    required = new ArrayList<>();
                    logger.warn("The \"required\" field is misisng from json " + jsonFile.getName());
                }
                else{
                    required = json.required;
                }

                List<File> requiredFiles = allFiles.stream()
                        .filter(file -> required.contains(file.getName())).toList();
                requiredFiles.forEach(v -> loadRequiredJsonAndSave(allFiles, v));
                saveJsonToDatabase(jsonFile);
            } catch (JsonMappingException e) {
                logger.error("Hiba a json fájlban! " + jsonFile.getName());
            } catch (IOException e) {
                logger.error("HIBA: " + jsonFile.getName());
            }
        }
    }

    private void saveJsonToDatabase(File jsonFile) {
        EntityTransaction entityTransaction = em.getTransaction();
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonFile json = objectMapper.readValue(jsonFile, JsonFile.class);
            json.init();
            String sql = json.toSQL();
            entityTransaction.begin();
            em.createNativeQuery(sql).executeUpdate();
            entityTransaction.commit();
            logger.info("Data loaded successfully from JSON! " + jsonFile.getName());
            loaded.add(jsonFile);
        } catch (JsonMappingException e) {
            logger.error("Hiba a json fájlban! " + jsonFile.getName());
        } catch (IOException e){
            logger.error("HIBA: " + jsonFile.getName());
        }
    }

    public void executeSQL(String sql){
        EntityTransaction entityTransaction = em.getTransaction();
        entityTransaction.begin();
        em.createNativeQuery(sql).executeUpdate();
        entityTransaction.commit();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    static class JsonFile {
        List<Map<String, Object>> data;
        String objectName;
        ArrayList<String> required;

        @PostConstruct
        public void init(){
            this.objectName = fixObjectName(objectName);
        }

        private static String fixObjectName(String objectName){
            switch (objectName) {
                case "Order": return "orders";
                case "User": return "loginuser";
                default: return objectName;
            }
        }

        protected String toSQL() {
            if (data.isEmpty()) {
                return "";
            }

            List<String> keys = new ArrayList<>(data.get(0).keySet());
            String fieldNames = String.join(", ", keys);
            String baseSql = "INSERT INTO " + objectName + " (" + fieldNames + ") VALUES\n";
            List<String> valueRows = data.stream()
                    .map(obj -> buildValuesRow(obj, keys))
                    .collect(Collectors.toList());

            return baseSql + String.join(",\n", valueRows);
        }

        private String buildValuesRow(Map<String, Object> obj, List<String> keys) {
            List<String> valuesList = keys.stream()
                    .map(key -> formatValue(obj.get(key), key))
                    .collect(Collectors.toList());

            return "\t(" + String.join(", ", valuesList) + ")";
        }

        private String formatValue(Object value, String key) {
            if (value instanceof LinkedHashMap && ((LinkedHashMap<?, ?>) value).containsKey("refClass")) {
                return generateSelectSQLQuery((LinkedHashMap<String, Object>) value, key);
            }
            return value == null ? "NULL" : "'" + value.toString().replace("'", "\\u0027") + "'";
        }

        private static String generateSelectSQLQuery(LinkedHashMap<String, Object> reference, String columnName) {
            String refClass = fixObjectName((String) reference.get("refClass"));
            LinkedHashMap<String, Object> filter = (LinkedHashMap<String, Object>) reference.get("filter");

            String conditions = filter.entrySet().stream()
                    .map(entry -> buildConditionForSelect(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(" AND "));

            return "(SELECT id as " + columnName + " FROM " + refClass + " WHERE " + conditions + " LIMIT 1)";
        }

        private static String buildConditionForSelect(String key, Object value) {
            return key + (value instanceof String ? " ILIKE \'" + value + "\'" : " = " + value);
        }
    }
}