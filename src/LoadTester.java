import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadTester {

    public static long successfulRequests = 0, totalResponseTime = 0, failedRequests = 0;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        String targetUrl = args[0];
        int frequency = Integer.parseInt(args[1]);

        System.out.println("Arguments received targetUrl = " + targetUrl + " frequency = " + frequency);

        ExecutorService executor = Executors.newFixedThreadPool(frequency);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < frequency; i++) {
            CompletableFuture<Void> primeCheck = CompletableFuture.runAsync(() -> {
                long startTime = System.currentTimeMillis();
                int responseCode = callPrimeCheck(targetUrl);
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;

                if (responseCode == 200) {
                    successfulRequests++;
                    totalResponseTime += responseTime;
                } else {
                    failedRequests++;
                }

//                System.out.println("Response Code: " + responseCode);
//                System.out.println("Response Time: " + responseTime + "ms");

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
//        System.out.println("Successful requests: " + successfulRequests);
        System.out.println("Failed requests: " + failedRequests);
        System.out.println("Average response time: " + (totalResponseTime / (double) successfulRequests) + "ms");


    }

    private static int callPrimeCheck(String targetUrl) {

        try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);

            int statusCode = con.getResponseCode();

            return statusCode;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return 0;
    }
}
