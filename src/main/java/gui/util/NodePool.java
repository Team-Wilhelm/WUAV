package gui.util;

import javafx.scene.Node;
import utils.enums.NodeType;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

public class NodePool {
    private static NodePool instance;
    private HashMap<NodeType, Deque<Node>> nodePool;

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
        nodePool.get(nodeType).add(node);
    }

    public Node getNode(NodeType nodeType) {
        return nodePool.get(nodeType).getFirst();
    }
}
