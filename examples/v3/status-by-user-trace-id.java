// StatusByUserTraceId - وضعیت پیامک با شناسه‌هایی که خودتان داده‌اید.
//
// اگر UserTraceId را کلید رکورد پایگاه داده خودتان بگذارید، دیگر لازم نیست Id
// سامانه را ذخیره کنید. این متد راه امن تشخیص ارسال تکراری هم هست: بعد از قطع
// ارتباط، اول اینجا بپرسید ثبت شده یا نه.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... java -cp lib/gson.jar examples/v3/status-by-user-trace-id.java

// docs:start
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class StatusByUserTraceId {
    public static void main(String[] args) throws Exception {
        JsonArray traceIds = new JsonArray();
        traceIds.add(1001L);
        traceIds.add(1002L);

        JsonObject payload = new JsonObject();
        payload.addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"));
        payload.add("UserTraceIds", traceIds);

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/StatusByUserTraceId"))
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

            // کد ۸ یعنی این شناسه در حساب شما نیست. بعد از یک timeout، همین
            // یعنی ارسال ثبت نشده و می‌توانید با خیال راحت دوباره بفرستید.
            if (message.get("StatusCode").getAsInt() == 8) {
                System.out.println(message.get("UserTraceId") + ": ثبت نشده");
                return;
            }

            System.out.printf("%s: %s%n", message.get("UserTraceId"), message.get("Status").getAsString());
        });
    }
}
// docs:end
