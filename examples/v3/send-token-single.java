// SendTokenSingle - ارسال قالب به یک شماره، با بدنه JSON.
//
// مسیر معمول رمز یک‌بارمصرف. خط فرستنده ورودی ندارد؛ سامانه آن را از روی خود
// قالب برمی‌دارد. همین واریانت POST را به کار ببرید، نه GET: در GET هم کلید
// حساب و هم خود رمز داخل نشانی و لاگ وب‌سرور می‌نشینند.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... java -cp lib/gson.jar examples/v3/send-token-single.java

// docs:start
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SendTokenSingle {
    public static void main(String[] args) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"));
        payload.addProperty("TemplateKey", "verifycode");
        payload.addProperty("Destination", 9121112222L);
        payload.addProperty("p1", "123456");

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/SendTokenSingle"))
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

        // این متد UserTraceId در ورودی ندارد، پس در پاسخ null برمی‌گردد. اگر
        // شناسه پی‌گیری لازم دارید، SendTokenMulti را حتی برای یک گیرنده هم
        // می‌شود به کار برد.
        response.getAsJsonArray("Result").forEach(item -> {
            JsonObject message = item.getAsJsonObject();
            System.out.printf("شناسه %s از خط %s%n", message.get("Id"), message.get("Sender"));
            System.out.println("متن نهایی: " + message.get("FinalText").getAsString());
        });
    }
}
// docs:end
