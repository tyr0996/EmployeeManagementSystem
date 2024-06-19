package hu.martin.ems.core.service;

import java.util.List;

public class DataConverter {

    public static String[][] convertListToArray2(List<List<String>> data) {
        int rows = data.size();
        int cols = data.get(0).size();

        String[][] array = new String[rows][cols];

        for (int i = 0; i < rows; i++) { //TODO maybe it can be simpler.
            List<String> row = data.get(i);
            for (int j = 0; j < cols; j++) {
                array[i][j] = row.get(j);
            }
        }

        return array;
    }
}
