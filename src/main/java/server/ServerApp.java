package server;

import configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

public class ServerApp {
    public void run(String[] args) {
       var argMap = createMapOfArgs(args);
       var server = new ChatServer(Integer.parseInt(argMap.get("port")));
       server.run();
    }

    private Map<String, String> createMapOfArgs(String[] args) {
        if(args.length > 1) throw new IllegalArgumentException("Too many arguments passed in. Only port argument should be passed in.");
        var map = new HashMap<String, String>();
        if(args.length == 1){
            if(args[0].startsWith("port=")){
                map.put("port", args[0].split("=")[1]);
            }else{
                throw new IllegalArgumentException("port argument is not formatted correctly. Port argument should be specified as 'port=<port number>'");
            }
        }else{
            map.put("port", String.valueOf(Configuration.BIND_PORT));
        }
        return map;
    }
}
