package hu.martin.ems.core.date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Date {

    private static final String[] yearsFormat = new String[]{"yyyy"};
    private static final String[] monthsFormat = new String[]{"MM", "M"};
    private static final String[] daysFormat = new String[]{"dd", "d"};
    private static final String[] separators = new String[]{"/", "-", "."};


    public static List<String> generateAllFormatDate(LocalDate date){
        List<String> generated = new ArrayList<>();
        List<String> allFormat = generateAllFormats();
        for(int i = 0; i < allFormat.size(); i++){
            generated.add(date.format(DateTimeFormatter.ofPattern(allFormat.get(i))));
        }
        return generated;
    }

    public static List<String> generateAllFormats() {
        List<String> allFormat = new ArrayList<>();
        for (String year : yearsFormat) {
            for (String month : monthsFormat) {
                for (String day : daysFormat) {
                    List<String> middleCombinations = generateMiddleCombinations();
                    for (String middle1 : middleCombinations) {
                        for (String middle2 : middleCombinations) {
                            String format = year + middle1 + month + middle2 + day;
                            allFormat.add(format);
                            allFormat.add(format + ".");
                        }
                    }
                }
            }
        }
        return allFormat.stream().filter(v -> !v.contains("MMdd") && !v.contains("yyyyMM")).toList();
    }

    private static List<String> generateMiddleCombinations() {
        List<String> combinations = new ArrayList<>();
        combinations.add(" ");
        combinations.add("");

        for (String sep : separators) {
            combinations.add(sep);
            combinations.add(" " + sep);
            combinations.add(sep + " ");
            combinations.add(" " + sep + " ");
        }

        return combinations;
    }
}
