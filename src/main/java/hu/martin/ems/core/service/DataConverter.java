package hu.martin.ems.core.service;

import java.util.List;

public class DataConverter {

    public static String[][] convertListToArray2(List<List<String>> data) {
        String[][] array = new String[data.size()][];

        for (int i = 0; i < data.size(); i++) {
            List<String> row = data.get(i);
            array[i] = row.toArray(new String[0]);
        }

        return array;
    }
}
