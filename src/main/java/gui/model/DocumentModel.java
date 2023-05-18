package gui.model;

import be.Document;
import be.User;
import gui.nodes.DocumentCard;
import bll.manager.DocumentManager;
import bll.ManagerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DocumentModel implements IModel<Document> {
    private static DocumentModel instance;
    private DocumentManager documentManager;
    private HashMap<UUID, Document> allDocuments;

    private DocumentModel() {
        documentManager = (DocumentManager) ManagerFactory.createManager(ManagerFactory.ManagerType.DOCUMENT);
        allDocuments = new HashMap<>();
        setAllDocuments();
    }

    public static DocumentModel getInstance() {
        if (instance == null) {
            instance = new DocumentModel();
        }
        return instance;
    }

    @Override
    public CompletableFuture<String> add(Document document) {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete(documentManager.add(document));
        if (future.isDone() && future.get().equals("saved")) {
            allDocuments.put(document.getDocumentID(), document);
        }
        future.thenRun(() -> allDocuments.put(document.getDocumentID(), document));
        return future;
    }

    @Override
    public String update(Document document) {

        return documentManager.update(document);
    }

    @Override
    public String delete(UUID id) {
        allDocuments.remove(id);
        return documentManager.delete(id);
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
        this.allDocuments.clear();
        this.allDocuments.putAll(documentManager.getAll());
    }

    public void assignUserToDocument(User user, Document document, boolean isAssigning){
        documentManager.assignUserToDocument(user, document, isAssigning);
    }

    public List<Document> searchDocuments(String query) {
        List<Document> filteredDocuments = new ArrayList<>();
        allDocuments.values().stream().filter(document ->
                        document.getCustomer().getCustomerName().toLowerCase().contains(query)
                                || document.getCustomer().getCustomerEmail().toLowerCase().contains(query)
                                || document.getDateOfCreation().toString().toLowerCase().contains(query)
                                || document.getJobTitle().toLowerCase().contains(query)
                                //|| document.getTechnicians().stream().allMatch(user -> user.getFullName().toLowerCase().contains(query.toLowerCase()))
                                //|| document.getTechnicians().stream().allMatch(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                        ).forEach(filteredDocuments::add);
        return filteredDocuments;
    }
}
