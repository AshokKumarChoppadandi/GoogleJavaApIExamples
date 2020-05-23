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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WriteDataToGoogleSheet {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String VALUE_INPUT_OPTION = "RAW";

    public static void main(String[] args) throws GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadSheetId = args[0];
        final String email = args[1];
        final String range = "Sheet1!A5:D";
        Sheets service = new Sheets.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT, email)
        ).setApplicationName("WriteDataToGoogleSheet").build();

        ValueRange body = new ValueRange()
                .setValues(generateTestData());
        UpdateValuesResponse response = service
                .spreadsheets()
                .values()
                .update(spreadSheetId, range, body)
                .setValueInputOption(VALUE_INPUT_OPTION)
                .execute();

        System.out.println(response.getUpdatedCells() + " cells updated");

    }

    private static List<List<Object>> generateTestData() {
        List<Object> row1 = new ArrayList<>();
        row1.add(456);
        row1.add("TEST2");
        row1.add(4000);
        row1.add(101);

        List<Object> row2 = new ArrayList<>();
        row2.add(789);
        row2.add("TEST3");
        row2.add(5000);
        row2.add(102);

        List<List<Object>> values = new ArrayList<>();
        values.add(row1);
        values.add(row2);

        return values;
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
        return new AuthorizationCodeInstalledApp(flow, serverReceiver).authorize(email);

    }
}
