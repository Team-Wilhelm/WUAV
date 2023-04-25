package bll;

import be.Document;
import dao.DAOFactory;
import dao.DocumentDAO;

import java.util.Map;
import java.util.UUID;

public class DocumentManager implements IManager<Document> {
    private DocumentDAO dao;

    public DocumentManager() {
        dao = (DocumentDAO) DAOFactory.createDAO(DAOFactory.DAOType.DOCUMENT);
    }

    @Override
    public String add(Document document) {
        return dao.add(document);
    }

    @Override
    public String update(Document document) {
        return dao.update(document);
    }

    @Override
    public String delete(UUID id) {
        return dao.delete(id);
    }

    @Override
    public Map<UUID, Document> getAll() {
        return dao.getAll();
    }

    @Override
    public Document getById(UUID id) {
        return null;
    }
}
