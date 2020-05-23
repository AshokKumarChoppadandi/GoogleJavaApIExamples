package com.bigdata.google.sheets.api;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WriteDataToGoogleSheetFromFile {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String VALUE_INPUT_OPTION = "RAW";

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        // 1. String inputDataLocation = "Path to Your Input CSV File";
        String inputDataLocation = args[0];
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadSheetId = args[1];
        final String email = args[2];

        // 2. Range SYNTAX => SheetName!StartingColumnNumber:EndingColumn
        final String range = "Sheet2!A1:L";
        Sheets service = new Sheets.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT, email)
        ).setApplicationName("WriteDataToGoogleSheetFromFile").build();

        ValueRange body = new ValueRange()
                // 3. New Method getInputData(inputDataLocation) added here.
                .setValues(getInputData(inputDataLocation));
        UpdateValuesResponse response = service
                .spreadsheets()
                .values()
                .update(spreadSheetId, range, body)
                .setValueInputOption(VALUE_INPUT_OPTION)
                .execute();

        System.out.println(response.getUpdatedCells() + " cells updated");

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
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File Not Found Exception: " + e);
        } catch (IOException e) {
            throw new RuntimeException("IOException: " + e);
        }
    }

    private static HttpRequestInitializer getCredentials(NetHttpTransport HTTP_TRANSPORT, String email) throws IOException {
        InputStream in = ReadGoogleSheetData.class.getResourceAsStream("/credentials.json");
        if(in == null) {
            throw new RuntimeException("File Not Found....!!!!");
        }

        GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                secrets,
                SCOPES
        ).setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver serverReceiver = new LocalServerReceiver.Builder().setPort(8888).build();
        // 4. YOUR EMAIL ID
        return new AuthorizationCodeInstalledApp(flow, serverReceiver).authorize(email);
    }
}
