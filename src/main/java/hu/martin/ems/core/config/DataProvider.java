package hu.martin.ems.core.config;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.service.AdminToolsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.metamodel.EntityType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Configuration
@Transactional
@Slf4j
@NeedCleanCoding
public class DataProvider {
    private final EntityManager em;
    private final AdminToolsService adminToolsService;
    private final Logger logger = LoggerFactory.getLogger(DataProvider.class);
    private static List<File> loaded = new ArrayList<>();
    private static Set<EntityType<?>> loadedToEntityManager = new HashSet<>();
    private static Set<EntityType<?>> tree = new HashSet<>();

    private static final Path jsonsDirectory = Paths.get("src/main/resources/static");
    private static Gson gson = new Gson();

    @Value("${generated.sql.files.path}")
    @Getter
    private String GENERATED_SQL_FILES_PATH;

    @Value("${static.json.folder.path}")
    @Getter
    private String STATIC_JSON_FOLDER_PATH;

    @Autowired
    public DataProvider(EntityManager em,
                        AdminToolsService adminTools) throws IOException {
        this.em = em;
        adminToolsService = adminTools;
        clearAllDatabaseTable();
        loadAllJsonAndSave();
        loaded = new ArrayList<>();
    }

    public long countElementsInTable(String table, String where) {
        EntityManagerFactory factory = em.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();

        Long res = (Long) tempEm.createNativeQuery("SELECT count(*) FROM " + table + " WHERE " + where).getSingleResult();

        tempEm.close();
        return res;
    }

    private void clearAllDatabaseTable() {
        deleteAllRecordsFromAllTable();
        resetIDSequences();
        loaded.clear();
        em.clear();
    }

    private LinkedHashMap<String, String> generateAllSqlsFromJsons() throws IOException {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        List<File> files = Files.walk(jsonsDirectory)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                .map(Path::toFile).toList();
        for (int i = 0; i < files.size(); i++) {
            result.put(files.get(i).getName().substring(0, files.get(i).getName().length() - 5) + ".sql", generateSqlFromJson(files.get(i)));
        }
        return result;
    }

    public void saveAllSqlsFromJsons() throws IOException {
        LinkedHashMap<String, String> fileNameAndFileContent = generateAllSqlsFromJsons();
        File directory = new File(GENERATED_SQL_FILES_PATH);
        deleteFilesInDirectory(directory);
        for (Map.Entry<String, String> entry : fileNameAndFileContent.entrySet()) {
            writeFile(entry.getKey(), entry.getValue());
        }
    }

