import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadTester {

    public static long successfulRequests = 0, totalResponseTime = 0, failedRequests = 0;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        Properties properties = new Properties();
        try (InputStream input = LoadTester.class.getResourceAsStream("config/config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String targetUrl = properties.getProperty("target");
        int frequency = Integer.parseInt(properties.getProperty("frequency"));

        System.out.println("Arguments received targetUrl = " + targetUrl + " frequency = " + frequency);

        while (true) {
            ExecutorService executor = Executors.newFixedThreadPool(frequency);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < frequency; i++) {
                CompletableFuture<Void> primeCheck = CompletableFuture.runAsync(() -> {
                    long startTime = System.currentTimeMillis();
//                    System.out.println(startTime);
                    List<Integer> response = callPrimeCheck(targetUrl);
//                    System.out.println("resposne received " + response);
                    long endTime = System.currentTimeMillis();
                    long responseTime = endTime - startTime;

                    if (response.get(0) == 200) {
                        successfulRequests++;
                        totalResponseTime += response.get(1);
                    } else {
                        failedRequests++;
                    }

                }, executor);
                futures.add(primeCheck);
            }

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            try {
                allOf.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            executor.shutdown();
            System.out.println("Failed requests: " + failedRequests);
            System.out.println("Average response time: " + (totalResponseTime / (double) successfulRequests) + " ms");
        }
    }

    private static List<Integer> callPrimeCheck(String targetUrl) {

        List<Integer> res = new ArrayList<>();
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);

            int statusCode = con.getResponseCode();

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            String[] p = response.toString().split(" ");
            res.add(statusCode);
            res.add(Integer.parseInt(p[1]));
            reader.close();

            return res;
        } catch (Exception e) {
            System.out.println("Exception caught " + e.getMessage());
            res.add(500);
        }

        return res;
    }
}
