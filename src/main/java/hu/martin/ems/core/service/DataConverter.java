package hu.martin.ems.core.service;

import java.util.List;

public class DataConverter {

    public static String[][] convertListToArray2(List<List<String>> data) {
        String[][] array = new String[data.size()][];

        for (int i = 0; i < data.size(); i++) { //TODO maybe it can be simpler.
            List<String> row = data.get(i);
            String[] r = new String[row.size()];
            for (int j = 0; j < row.size(); j++) {
                r[j] = data.get(i).get(j);
            }
            array[i] = r;
        }

        return array;
    }
}
