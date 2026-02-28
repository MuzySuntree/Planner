package statecentre.model;

//功能名称
public class Action {

//    动作线程锁
    Object lock;

    String name;
//    能够做什么
    String capability;
//    参数
    public class Args{
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
    }

//    输入参数
    Args[] inputArgs;
//    输出结果
    Args outputArgs;
}
