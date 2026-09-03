// GetInbox - پیامک‌هایی که کاربران به خطوط حساب شما فرستاده‌اند.
//
// این یک استعلام است، نه webhook: سامانه چیزی به سرور شما نمی‌فرستد و باید
// خودتان دوره‌ای صدایش بزنید. فاصله را کمتر از چند دقیقه نگذارید، وگرنه به
// خطای ۲۰ می‌خورید.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... java -cp lib/gson.jar examples/v3/get-inbox.java

// docs:start
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GetInbox {
    public static void main(String[] args) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"));

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/GetInbox"))
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
            JsonObject sms = item.getAsJsonObject();
            // نام فیلد فرستنده در خود سرویس Form است، نه From. دنبال From نگردید.
            System.out.printf("%s  %s -> %s: %s%n",
                    sms.get("Time").getAsString(), sms.get("Form"), sms.get("To"), sms.get("Text").getAsString());
        });
    }
}
// docs:end
