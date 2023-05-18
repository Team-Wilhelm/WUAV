package bll;

import utils.enums.ResultState;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public interface IManager<T> {
    ResultState add(T obj);
    ResultState update(T obj);
    ResultState delete(UUID id);
    Map<UUID, T> getAll();
    T getById(UUID id);
}
