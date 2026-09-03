// Send - ساده‌ترین ارسال، یک متن به چند شماره با یک درخواست GET.
//
// برای آزمایش سریع خوب است. در محیط عملیاتی SendBulk را بردارید: کلید را از
// نشانی بیرون می‌برد و برای هر گیرنده شناسه پی‌گیری می‌پذیرد.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... PAYAM_RESAN_SENDER=... java -cp lib/gson.jar examples/v3/send.java

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

public class Send {
    public static void main(String[] args) throws Exception {
        // URLEncoder دقیقاً یک بار encode می‌کند. اگر متن را خودتان هم پیش از
        // این encode کنید، پیامک با نویسه‌های %D8 به گوشی می‌رسد.
        String query = "ApiKey=" + encode(System.getenv("PAYAM_RESAN_API_KEY"))
                + "&Sender=" + encode(System.getenv("PAYAM_RESAN_SENDER"))
                + "&Text=" + encode("کد تأیید شما ۱۲۳۴۵۶ است")
                + "&Recipients=" + encode("9121112222,9121113333");

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/Send?" + query))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        String raw = http.send(request, HttpResponse.BodyHandlers.ofString()).body();
        JsonObject response = JsonParser.parseString(raw).getAsJsonObject();

        if (!response.has("Success") || !response.get("Success").getAsBoolean()) {
            System.err.printf("ناموفق. کد %s: %s%n", response.get("ErrorCode"), response.get("Error"));
            System.exit(1);
        }

        response.getAsJsonArray("Result").forEach(item ->
                System.out.println("شناسه " + item.getAsJsonObject().get("Id")));
    }

    static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
// docs:end
