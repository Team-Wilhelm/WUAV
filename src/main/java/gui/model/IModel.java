package gui.model;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public interface IModel<T> {
    CompletableFuture<String> add(T obj);
    String update(T obj, CountDownLatch latch);
    String delete(UUID id);
    Map<UUID, T> getAll();
    T getById(UUID id);

}
