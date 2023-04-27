package utils;

import java.sql.Blob;
import java.util.Locale;

import com.azure.storage.blob.*;

import java.util.UUID;

public class BlobService {
    private static BlobService instance;
    private String connectStr;
    private BlobServiceClient blobServiceClient;
    private String containerName = "wuav";
    private BlobService() {
        connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient();
    }
    public static BlobService getInstance()
    {
        if (instance == null)
            instance = new BlobService();

        return instance;
    }

    /**
     * Uploads a file to the storage
     * @param filePath The path to the file without the file name
     * @param fileName The name of the file
     * @param customerId The id of the customer
     * @return The url of the blob
     */
    public String UploadFile(String filePath, String fileName, UUID customerId) {

        // Create the container and return a container client object
        BlobContainerClient containerClient = this.blobServiceClient.getBlobContainerClient(containerName);

        // Create relative path in the storage for the file.
        var uploadPath = customerId + "/" + UUID.randomUUID() + "-" +fileName.toLowerCase(Locale.ROOT);

        // Get a reference to a blob
        BlobClient blobClient = containerClient.getBlobClient(uploadPath);

        // Upload the blob
        blobClient.uploadFromFile(filePath + "\\" + fileName);
        var blobUrl = blobClient.getBlobUrl();
        System.out.println(blobUrl);
        return blobUrl;
    }

    /**
     * Deletes a blob from the storage
     * The Url needs to be without the container name: https://wuav.blob.core.windows.net/wuav/
     * @param blobUrl The url of the blob to delete
     * @return True if the blob was deleted, false if not
     */

    public boolean DeleteBlob(String blobUrl) {
        BlobClient blobClient = this.blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobUrl);
        return blobClient.deleteIfExists();
    }
}

class startup{
    public static void main(String[] args) {
        BlobService blobService = BlobService.getInstance();
        var Url = blobService.UploadFile("C:\\Users\\matej\\EASV\\CSe22\\2ndSemester\\WUAV\\src\\test", "test.txt", UUID.randomUUID());
        var out = blobService.DeleteBlob("a812f7b0-284f-4494-a139-631db6dc49f9/a8a48ace-acc9-4eaa-bd32-d58c67aa0023-test.txt");
        System.out.println(out);
    }
}