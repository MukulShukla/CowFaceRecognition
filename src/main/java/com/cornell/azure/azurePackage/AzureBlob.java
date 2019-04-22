package com.cornell.azure.azurePackage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.ListBlobsOptions;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.azure.storage.blob.TransferManager;
import com.microsoft.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.storage.blob.models.ContainerCreateResponse;
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse;
import com.microsoft.rest.v2.RestException;

import io.reactivex.Single;

public class AzureBlob {

	String accountName;
	String accountKey;
	SharedKeyCredentials creds;
	final ServiceURL serviceURL;
	ContainerURL containerURL;

	public AzureBlob() throws InvalidKeyException, MalformedURLException {

		accountName = "cowimagestore";
		accountKey = "zgjzOJxE3t0+NeVSz6zuc4ZL4o++VPLeTtYDA4NEvR8Emkjt/YwdNZc+TUzr8eei7+SQnNSsrerDsRRq5ViTIg==";
		creds = new SharedKeyCredentials(accountName, accountKey);
		serviceURL = new ServiceURL(new URL("https://" + accountName + ".blob.core.windows.net"),
				StorageURL.createPipeline(creds, new PipelineOptions()));
		containerURL = serviceURL.createContainerURL("images");

		try {
			ContainerCreateResponse response = containerURL.create(null, null, null).blockingGet();
			System.out.println("Container Create Response was " + response.statusCode());
		} catch (RestException e) {
			if (e instanceof RestException && ((RestException) e).response().statusCode() != 409) {
				throw e;
			} else {
				System.out.println("images container already exists, resuming...");
			}
		}
	}

	public void uploadFile(File sourceFile) throws IOException {

		BlockBlobURL blobURL = containerURL.createBlockBlobURL(sourceFile.getName());
		AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(sourceFile.toPath());

		TransferManager.uploadFileToBlockBlob(fileChannel, blobURL, 8 * 1024 * 1024, null, null).subscribe(response -> {
			System.out.println("Completed upload request.");
			System.out.println(response.response().statusCode());
		});
	}

	static void listBlobs(ContainerURL containerURL) {

		ListBlobsOptions options = new ListBlobsOptions();
		options.withMaxResults(10);

		containerURL.listBlobsFlatSegment(null, options, null)
				.flatMap(containerListBlobFlatSegmentResponse -> listAllBlobs(containerURL,
						containerListBlobFlatSegmentResponse))
				.subscribe(response -> {
					System.out.println("Completed list blobs request.");
					System.out.println(response.statusCode());
				});
	}

	private static Single<ContainerListBlobFlatSegmentResponse> listAllBlobs(ContainerURL url,
			ContainerListBlobFlatSegmentResponse response) {
		// Process the blobs returned in this result segment (if the segment is empty,
		// blobs() will be null.
		if (response.body().segment() != null) {
			for (BlobItem b : response.body().segment().blobItems()) {
				String output = "Blob name: " + b.name();
				if (b.snapshot() != null) {
					output += ", Snapshot: " + b.snapshot();
				}
				System.out.println(output);
			}
		} else {
			System.out.println("There are no more blobs to list off.");
		}

		if (response.body().nextMarker() == null) {
			return Single.just(response);
		} else {
			/*
			 * IMPORTANT: ListBlobsFlatSegment returns the start of the next segment; you
			 * MUST use this to get the next segment (after processing the current result
			 * segment
			 */

			String nextMarker = response.body().nextMarker();

			/*
			 * The presence of the marker indicates that there are more blobs to list, so we
			 * make another call to listBlobsFlatSegment and pass the result through this
			 * helper function.
			 */

			return url.listBlobsFlatSegment(nextMarker, new ListBlobsOptions().withMaxResults(10), null).flatMap(
					containersListBlobFlatSegmentResponse -> listAllBlobs(url, containersListBlobFlatSegmentResponse));
		}
	}

	static void getBlob(BlockBlobURL blobURL, File sourceFile) throws IOException {
		AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(sourceFile.toPath(),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE);

		TransferManager.downloadBlobToFile(fileChannel, blobURL, null, null).subscribe(response -> {
			System.out.println("Completed download request.");
			System.out.println("The blob was downloaded to " + sourceFile.getAbsolutePath());
		});
	}

}
