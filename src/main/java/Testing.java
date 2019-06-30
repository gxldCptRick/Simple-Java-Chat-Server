import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;

public class Testing {
    public static void main(String[] args) throws IOException, InterruptedException {
        var buffy = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            if(buffy.ready()){
                System.out.println("Received this on the stream: " + buffy.readLine());
            }
            if(ThreadLocalRandom.current().nextInt(0,1_000_000) > 999_000){
                System.out.println("milo: Hi");
            }
            Thread.sleep(100);
        }
    }
}
