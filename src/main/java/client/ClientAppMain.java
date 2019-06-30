package client;

public class ClientAppMain {
    public static void main(String[] args) {
        try{
            new ClientApp().run(args);
        }catch(IllegalArgumentException e){
            System.err.println(e.getMessage());
            System.err.println("Usage: java -jar <path_to_jar> <host> <?port?>");
        }
    }
}
