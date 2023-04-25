package dao;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface IDAO<T> {
    void add(T obj);
    void update(T obj);
    void delete(UUID id);
    Map<UUID, T> getAll();
    Object getById(UUID id);
}
