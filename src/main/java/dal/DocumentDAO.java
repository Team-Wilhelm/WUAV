package dal;

import be.Document;

import java.util.Map;
import java.util.UUID;

public class DocumentDAO implements IDAO<Document> {
    @Override
    public String add(Document document) {
        String result = "saved";
        return result;
    }

    @Override
    public String update(Document document) {
        String result = "updated";
        return result;
    }

    @Override
    public String delete(UUID id) {
        String result = "deleted";
        return result;
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
