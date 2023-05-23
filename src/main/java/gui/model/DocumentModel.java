package gui.model;

import be.Document;
import be.User;
import gui.nodes.DocumentCard;
import bll.manager.DocumentManager;
import bll.ManagerFactory;
import utils.enums.ResultState;
import gui.util.drawing.MyShape;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    public ResultState add(Document document) {
        ResultState resultState = documentManager.add(document);
        if (resultState.equals(ResultState.SUCCESSFUL)) {
            allDocuments.put(document.getDocumentID(), document);
        }
        return resultState;
    }

    @Override
    public ResultState update(Document document) {
        ResultState resultState = documentManager.update(document);
        if (resultState.equals(ResultState.SUCCESSFUL)) {
            allDocuments.put(document.getDocumentID(), document);
        }
        return documentManager.update(document);
    }

    @Override
    public ResultState delete(UUID id) {
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
                        ).forEach(filteredDocuments::add);
        return filteredDocuments;
    }

    public void addDrawingToDocument(Document document, String drawing){
        documentManager.addDrawingToDocument(document, drawing);
    }
    public String getDrawingFromDocument(Document document){
        return documentManager.getDrawingFromDocument(document);
    }

    public void reloadDocuments() {
        allDocuments.clear();
        allDocuments.putAll(documentManager.getAll());
    }
}
