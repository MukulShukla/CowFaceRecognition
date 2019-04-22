package com.cornell.intf.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cornell.azure.azurePackage.AzureBlob;
import com.cornell.drive.drivePackage.DriveService;

public class SyncDriveToAzure {

	public static void main(String[] args) {

		try {

			DriveService service = new DriveService();
			Map<String, String> folders = service.getFolderListFromRoot();
			AzureBlob blob = new AzureBlob();

			for (Map.Entry<String, String> entry : folders.entrySet()) {

				String folderId = entry.getKey();
				String label = entry.getValue();
				System.out.println("Processing Cow Label: " + label);

				List<String> fileIds = service.getFileIdsFromFOlder(folderId);
				int i = 0;

				List<String> filePaths = new ArrayList<String>();

				for (String fileId : fileIds) {
					String fileSavedName = "img_" + label + "_" + Integer.toString(i++) + ".jpg";
					String path = service.downloadFileFromId(fileId, fileSavedName);
					filePaths.add(path);

					ProcessBuilder builder = new ProcessBuilder("sh","/home/mukul/tmp/execute.sh", path);
					File csvFile = new File(path.substring(0, path.indexOf('.'))+".csv");
					csvFile.canWrite();
					builder.redirectOutput(csvFile);
					Process p = builder.start();
					p.waitFor();
					blob.uploadFile(new File(path.substring(0, path.indexOf('.')) + ".csv"));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}

	}

}
