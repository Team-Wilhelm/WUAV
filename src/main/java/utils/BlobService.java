package utils;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

public class BlobService {
    private static BlobService instance;
    private final BlobServiceClient blobServiceClient;
    private final String containerName = "wuav";

    private BlobService() {
        Properties prop = new Properties();
        InputStream input = startup.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            prop.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String connectStr = prop.getProperty("AZURE_STORAGE_CONNECTION_STRING");

        blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient();
    }
    public static BlobService getInstance() {
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
    public String UploadFile(String filePath, String fileName, UUID customerId) throws Exception {

        // Create the container and return a container client object
        BlobContainerClient containerClient = this.blobServiceClient.getBlobContainerClient(containerName);

        // Create relative path in the storage for the file.
        var uploadPath = customerId + "/" + UUID.randomUUID() + "--" +fileName.toLowerCase(Locale.ROOT);

        // Get a reference to a blob
        BlobClient blobClient = containerClient.getBlobClient(uploadPath);

        // Upload the blob
        try {
            blobClient.uploadFromFile(filePath + "\\" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blobClient.getBlobUrl();
    }

    /**
     * Uploads a file to the storage
     * @param path Absolute path to the file
     * @param customerId The id of the customer
     * @return path to the file
     */
    public String UploadFile(String path, UUID customerId) throws Exception {
        Path p = Paths.get(path);
        String fileName = p.getFileName().toString();
        String filePath = p.getParent().toString();
        return UploadFile(filePath, fileName, customerId);
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
    public static void main(String[] args) throws IOException {
        // This is just a test
        BlobService blobService = BlobService.getInstance();
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();

        String url = null;
        try {
            url = blobService.UploadFile(s+"\\src\\test", "test.txt", UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var out = blobService.DeleteBlob(url);
    }
}