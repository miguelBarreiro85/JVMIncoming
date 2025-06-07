package httpserver;
public class HandlerInfo {
    public final Class<?> controllerClass;
    public final String methodName;

    public HandlerInfo(Class<?> className, String m){
        this.controllerClass = className;
        this.methodName = m;
    }
}
