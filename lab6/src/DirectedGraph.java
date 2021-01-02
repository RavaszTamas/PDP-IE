import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class DirectedGraph {

    private Map<Integer, List<Integer>> edges;
    private List<Integer> nodes;
    DirectedGraph(int numberOfNodes){
        nodes = new ArrayList<>();
        edges = new HashMap<>();
        IntStream.range(0,numberOfNodes).forEach(integer->{
            edges.put(integer,new ArrayList<>());
            nodes.add(integer);
        });
    }

    void addEdge(int source, int destination)
    {
        if(!edges.get(source).contains(destination))
            edges.get(source).add(destination);
    }

    List<Integer> getNeighboursOfNode(int nodeId){
        return edges.get(nodeId);
    }

    List<Integer> getNodes(){
        return nodes;
    }

    int numberOfNodes(){
        return nodes.size();
    }

    @Override
    public String toString() {
        return "DirectedGraph{" +
                "edges=" + edges +
                ", nodes=" + nodes +
                '}';
    }
}
