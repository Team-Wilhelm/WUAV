package dal.interfaces;

import java.util.Map;
import java.util.UUID;

public interface IDAO<T> {
    String add(T obj);
    String update(T obj);
    String delete(UUID id);
    Map<UUID, T> getAll();
    T getById(UUID id);
}
