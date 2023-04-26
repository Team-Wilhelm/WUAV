package gui.model;

import be.Document;
import bll.IManager;
import bll.ManagerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class DocumentModel implements IModel<Document> {
    private static DocumentModel instance;
    private IManager<Document> documentManager;
    private HashMap<UUID, Document> allDocuments;

    private DocumentModel() {
        documentManager = ManagerFactory.createManager(ManagerFactory.ManagerType.DOCUMENT);
        allDocuments = new HashMap<>();
    }

    public static DocumentModel getInstance() {
        if (instance == null) {
            instance = new DocumentModel();
        }
        return instance;
    }

    @Override
    public CompletableFuture<String> add(Document document) {
        String message = documentManager.add(document);

        CompletableFuture<Map<UUID, Document>> future = CompletableFuture.supplyAsync(() -> documentManager.getAll());
        return future.thenApplyAsync(documents -> {
            allDocuments = (HashMap<UUID, Document>) documents;
            return message;
        });
    }

    @Override
    public String update(Document document, CountDownLatch latch) {
        return documentManager.update(document);
    }

    @Override
    public String delete(UUID id) {
        return documentManager.delete(id);
    }

    @Override
    public Map<UUID, Document> getAll() {
        return null;
    }

    @Override
    public Document getById(UUID id) {
        return null;
    }

    public HashMap<UUID, Document> getAllDocuments() {
        return allDocuments;
    }

    public void setAllDocuments(HashMap<UUID, Document> allDocuments) {
        this.allDocuments = allDocuments;
    }

    public List<Document> searchDocuments(String query) {
        List<Document> filteredDocuments = new ArrayList<>();
        allDocuments.values().stream().filter(document ->
                        document.getCustomer().getCustomerName().toLowerCase().contains(query.toLowerCase())
                                || document.getCustomer().getCustomerEmail().toLowerCase().contains(query.toLowerCase())
                                || document.getCustomer().getCustomerAddress().toLowerCase().contains(query.toLowerCase())
                                || document.getCustomer().getCustomerPhoneNumber().toLowerCase().contains(query.toLowerCase())
                                || document.getDateOfCreation().toString().toLowerCase().contains(query.toLowerCase())
                                || document.getTechnicians().stream().allMatch(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                                || document.getTechnicians().stream().allMatch(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                        ).forEach(filteredDocuments::add);
        return filteredDocuments;
    }
}
