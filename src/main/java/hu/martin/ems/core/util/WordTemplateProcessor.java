package hu.martin.ems.core.util;

import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WordTemplateProcessor {

    public static final String TEMPLATE_DIRECTORY = "src/main/resources/templates/";

    public static byte[] fillDocx(String template, Map<String, Object> placeholders) throws IOException {
        FileInputStream fis = new FileInputStream(TEMPLATE_DIRECTORY + template);

        try {
            XWPFDocument doc = new XWPFDocument(fis);

            for (XWPFParagraph p : doc.getParagraphs()) {
                List<XWPFRun> runs = p.getRuns();
                if (runs != null) {
                    for (int i = runs.size() - 1; i >= 0; i--) {
                        XWPFRun r = runs.get(i);
                        String text = r.getText(0);
                        if (text != null && text.contains("##")) {
                            replacePlaceHolder(text, placeholders, r, p);
                        }
                    }
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            out.close();
            doc.close();

            byte[] xwpfDocumentBytes = out.toByteArray();
            try (FileOutputStream stream = new FileOutputStream("./XWPFDocument.docx")) {
                stream.write(xwpfDocumentBytes);
            }
            return xwpfDocumentBytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void replacePlaceHolder(String text, Map<String, Object> placeHolders, XWPFRun r, XWPFParagraph p){
        String[] splitted = text.split("##");
        for(int i = 1; i < splitted.length; i += 2){
            Object replacement = placeHolders.get(splitted[i]);
            splitted[i] = placeHolders.get(splitted[i]).toString();
            if(replacement instanceof String[][]){
                replaceTableInParagraph(p, (String[][]) replacement);
                r.setText("", 0);
                p.removeRun(i);
            }
            else{
                String replacedText = Arrays.stream(splitted).collect(Collectors.joining(""));
                if (!replacedText.equals(text)) {
                    r.setText(replacedText, 0);
                } else {
                    p.removeRun(i);
                }
            }
        }
    }

    private static void replaceTableInParagraph(XWPFParagraph paragraph, String[][] tableData) {
        XWPFDocument doc = (XWPFDocument) paragraph.getBody();

        XWPFTable table = doc.insertNewTbl(paragraph.getCTP().newCursor());
        for(int i = 0; i < tableData.length; i++){
            XWPFTableRow row = table.createRow();
            for(int j = 0; j < tableData[i].length; j++){
                if(j == 0){
                    row.getCell(0).setText(tableData[i][j]);
                }
                else{
                    XWPFTableCell cell = row.addNewTableCell();
                    cell.setText(tableData[i][j]);
                }
            }
        }
        table.removeRow(0);
    }
}