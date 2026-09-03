// TokenList - قالب‌های حساب، با کلید و متن و وضعیت تأییدشان.
//
// برای پیدا کردن TemplateKey که متدهای ارسال قالب لازم دارند. این متد هم مثل
// AccountInfo از بررسی اعتبار معاف است.
//
// روی سرور آزمایشی پیاده نشده و ۴۰۴ می‌دهد؛ همین متد را از سرور عملیاتی صدا
// بزنید، چیزی نمی‌فرستد و اعتباری مصرف نمی‌کند.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... java -cp lib/gson.jar examples/v3/token-list.java

// docs:start
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TokenList {
    public static void main(String[] args) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"));

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/TokenList"))
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
            JsonObject template = item.getAsJsonObject();
            String sendable = template.get("Status").getAsInt() == 2 ? "قابل ارسال" : "قابل ارسال نیست";
            System.out.printf("%s (%s): %s%n",
                    template.get("Key").getAsString(), sendable, template.get("TextTemplate").getAsString());
        });
    }
}
// docs:end
