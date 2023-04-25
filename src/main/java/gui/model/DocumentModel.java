package gui.model;

import be.Document;
import bll.IManager;
import bll.ManagerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class DocumentModel implements IModel<Document> {
    private static DocumentModel instance;
    private IManager<Document> bll;

    private DocumentModel() {
        bll = ManagerFactory.createManager(ManagerFactory.ManagerType.DOCUMENT);
    }

    public static DocumentModel getInstance() {
        if (instance == null) {
            instance = new DocumentModel();
        }
        return instance;
    }

    @Override
    public String add(Document document) {
        return bll.add(document);
    }

    @Override
    public String update(Document document) {
        return bll.update(document);
    }

    @Override
    public String delete(UUID id) {
        return bll.delete(id);
    }

    @Override
    public Map<UUID, Document> getAll() {
        return null;
    }

    @Override
    public Document getById(UUID id) {
        return null;
    }
}