    public void deleteFilesInDirectory(File directory) {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            file.delete();
        }
    }

    public void writeFile(String fileName, String fileContent) throws IOException {
        File file = new File(GENERATED_SQL_FILES_PATH + "\\" + fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(fileContent);
        }
    }

    public String generateSqlFromJson(File jsonFile) throws IOException {
        try (FileReader reader = new FileReader(jsonFile)) {
            JsonFile json = gson.fromJson(reader, JsonFile.class);
            return json.toSQL();
        }
    }

    private void loadAllJsonAndSave() throws IOException {
        Path directory = Paths.get("src/main/resources/static");

        List<File> files = Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                .map(Path::toFile).collect(Collectors.toList());
        for (File v : files) {
            if (!loaded.contains(v)) {
                loadRequiredJsonAndSave(files, v);
            }
        }
    }

    private void loadRequiredJsonAndSave(List<File> allFiles, File jsonFile) throws IOException {
        if (loaded.contains(jsonFile)) {
            return;
        } else {
            FileReader reader = new FileReader(jsonFile);
            JsonFile json = gson.fromJson(reader, JsonFile.class);
            List<String> required = json.required;

            List<File> requiredFiles = allFiles.stream()
                    .filter(file -> required.contains(file.getName())).toList();
            for (int i = 0; i < requiredFiles.size(); i++) {
                loadRequiredJsonAndSave(allFiles, requiredFiles.get(i));
            }
            saveJsonToDatabase(jsonFile);
        }
    }

    private void saveJsonToDatabase(File jsonFile) throws IOException {
        EntityManagerFactory factory = em.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();
        EntityTransaction entityTransaction = tempEm.getTransaction();
        try (FileReader reader = new FileReader(jsonFile)) {
            JsonFile json = gson.fromJson(reader, JsonFile.class);
            json.init();
            String sql = json.toSQL();
            entityTransaction.begin();
            tempEm.createNativeQuery(sql).executeUpdate();
            tempEm.flush();
            entityTransaction.commit();
            json.setPrimaryKeyStartIfRequired(tempEm);
            logger.info("Data loaded successfully from JSON! " + jsonFile.getName());
            loaded.add(jsonFile);
        } finally {
            tempEm.close();
        }
    }

    private void resetIDSequences() {
        EntityManagerFactory factory = em.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();

        List<String> tableNames = getTableNames();
        EntityTransaction emt = tempEm.getTransaction();
        emt.begin();
        List<String> allSeq = tempEm.createNativeQuery("SELECT relname sequence_name FROM pg_class WHERE relkind = 'S'").getResultList();

        for (String seq : allSeq) {
            if (seq.split("_").length == 3) {
                tempEm.createNativeQuery("ALTER SEQUENCE " + seq + " RESTART WITH 1").executeUpdate();
            }
        }

        emt.commit();
        tempEm.close();
    }

    public void resetDatabase() throws IOException {
        deleteAllRecordsFromAllTable();
        resetIDSequences();
        loaded.clear();
        em.clear();
        loadAllJsonAndSave();
    }

    private void deleteAllRecordsFromAllTable() {
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < getTableNames().size(); i++) {
            sql.append("TRUNCATE \"").append(getTableNames().get(i)).append("\" CASCADE; ");
        }
        executeSQL(sql.toString());
    }

    public void resetTable(File sql) throws IOException {
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(sql))) {
            line = reader.readLine();
        }
        String deleteSQL = "TRUNCATE " + JsonFile.fixObjectName(getTableNameFromInsert(line)).toLowerCase() + " CASCADE;";
        executeSQL(deleteSQL);
        executeSQLFile(sql);
    }

    private String getTableNameFromInsert(String line) {
        Pattern pattern = Pattern.compile("INSERT\\s+INTO\\s+([a-zA-Z0-9_\\.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        matcher.find();
        return matcher.group(1);
    }

    private List<String> getTableNames() {
        EntityManagerFactory factory = em.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();
        List<Object[]> tables = tempEm.createNativeQuery("SELECT * FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'").getResultList();
        List<String> tableNames = new ArrayList<>();
        tables.forEach(v -> tableNames.add((String) v[1]));
        tableNames.remove("databasechangelog");
        tableNames.remove("databasechangeloglock");
        tempEm.close();
        return tableNames;
    }

    public void executeSQL(String sql) {
        EntityManagerFactory factory = em.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();
        EntityTransaction entityTransaction = tempEm.getTransaction();
        entityTransaction.begin();
        tempEm.createNativeQuery(sql).executeUpdate();
        entityTransaction.commit();
        tempEm.close();
    }

    public void executeSQLFile(File sql) throws IOException {
        executeSQL(Files.readString(sql.toPath()));
    }

    @NoArgsConstructor
    public static class JsonFile {
        List<Map<String, Object>> data;
        String objectName;
        ArrayList<String> required;

        @PostConstruct
        public void init() {
            this.objectName = fixObjectName(objectName);
        }

        public static String fixObjectName(String objectName) {
            String fixedObjectName = "";
            switch (objectName) {
                case "Order":
                    fixedObjectName = "orders";
                    break;
                case "User":
                    fixedObjectName = "loginuser";
                    break;
                default:
                    fixedObjectName = objectName;
                    break;
            }
            return fixedObjectName;
        }

        protected String toSQL() {
            List<String> keys = new ArrayList<>(data.get(0).keySet());
            String fieldNames = String.join(", ", keys);
            String baseSql = "INSERT INTO " + objectName + " (" + fieldNames + ") VALUES\n";
            List<String> valueRows = data.stream()
                    .map(obj -> buildValuesRow(obj, keys))
                    .collect(Collectors.toList());
            return (baseSql + String.join(",\n", valueRows)).replaceAll("'(-?\\d+)\\.0'", "'$1'");
        }

        private String buildValuesRow(Map<String, Object> obj, List<String> keys) {
            List<String> valuesList = keys.stream()
                    .map(key -> formatValue(obj.get(key), key))
                    .collect(Collectors.toList());

            return "\t(" + String.join(", ", valuesList) + ")";
        }

        private String formatValue(Object value, String key) {
            if (value instanceof LinkedTreeMap) { //Az EMS-es JSON kötöttebb formátuma miatt mindig van benne refClass vagy date, hogyha LinkedTreeMap
                LinkedTreeMap valueTreeMap = (LinkedTreeMap<?, ?>) value;
                if (valueTreeMap.containsKey("refClass")) {
                    return generateSelectSQLQuery((LinkedTreeMap<String, Object>) value, key);
                } else { //original: valueTreeMap.containsKey("date")
                    return "date('" + ((LinkedTreeMap<?, ?>) value).get("date") + "')";
                }
            } else {
                return value == null ? "NULL" : "'" + value.toString().replace("'", "\\u0027") + "'";
            }
        }

        private static String generateSelectSQLQuery(LinkedTreeMap<String, Object> reference, String columnName) {
            String refClass = fixObjectName((String) reference.get("refClass"));
            LinkedTreeMap<String, Object> filter = (LinkedTreeMap<String, Object>) reference.get("filter");

            String conditions = filter.entrySet().stream()
                    .map(entry -> buildConditionForSelect(entry.getKey(), entry.getValue()))
                    .collect(Collectors.joining(" AND "));

            return "(SELECT id as " + columnName + " FROM " + refClass + " WHERE " + conditions + " LIMIT 1)";
        }

        private static String buildConditionForSelect(String key, Object value) {
            return key + (value instanceof String ? " ILIKE \'" + value + "\'" : " = " + value);
        }


        protected void setPrimaryKeyStartIfRequired(EntityManager em) {
            if (data.get(0).keySet().contains("id")) {
                List<Long> allIds = data.stream().map(v -> ((Double) Double.parseDouble(v.get("id").toString())).longValue()).toList();
                Long lastId = allIds.stream()
                        .mapToLong(v -> v)
                        .max().orElseThrow(NoSuchElementException::new);

                EntityTransaction emt = em.getTransaction();
                emt.begin();
                Object seq = em.createNativeQuery("SELECT pg_get_serial_sequence('" + this.objectName + "', 'id');").getSingleResult();
                String sql = "ALTER SEQUENCE " + seq + " RESTART WITH " + (lastId + 1);
                em.createNativeQuery(sql).executeUpdate();
                emt.commit();
            }
        }
    }
}