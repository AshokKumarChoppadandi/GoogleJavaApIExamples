package com.bigdata.google.sheets.api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadCsvFile {
    public static void main(String[] args) throws IOException {
        String filePath = args[0];
        List<List<Object>> rows = getInputData(filePath);
        rows.forEach(System.out::println);
    }

    private static List<List<Object>> getInputData(String filePath) {
        List<List<Object>> rows = new ArrayList<>();
        FileReader fileReader;
        try {
            fileReader = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            while ((line = br.readLine()) != null) {
                List<Object> row = new ArrayList<>();
                String[] values = line.split(",");

                String quarter = values[0];
                String areaTeamCode = values[1];
                String areaTeamName = values[2];
                String cCGCode = values[3];
                String cCGName = values[4];
                String chapter = values[5];
                String chapterName = values[6];
                String section = values[7];
                String sectionName = values[8];
                String items = values[9];
                String actualCost = values[10];
                String nic = values[11];

                row.add(quarter);
                row.add(areaTeamCode);
                row.add(areaTeamName);
                row.add(cCGCode);
                row.add(cCGName);
                row.add(chapter);
                row.add(chapterName);
                row.add(section);
                row.add(sectionName);
                row.add(items);
                row.add(actualCost);
                row.add(nic);

                rows.add(row);
            }

            return rows;
        } catch (IOException e) {
            throw new RuntimeException("File Not Found Exception: " + e);
        }
    }
}
