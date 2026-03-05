package statecentre.model;

import java.util.ArrayList;
import java.util.List;

//功能名称
public class Action {

//    动作线程锁
    Object lock;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapability() {
        return capability;
    }

    public void setCapability(String capability) {
        this.capability = capability;
    }

    public List<Args> getInputArgs() {
        return inputArgs;
    }

    public void addInputArgs(Args inputArgs) {
        this.inputArgs.add(inputArgs);
    }

    public Args getOutputArgs() {
        return outputArgs;
    }

    public void setOutputArgs(Args outputArgs) {
        this.outputArgs = outputArgs;
    }

    String name;
//    能够做什么
    String capability;
//    参数
    public static class Args{
        public enum ArgsType{
            STRING,
            INTEGER,
            FLOAT,
        }
//        参数类型
        ArgsType argsType;
//        参数说明
        String name;
//        参数值
        String value;

        public Args(ArgsType argsType, String name, String value) {
            this.argsType = argsType;
            this.name = name;
            this.value = value;
        }
        public Args(ArgsType argsType, String name) {
            this.argsType = argsType;
            this.name = name;
            this.value = value;
        }
        public Args(){}

    public ArgsType getArgsType() {
        return argsType;
    }

    public void setArgsType(ArgsType argsType) {
        this.argsType = argsType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

//    输入参数
    List<Args> inputArgs = new ArrayList<Args>();
//    输出结果
    Args outputArgs;
}
