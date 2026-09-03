// SendMultiple - متن و خط فرستنده جدا برای هر گیرنده.
//
// برای پیام‌های شخصی‌سازی‌شده که با یک قالب ثابت پوشش داده نمی‌شوند. برخلاف
// SendBulk، اینجا Text و Sender در سطح هر گیرنده تعریف می‌شوند.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... PAYAM_RESAN_SENDER=... java -cp lib/gson.jar examples/v3/send-multiple.java

// docs:start
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SendMultiple {
    public static void main(String[] args) throws Exception {
        long sender = Long.parseLong(System.getenv("PAYAM_RESAN_SENDER"));

        JsonArray recipients = new JsonArray();
        recipients.add(recipient(sender, 9121112222L, "آقای محمدی، سفارش شما ارسال شد.", 1001L));
        recipients.add(recipient(sender, 9121113333L, "خانم رضایی، سفارش شما ارسال شد.", 1002L));

        JsonObject payload = new JsonObject();
        payload.addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"));
        payload.add("Recipients", recipients);

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/SendMultiple"))
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

    static JsonObject recipient(long sender, long destination, String text, long userTraceId) {
        JsonObject one = new JsonObject();
        one.addProperty("Sender", sender);
        one.addProperty("Destination", destination);
        one.addProperty("Text", text);
        one.addProperty("UserTraceId", userTraceId);
        return one;
    }
}
// docs:end
