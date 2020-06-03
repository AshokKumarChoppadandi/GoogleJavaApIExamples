/**
 * java -cp GoogleSheetsAPIExamples-1.0-SNAPSHOT.jar com.bigdata.google.sheets.api.WriteJsonFileToGoogleSheet [Application Name] [Spread Sheet Name] [JSON Input File Path] [Credentials.json File Path] [EMAIL_ID]
 */

package com.bigdata.google.sheets.api;

import com.google.api.client.auth.oauth2.Credential;
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
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

public class WriteJsonFileToGoogleSheet3 {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String VALUE_INPUT_OPTION = "RAW";
    private static final String ACCESS_TYPE = "offline";

    private HttpRequestInitializer getCredentials(String credentialFilePath, NetHttpTransport HTTP_TRANSPORT, String EMAIL_ID) {
        InputStream in = ReadGoogleSheetData.class.getResourceAsStream(credentialFilePath);
        if(in == null) {
            throw new RuntimeException("File Not Found....!!!!");
        }

        Credential credential = null;
        LocalServerReceiver serverReceiver = new LocalServerReceiver.Builder().setPort(8888).build();
        try {
            GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT,
                    JSON_FACTORY,
                    secrets,
                    SCOPES
            ).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType(ACCESS_TYPE)
                    .build();
            credential = new AuthorizationCodeInstalledApp(flow, serverReceiver).authorize(EMAIL_ID);
        } catch (IOException e) {
            System.out.println("Exception Occurred while Authorization using the credentials provided...!!!");
        }

        return credential;
    }

    private Sheets getSheetService(String applicationName, String credentialsFilePath, String EMAIL_ID, NetHttpTransport HTTP_TRANSPORT) {
        return new Sheets.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                getCredentials(credentialsFilePath, HTTP_TRANSPORT, EMAIL_ID)
        ).setApplicationName(applicationName).build();
    }

    private Spreadsheet createSpreadSheet(Sheets service, String sheetTitle) {
        Spreadsheet result;
        try {
            Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(sheetTitle));
            result = service.spreadsheets().create(spreadsheet).execute();
        } catch (IOException e) {
            throw new RuntimeException("Exception Occurred while creating the sheet...!!!\n" + e);
        }

        return result;
    }

    public List<List<Object>> getJsonData(String filePath) {
        List<List<Object>> finalResult = new ArrayList<>();

        FileReader fileReader;
        try {
            fileReader = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            System.out.println("Exception Occurred while reading the input JSON file.");
            throw new RuntimeException("FileNotFoundException :: " + e);
        }
        JsonObject object1 = new JsonParser().parse(fileReader).getAsJsonObject();

        Set<String> jsonParentKeys = object1.keySet();
        finalResult.add(new ArrayList<>(jsonParentKeys));
        Set<String> childKeys = object1.getAsJsonObject(jsonParentKeys.iterator().next()).keySet();
        try {
            childKeys.forEach(childKey -> {
                List<Object> row = new ArrayList<>();
                jsonParentKeys.forEach(parentKey -> {
                    String column = object1.getAsJsonObject(parentKey).get(childKey).getAsString();
                    row.add(column);
                });
                finalResult.add(row);
            });
        } catch (Exception ex) {
            System.out.println("Exception Occurred while converting the Input JSON data to List of Objects.\n" + ex);
            throw new RuntimeException(ex);
        }

        return finalResult;
    }

    private void writeToSpreadSheet(Sheets service, String spreadSheetId, String inputDataLocation) {
        final String range = "Sheet1";
        ValueRange body = new ValueRange()
                .setValues(getJsonData(inputDataLocation));
        UpdateValuesResponse response;
        try {
            response = service
                    .spreadsheets()
                    .values()
                    .update(spreadSheetId, range, body)
                    .setValueInputOption(VALUE_INPUT_OPTION)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Exception Occurred while insert / updating the values in Google Spread Sheet : " + spreadSheetId + "\n" + e);
        }

        System.out.println(response.getUpdatedCells() + " cells updated");
    }

    public void run(String[] args) throws GeneralSecurityException, IOException {
        String applicationName = args[0];
        String sheetTitle = args[1];
        String inputDataLocation = args[2];
        String CREDENTIALS_FILE_PATH = args[3];
        String EMAIL_ID = args[4];

        System.out.printf("Input Arguments:: \n1. Application Name :: %s\n2. Sheet Title :: %s\n" +
                        "3. Input Data Location :: %s\n4. Credentials File Path :: %s\n5. Email :: %s\n",
                applicationName, sheetTitle, inputDataLocation, CREDENTIALS_FILE_PATH, EMAIL_ID);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = getSheetService(applicationName, CREDENTIALS_FILE_PATH, EMAIL_ID, HTTP_TRANSPORT);
        System.out.println("Sheets Service Created Successfully. " + service);

        Spreadsheet spreadsheet = createSpreadSheet(service, sheetTitle);
        System.out.println("Spread Sheet Created Successfully. ID : " + spreadsheet.getSpreadsheetId());

        writeToSpreadSheet(service, spreadsheet.getSpreadsheetId(), inputDataLocation);
        System.out.println("Sheet Created and Data has been Written Successfully...!!!");
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        WriteJsonFileToGoogleSheet3 obj = new WriteJsonFileToGoogleSheet3();
        obj.run(args);

        /*String filePath = "C:\\Users\\PC\\IdeaProjects\\GoogleJavaApIExamples\\GoogleSheetsAPIExamples\\src\\main\\resources\\data\\input2.json";
        FileReader fileReader;
        try {
            fileReader = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            System.out.println("Exception Occurred while reading the input JSON file.");
            throw new RuntimeException("FileNotFoundException :: " + e);
        }
        JsonObject object1 = new JsonParser().parse(fileReader).getAsJsonObject();

        List<List<Object>> finalResult = new ArrayList<>();

        Set<String> jsonParentKeys = object1.keySet();
        finalResult.add(new ArrayList<>(jsonParentKeys));

        Set<String> childKeys = object1.getAsJsonObject(jsonParentKeys.iterator().next()).keySet();
        System.out.println("Child Keys :: " + childKeys);

        try {
            childKeys.forEach(childKey -> {
                List<Object> row = new ArrayList<>();
                jsonParentKeys.forEach(parentKey -> {
                    String column = object1.getAsJsonObject(parentKey).get(childKey).getAsString();
                    row.add(column);
                });
                finalResult.add(row);
            });
        } catch (Exception ex) {
            System.out.println("Exception Occurred while converting the Input JSON data to List of Objects.\n" + ex);
            throw new RuntimeException(ex);
        }

        finalResult.forEach(System.out::println);*/
    }
}
