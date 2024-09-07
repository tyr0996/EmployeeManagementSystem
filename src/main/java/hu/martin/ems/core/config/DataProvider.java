package hu.martin.ems.core.config;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.NeedCleanCoding;
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
        loaded = null;
    }

    private static LinkedHashMap<String, String> generateAllSqlsFromJsons() {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        try{
            List<File> files = Files.walk(jsonsDirectory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .map(Path::toFile).collect(Collectors.toList());
            files.forEach(v -> {
                result.put(v.getName().substring(0, v.getName().length()-5) + ".sql", generateSqlFromJson(v));
            });
        }
        catch (IOException e){
            e.printStackTrace();
            //TODO
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
                    System.err.println("Failed to delete file: " + file.getAbsolutePath());
                }
            }
        } else {
            System.err.println("Provided path is not a directory: " + directory.getAbsolutePath());
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

    private static String generateSqlFromJson(File jsonFile) {
        try{
            ObjectMapper om = new ObjectMapper();
            JsonFile json = om.readValue(jsonFile, JsonFile.class);
            return json.toSQL();
        } catch (StreamReadException e) {
            e.printStackTrace();
            //TODO
            return "";
        } catch (DatabindException e) {
            e.printStackTrace();
            //TODO
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            //TODO
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
        } catch (JsonMappingException e) {
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

        protected String toSQL() {
            switch (object) {
                case "Order":
                    object = "orders";
                    break;
                case "User":
                    object = "loginuser";
                    break;
                default:
                    break;
            }
            if (data.size() > 0) {
                List<String> res = new ArrayList<>();
                List<String> keys = new ArrayList<>(data.get(0).keySet());
                String fieldNames = String.join(", ", keys);
                String baseSql = "INSERT INTO " + object + " (" + fieldNames + ") VALUES ";
                for (Map<String, Object> obj : data) {
                    List<String> valuesList = new ArrayList<>();
                    for (String key : keys) {
                        Object value = obj.get(key);
                        if (value instanceof LinkedHashMap && ((LinkedHashMap<String, Object>) value).containsKey("refClass")) {
                            LinkedHashMap<String, Object> valHashMap = (LinkedHashMap<String, Object>) value;
                            valuesList.add(generateSelectSQLQuerry(valHashMap, key));
                        } else {
                            valuesList.add(value == null ? "NULL" : "'" + value.toString().replace("'", "\\u0027") + "'");
                        }
                    }
                    String values = String.join(", ", valuesList);
                    res.add("(" + values + ")");
                }
                return baseSql + String.join(", ", res);
            } else {
                return "";
            }
        }


        private static String generateSelectSQLQuerry(LinkedHashMap<String, Object> reference, String columnName) {
            String refClass = (String) reference.get("refClass");
            switch (refClass) {
                case "Order":
                    refClass = "orders";
                    break;
                case "User":
                    refClass = "loginuser";
                    break;
                default:
                    break;
            }
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> filter = (LinkedHashMap<String, Object>) reference.get("filter");
            String sql = "(SELECT id as " + columnName + " FROM " + refClass + " WHERE ";

            sql += filter.entrySet().stream().map(v -> v.getKey() + " " + (v.getValue() instanceof String ? "ilike" : "=") + " '" + v.getValue() + "'").collect(Collectors.joining(" AND "));

            return sql + " LIMIT 1)";
        }
    }
}