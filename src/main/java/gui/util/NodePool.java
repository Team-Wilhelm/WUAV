package gui.util;

import javafx.scene.Node;
import utils.enums.NodeType;

import java.util.HashMap;

public class NodePool {
    private static NodePool instance;
    private HashMap<NodeType, Node> nodePool;

    private NodePool() {
        nodePool = new HashMap<>();
    }

    public static NodePool getInstance() {
        if (instance == null) {
            instance = new NodePool();
        }
        return instance;
    }

    public void addNode(NodeType nodeType, Node node) {
        nodePool.put(nodeType, node);
    }

    public Node getNode(NodeType nodeType) {
        return nodePool.get(nodeType);
    }

    public void removeNode(NodeType nodeType) {
        nodePool.remove(nodeType);
    }
}
