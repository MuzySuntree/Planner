package plan;

public class Edge {
    Step stepTo;
    int weight;   // 可扩展
    String type;  // 依赖类型
//    是否为确定性边或者只是AI临时生成的边
    public enum TaskScope{
        GLOBAL,
        TEMPORARY,
    }
    TaskScope taskScope;

    public Edge(Step stepTo, int weight, String type) {
        this.stepTo = stepTo;
        this.weight = weight;
        this.type = type;
    }

    public void setTaskScope(TaskScope taskScope) {
        this.taskScope = taskScope;
    }

    public TaskScope getTaskScope() {
        return taskScope;
    }
}
