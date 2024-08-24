package Parser;

public class Node {
    
    public NodeType type;
    public String value;
    public int childrenCount;

    public Node(NodeType type, String value, int children) {
        this.type = type;
        this.value = value;
        this.childrenCount = children;
    }
}
