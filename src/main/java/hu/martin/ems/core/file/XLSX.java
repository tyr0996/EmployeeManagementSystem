package hu.martin.ems.core.file;


import hu.martin.ems.annotations.NeedCleanCoding;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@NeedCleanCoding
public class XLSX {
    public static byte[] createExcelFile(String sheetname, String[][] sheetData){
        ArrayList<String[][]> sd = new ArrayList<>();
        sd.add(sheetData);
        return createExcelFile(List.of(sheetname), sd);
    }

    public static byte[] createExcelFile(List<String> sheetNames, List<String[][]> sheetDatas) {
        Workbook workbook = new XSSFWorkbook();
        for (int i = 0; i < sheetNames.size(); i++) {
            Sheet sheet = workbook.createSheet(sheetNames.get(i));
            String[][] sheetData = sheetDatas.get(i);
            for (int j = 0; j < sheetData.length; j++) {
                Row row = sheet.createRow(j);
                for (int k = 0; k < sheetData[j].length; k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(sheetData[j][k]);
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            workbook.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
