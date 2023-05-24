package bll;

import utils.enums.ResultState;

import java.util.Map;
import java.util.UUID;

public interface IManager<T> {
    /**
     * Add an object to the database
     * @param obj object to add
     * @return ResultState
     */
    ResultState add(T obj);
    /**
     * Update an object in the database
     * @param obj object to update
     * @return ResultState
     */
    ResultState update(T obj);
    /**
     * Delete an object from the database
     * @param id id of the object to delete
     * @return ResultState
     */
    ResultState delete(UUID id);
    /**
     * Get all objects from the database
     * @return Map<UUID, T>
     */
    Map<UUID, T> getAll();
    /**
     * Get an object by id from the database
     * @param id id of the object to get
     * @return T
     */
    T getById(UUID id);
}
