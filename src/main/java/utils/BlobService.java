package utils;

import java.util.Locale;

import com.azure.storage.blob.*;

import java.util.UUID;

public class BlobService {
    private static BlobService instance;
    private final BlobServiceClient blobServiceClient;
    private final String containerName = "wuav";
    private BlobService() {
        String connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
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

        return blobClient.getBlobUrl();
    }

    /**
     * Deletes a blob from the storage
     * @param blobUrl The url of the blob to delete
     * @return True if the blob was deleted, false if not
     */

    public boolean DeleteBlob(String blobUrl) {
        blobUrl = blobUrl.replace("https://easvprojects.blob.core.windows.net/wuav/", "").replace("%2F", "/");
        BlobClient blobClient = this.blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobUrl);
        return blobClient.deleteIfExists();
    }
}

class startup{
    public static void main(String[] args) {
        // This is just a test
        // The file test.txt needs to be in the folder C:\Users\matej\EASV\CSe22\2ndSemester\WUAV\src\test
        // TODO: Make this into unit tests
        BlobService blobService = BlobService.getInstance();
        var url = blobService.UploadFile("C:\\Users\\matej\\EASV\\CSe22\\2ndSemester\\WUAV\\src\\test", "test.txt", UUID.randomUUID());
        var out = blobService.DeleteBlob(url);
        System.out.println(out);
    }
}