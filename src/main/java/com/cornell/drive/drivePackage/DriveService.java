package com.cornell.drive.drivePackage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveService {
	private static final String APPLICATION_NAME = "GDrive";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_READONLY);
	private static final String CREDENTIALS_FILE_PATH = "credentials.json";

	Drive service;

	public DriveService() throws GeneralSecurityException, IOException {

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
	}

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

		InputStream in = DriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public Map<String, String> getFolderListFromRoot() throws IOException {

		Map<String,String> folders = new HashMap<String, String>();
		FileList result = service.files().list().setQ(
				"'1UBiQ01hpdW9HaacDqOvwUywWyC9D_3Ni' in parents and trashed = false and mimeType = 'application/vnd.google-apps.folder'")
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();

		List<File> files = result.getFiles();

		for (File file : files) {
			folders.put(file.getId(), file.getName());
			System.out.println("Found folder "+file.getId()+" with name "+ file.getName());
		}

		return folders;
	}

	public List<String> getFileIdsFromFOlder(String folderId) throws IOException {
		List<String> resultIds = new ArrayList<String>();
		FileList result = service.files().list()
				.setQ("'" + folderId
						+ "' in parents and trashed = false and mimeType != 'application/vnd.google-apps.folder'")
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();

		List<File> files = result.getFiles();

		for (File file : files) {
			resultIds.add(file.getId());
		}

		return resultIds;
	}

	public String downloadFileFromId(String fileId, String storeAsName) throws IOException {

		String filePath = "/home/mukul/tmp/" + storeAsName;
		OutputStream out = new FileOutputStream(filePath);
		service.files().get(fileId).executeMediaAndDownloadTo(out);
		return filePath;
	}

}