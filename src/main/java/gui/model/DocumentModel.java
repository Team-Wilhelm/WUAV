package gui.model;

import be.Document;
import be.User;
import be.cards.DocumentCard;
import bll.DocumentManager;
import bll.IManager;
import bll.ManagerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class DocumentModel implements IModel<Document> {
    private static DocumentModel instance;
    private DocumentManager documentManager;
    private HashMap<UUID, Document> allDocuments;
    private HashMap<Document, DocumentCard> createdDocumentCards;

    private DocumentModel() {
        long start = System.currentTimeMillis();
        documentManager = (DocumentManager) ManagerFactory.createManager(ManagerFactory.ManagerType.DOCUMENT);
        createdDocumentCards = new HashMap<>();
        setAllDocuments();
        createDocumentCards();
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
    public CompletableFuture<String> update(Document document) {
        String message = documentManager.update(document);
        CompletableFuture<Map<UUID, Document>> future = CompletableFuture.supplyAsync(() -> documentManager.getAll());
        return future.thenApplyAsync(documents -> {
            allDocuments.clear();
            allDocuments.putAll(documents);
            return message;
        });
    }

    @Override
    public CompletableFuture<String> delete(UUID id) {
        String message = documentManager.delete(id);
        CompletableFuture<Map<UUID, Document>> future = CompletableFuture.supplyAsync(() -> documentManager.getAll());
        return future.thenApplyAsync(documents -> {
            allDocuments = (HashMap<UUID, Document>) documents;
            return message;
        });
    }

    @Override
    public Map<UUID, Document> getAll() {
        return allDocuments;
    }

    @Override
    public Document getById(UUID id) {
        return documentManager.getById(id);
    }

    public void setAllDocuments() {
        this.allDocuments = new HashMap<>();
        this.allDocuments.putAll(documentManager.getAll());
    }

    public HashMap<Document, DocumentCard> getCreatedDocumentCards() {
        return createdDocumentCards;
    }

    public void createDocumentCards() {
        for (Document document: allDocuments.values()){
            if(!createdDocumentCards.containsKey(document)){
                createdDocumentCards.put(document, new DocumentCard(document));
            }
        }
    }

    public void assignUserToDocument(User user, Document document, boolean isAssigning){
        documentManager.assignUserToDocument(user, document, isAssigning);
    }

    public List<Document> searchDocuments(String query) {
        List<Document> filteredDocuments = new ArrayList<>();
        allDocuments.values().stream().filter(document ->
                        document.getCustomer().getCustomerName().toLowerCase().contains(query.toLowerCase())
                                || document.getCustomer().getCustomerEmail().toLowerCase().contains(query.toLowerCase())
                                || document.getCustomer().getCustomerPhoneNumber().toLowerCase().contains(query.toLowerCase())
                                || document.getDateOfCreation().toString().toLowerCase().contains(query.toLowerCase())
                                //|| document.getTechnicians().stream().allMatch(user -> user.getFullName().toLowerCase().contains(query.toLowerCase()))
                                //|| document.getTechnicians().stream().allMatch(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                        ).forEach(filteredDocuments::add);
        return filteredDocuments;
    }
}
