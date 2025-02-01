//package hu.martin.ems.core.config;
//
//import lombok.Getter;
//import org.hibernate.resource.jdbc.spi.StatementInspector;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class SQLInspector implements StatementInspector {
//    @Getter    private List<String> executedQueries = new ArrayList<>();
//
//    @Override
//    public String inspect(String sql) {
//        executedQueries.add(sql);
//        System.out.println("Captured SQL: " + sql);
//        return sql;
//    }
//
//    private void printExecutedQueries(){
//        for(int i = 0; i < executedQueries.size(); i++){
//            System.out.println("Query #" + i + ": " + executedQueries.get(i));
//        }
//    }
//}
