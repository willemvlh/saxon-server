package tv.mediagenix.xslt.transformer;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class Coverage {

    @Test
    public void postman() throws IOException {
        /*FutureTask<Integer> task = new FutureTask<>(() -> {
            ProcessBuilder pb = new ProcessBuilder();
            String fp = this.getClass().getResource("postman_collection.json").getPath();
            System.out.println(fp);
            pb.command("C:\\Users\\willem.van.lishout\\AppData\\Roaming\\npm\\newman.cmd", "run", fp);
            Process p = pb.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while(p.isAlive()){
                String s;
                while((s = r.readLine()) != null){
                    System.out.println(s);
                }
            }
            return p.exitValue();
        });
        Server s = Server.newServer(new ServerOptions());
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.submit(task);
        s.stop();
        System.out.println("DONE");
*/
    }
}
