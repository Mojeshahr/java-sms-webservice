// SendBulk - یک متن به چند گیرنده، هر کدام با شناسه پی‌گیری خودتان.
//
// روش پیشنهادی برای ارسال عملیاتی. کلید در بدنه درخواست می‌رود نه در نشانی،
// و برای هر گیرنده UserTraceId می‌پذیرد تا گزارش تحویل را بدون نگه‌داشتن Id
// سامانه بگیرید.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... PAYAM_RESAN_SENDER=... java -cp lib/gson.jar examples/v3/send-bulk.java

// docs:start
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SendBulk {
    public static void main(String[] args) throws Exception {
        JsonArray recipients = new JsonArray();
        recipients.add(recipient(9121112222L, 1001L));
        recipients.add(recipient(9121113333L, 1002L));

        JsonObject payload = new JsonObject();
        payload.addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"));
        payload.addProperty("Sender", Long.parseLong(System.getenv("PAYAM_RESAN_SENDER")));
        payload.addProperty("Text", "سفارش شما ثبت شد.");
        payload.add("Recipients", recipients);

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/SendBulk"))
                .header("Content-Type", "application/json; charset=utf-8")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        String raw = http.send(request, HttpResponse.BodyHandlers.ofString()).body();
        JsonObject response = JsonParser.parseString(raw).getAsJsonObject();

        if (!response.has("Success") || !response.get("Success").getAsBoolean()) {
            System.err.printf("ناموفق. کد %s: %s%n", response.get("ErrorCode"), response.get("Error"));
            System.exit(1);
        }

        response.getAsJsonArray("Result").forEach(item -> {
            JsonObject message = item.getAsJsonObject();
            System.out.printf("%s => شناسه %s%n", message.get("UserTraceId"), message.get("Id"));
        });
    }

    static JsonObject recipient(long destination, long userTraceId) {
        JsonObject one = new JsonObject();
        one.addProperty("Destination", destination);
        one.addProperty("UserTraceId", userTraceId);
        return one;
    }
}
// docs:end
