package gui.model;

import be.Customer;
import be.Document;
import be.User;
import bll.ManagerFactory;
import bll.manager.DocumentManager;
import utils.enums.BusinessEntityType;
import utils.enums.ResultState;

import java.util.*;

public class DocumentModel implements IModel<Document> {
    private static DocumentModel instance;
    private CustomerModel customerModel;
    private DocumentManager documentManager;
    private HashMap<UUID, Document> allDocuments;

    private DocumentModel() {
        documentManager = (DocumentManager) ManagerFactory.createManager(BusinessEntityType.DOCUMENT);
        allDocuments = new HashMap<>();
        customerModel = CustomerModel.getInstance();
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
        addOrUpdateCustomer(document);
        ResultState resultState = documentManager.add(document);
        if (resultState.equals(ResultState.SUCCESSFUL)) {
            allDocuments.put(document.getDocumentID(), document);
            customerModel.put(document.getCustomer());
        }
        return resultState;
    }

    @Override
    public ResultState update(Document document) {
        addOrUpdateCustomer(document);
        ResultState resultState = documentManager.update(document);
        if (resultState.equals(ResultState.SUCCESSFUL)) {
            allDocuments.put(document.getDocumentID(), document);
            customerModel.put(document.getCustomer());
        }
        return resultState;
    }

    @Override
    public ResultState delete(UUID id) {
        ResultState resultState = documentManager.delete(id);
        if (resultState.equals(ResultState.SUCCESSFUL)) {
            Document d = allDocuments.remove(id);

            Customer c = CustomerModel.getInstance().getById(d.getCustomer().getCustomerID());
            if (c != null)
                CustomerModel.getInstance().getById(d.getCustomer().getCustomerID()).getContracts().remove(id);
        }
        return resultState;
    }

    @Override
    public Map<UUID, Document> getAll() {
        return allDocuments;
    }

    @Override
    public Document getById(UUID id) {
        return allDocuments.get(id);
    }

    public void setAllDocuments() {
        this.allDocuments.clear();
        this.allDocuments.putAll(documentManager.getAll());

        CustomerModel customerModel = CustomerModel.getInstance();
        for (Document document : allDocuments.values()) {
            if (customerModel.getById(document.getCustomer().getCustomerID()) == null) {
                customerModel.put(document.getCustomer());
            } else {
                customerModel.addContract(document.getCustomer().getCustomerID(), document);
            }
        }
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


    public void addOrUpdateCustomer(Document document) {
        if (document.getCustomer().getCustomerID() == null) {
            customerModel.add(document.getCustomer());
        } else {
            customerModel.update(document.getCustomer());
        }
    }

    public void deleteDocumentsByCustomer(UUID customerID) {
        List<Document> documentsToDelete = new ArrayList<>();
        for (Document document : allDocuments.values()) {
            if (document.getCustomer().getCustomerID().equals(customerID)) {
                documentsToDelete.add(document);
            }
        }
        for (Document document : documentsToDelete) {
            delete(document.getDocumentID());
        }
    }
}
