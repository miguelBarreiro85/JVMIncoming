package app;

import app.controllers.TestController;
import httpserver.HandlerInfo;
import httpserver.Server;

public class Main {

  public static String fileDir;
  
  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");
    Main.fileDir = args.length >= 1 ? args[1] : "/tmp";

    Main.fileDir = args.length >= 1 ? args[1] : null;
    Server.get("/", new HandlerInfo(TestController.class, "getHome"));
    Server.get("/echo/{word}", new HandlerInfo(TestController.class, "getEcho"));
    Server.get("/user-agent", new HandlerInfo(TestController.class, "handleUserAgent"));
    Server.get("/files/{filename}", new HandlerInfo(TestController.class, "handleGetFile"));
    Server.post("/files/{filename}", new HandlerInfo(TestController.class, "handlePostFile"));
    Server.listen(4221);
  }
}
