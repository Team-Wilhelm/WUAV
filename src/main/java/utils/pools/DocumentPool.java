package utils.pools;

import be.Document;

import java.util.Deque;

public class DocumentPool {
    //TODO: Implement DocumentPool
    private static DocumentPool instance;
    private Deque<Document> documentPool;

    private DocumentPool() {}
    public static DocumentPool getInstance() {
        if (instance == null) {
            instance = new DocumentPool();
        }
        return instance;
    }

    public void addDocument() {

    }

}
