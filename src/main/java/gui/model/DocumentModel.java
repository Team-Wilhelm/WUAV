package gui.model;

import be.Document;
import bll.IManager;
import bll.ManagerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class DocumentModel implements IModel<Document> {
    private static DocumentModel instance;
    private IManager documentManager;

    private DocumentModel() {
        documentManager = ManagerFactory.createManager(ManagerFactory.ManagerType.DOCUMENT);
    }

    public static DocumentModel getInstance() {
        if (instance == null) {
            instance = new DocumentModel();
        }
        return instance;
    }

    @Override
    public void add(Document document) {
        documentManager.add(document);
    }

    @Override
    public void update(Document document) {
        documentManager.update(document);
    }

    @Override
    public void delete(UUID id) {
        documentManager.delete(id);
    }

    @Override
    public Map<UUID, Document> getAll() {
        return null;
    }

    @Override
    public Object getById(UUID id) {
        return null;
    }
}
