package com.bigdata.google.sheets.api;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ReadAndParseJSONFile {
    public static void main(String[] args) {
        String filePath = args[0];
        List<List<Object>> result = getJsonData(filePath);

        result.forEach(System.out::println);
    }

    public static List<List<Object>> getJsonData(String filePath) {
        List<List<Object>> finalResult = new ArrayList<>();

        FileReader fileReader;
        try {
            fileReader = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            System.out.println("Exception Occurred while reading the input JSON file.");
            throw new RuntimeException("FileNotFoundException :: " + e);
        }
        JsonObject object1 = new JsonParser().parse(fileReader).getAsJsonObject();

        JsonArray columns1 = object1.getAsJsonArray("columns");
        int numOfColumns = columns1.size();

        List<Object> columnsList = new ArrayList<>();
        for (int i = 0; i < numOfColumns; i++) {
            String column = columns1.get(i).getAsString();
            columnsList.add(column);
        }
        finalResult.add(columnsList);

        JsonArray records = object1.getAsJsonArray("table");
        int numOfRecords = records.size();

        for (int i = 0; i < numOfRecords; i++) {
            List<Object> recordsList = new ArrayList<>();
            JsonArray record = records.get(i).getAsJsonArray();
            for (int j = 0; j < numOfColumns; j++) {
                String field = record.get(j).getAsString();
                recordsList.add(field);
            }

            finalResult.add(recordsList);
        }

        return finalResult;
    }
}
