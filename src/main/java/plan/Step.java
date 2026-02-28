package plan;

public class Step {
    public enum TaskTimes{
        IMMEDIATE,              //立即执行
        TIMEOUT,                //延迟执行
        INTERVAL,                //周期执行
        CONDITIONAL             //条件性执行
    }
    String goal;                //当前目标
    TaskTimes taskTimes;

    @Override
    public String toString() {
        StringBuilder  sb = new StringBuilder("Step{");
        sb.append("goal='").append(goal).append('\'');
        sb.append(this.taskTimes).append("}\n");
        return sb.toString();
    }
}
