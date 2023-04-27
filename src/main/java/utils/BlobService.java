package utils;

import java.util.Locale;

import com.azure.storage.blob.*;

import java.util.UUID;

public class BlobService {
    private String connectStr;
    private BlobServiceClient blobServiceClient;
    private String containerName = "wuav";
    public BlobService() {
        connectStr = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient();
    }
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
}

class startup{
    public static void main(String[] args) {
        BlobService blobService = new BlobService();
        var Url = blobService.UploadFile("C:\\Users\\matej\\EASV\\CSe22\\2ndSemester\\WUAV\\src\\test", "test.txt", UUID.randomUUID());
    }
}