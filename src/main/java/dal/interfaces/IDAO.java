package dal.interfaces;

import utils.enums.ResultState;

import java.util.Map;
import java.util.UUID;

public interface IDAO<T> {
    ResultState add(T obj);
    ResultState update(T obj);
    ResultState delete(UUID id);
    Map<UUID, T> getAll();
    T getById(UUID id);
}
