package bll;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public interface IManager<T> {
    String add(T obj);
    String update(T obj);
    String delete(UUID id);
    Map<UUID, T> getAll();
    T getById(UUID id);
}
