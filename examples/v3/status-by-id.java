// StatusById - وضعیت پیامک با شناسه‌هایی که متد ارسال برگردانده است.
//
// دسته‌ای بپرسید، نه یکی‌یکی. فاصله استعلام‌ها را هم کمتر از چند دقیقه
// نگذارید، وگرنه به خطای ۲۰ می‌خورید.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... java -cp lib/gson.jar examples/v3/status-by-id.java

// docs:start
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;

public class StatusById {
    // شرط را روی StatusCode بگذارید، نه روی متن Status. این پنج کد یعنی هنوز
    // در راه است و باید بعداً دوباره استعلام کنید، نه اینکه دوباره بفرستید.
    static final Set<Integer> PENDING = Set.of(0, 1, 2, 3, 10);

    public static void main(String[] args) throws Exception {
        JsonArray ids = new JsonArray();
        ids.add(9903211L);
        ids.add(9903212L);

        JsonObject payload = new JsonObject();
        payload.addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"));
        payload.add("Ids", ids);

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/StatusById"))
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
            String again = PENDING.contains(message.get("StatusCode").getAsInt()) ? " (بعداً دوباره بپرسید)" : "";
            System.out.printf("%s: %s%s%n", message.get("Id"), message.get("Status").getAsString(), again);
        });
    }
}
// docs:end
