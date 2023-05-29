package gui.model;

import utils.enums.ResultState;

import java.util.Map;
import java.util.UUID;

/**
 * Interface for the model classes
 * @param <T> the type of the model
 */
public interface IModel<T> {
    ResultState add(T obj);
    ResultState update(T obj);
    ResultState delete(UUID id);
    Map<UUID, T> getAll();
    T getById(UUID id);
}
