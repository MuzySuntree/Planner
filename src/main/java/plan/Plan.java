package plan;

import java.util.*;

public class Plan {
    private final Map<Step, List<Edge>> adj = new HashMap<>();
//    该计划的目标
    private String goal;

//    添加步骤
    public void addStep(Step node) {
        adj.putIfAbsent(node, new ArrayList<>());
    }

//    添加依赖边
    public void addEdge(Step from, Step to, int weight, String type) {
        adj.putIfAbsent(from, new ArrayList<>());
        adj.get(from).add(new Edge(to, weight, type));
    }

    public List<Edge> getEdges(Step node) {
        return adj.getOrDefault(node, Collections.emptyList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(goal).append("\n");
        for (Map.Entry<Step, List<Edge>> entry : adj.entrySet()) {
            Step step = entry.getKey();
            List<Edge> edges = entry.getValue();
            sb.append(step.toString()).append("\n");
            for (Edge edge : edges) {
                sb.append("->").append(edge.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}
