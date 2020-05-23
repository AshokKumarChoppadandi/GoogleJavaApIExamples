package com.bigdata.google.sheets.api;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class ReadGoogleSheetData {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        final NetHttpTransport  HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadSheetId = args[0];
        final String email = args[1];
        final String range = "Sheet1!A1:D";
        Sheets service = new Sheets.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                getCredentials(HTTP_TRANSPORT, email)
        ).setApplicationName("ReadGoogleSheetData").build();

        ValueRange response = service.spreadsheets().values().get(spreadSheetId, range).execute();
        List<List<Object>> values = response.getValues();

        if(values == null || values.isEmpty()) {
            System.out.println("No Data Found...!!!");
        } else {
            for(List<Object> row: values) {
                System.out.println(row.get(0) + ", " + row.get(1) + ", " + row.get(2) + ", " + row.get(3));
            }
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
        return new AuthorizationCodeInstalledApp(flow, serverReceiver).authorize(email);

    }
}
