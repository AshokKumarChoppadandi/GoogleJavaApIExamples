/**
 * https://console.developers.google.com/
 *
 * mvn clean install
 * java -cp GoogleSheetsAPIExamples-1.0-SNAPSHOT.jar com.bigdata.google.sheets.api [APPLICATION_NAME] [SHEET_TITLE] [INPUT_DATA_LOCATION] [CREDENTIALS_FILE_PATH] [EMAIL_ID]
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

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateAndWriteToGoogleSheet {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String VALUE_INPUT_OPTION = "RAW";
    private static final String ACCESS_TYPE = "offline";

    private static HttpRequestInitializer getCredentials(String credentialFilePath, NetHttpTransport HTTP_TRANSPORT, String EMAIL_ID) {
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

    private static Sheets getSheetService(String applicationName, String credentialsFilePath, String EMAIL_ID, NetHttpTransport HTTP_TRANSPORT) {
        return new Sheets.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                getCredentials(credentialsFilePath, HTTP_TRANSPORT, EMAIL_ID)
        ).setApplicationName(applicationName).build();
    }

    private static Spreadsheet createSpreadSheet(Sheets service, String sheetTitle) {
        Spreadsheet result;
        try {
            Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(sheetTitle));
            result = service.spreadsheets().create(spreadsheet).execute();
        } catch (IOException e) {
            throw new RuntimeException("Exception Occurred while creating the sheet...!!!\n" + e);
        }

        return result;
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

    private static void writeToSpreadSheet(Sheets service, String spreadSheetId, String inputDataLocation) {
        final String range = "Sheet1!A1:L";
        ValueRange body = new ValueRange()
                .setValues(getInputData(inputDataLocation));
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

    public static void main(String[] args) throws GeneralSecurityException, IOException {
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
}
