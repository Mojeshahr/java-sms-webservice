// SendTokenSingle با GET - همان ارسال قالب، با ورودی در نشانی.
//
// برای آزمایش دستی مناسب است، برای محیط عملیاتی نه: در GET هم کلید حساب و هم
// مقدار رمز یک‌بارمصرف داخل نشانی می‌نشینند و در لاگ وب‌سرور و هدر Referer
// ثبت می‌شوند. واریانت POST را بردارید.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... java -cp lib/gson.jar examples/v3/send-token-single-get.java

// docs:start
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SendTokenSingleGet {
    public static void main(String[] args) throws Exception {
        String query = "ApiKey=" + encode(System.getenv("PAYAM_RESAN_API_KEY"))
                + "&TemplateKey=" + encode("verifycode")
                + "&Destination=" + encode("9121112222")
                + "&p1=" + encode("123456");

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/SendTokenSingle?" + query))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        String raw = http.send(request, HttpResponse.BodyHandlers.ofString()).body();
        JsonObject response = JsonParser.parseString(raw).getAsJsonObject();

        if (!response.has("Success") || !response.get("Success").getAsBoolean()) {
            System.err.printf("ناموفق. کد %s: %s%n", response.get("ErrorCode"), response.get("Error"));
            System.exit(1);
        }

        response.getAsJsonArray("Result").forEach(item -> {
            JsonObject message = item.getAsJsonObject();
            System.out.printf("شناسه %s، متن نهایی: %s%n",
                    message.get("Id"), message.get("FinalText").getAsString());
        });
    }

    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
// docs:end
