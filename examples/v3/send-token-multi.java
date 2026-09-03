// SendTokenMulti - یک قالب، چند گیرنده، مقادیر متفاوت.
//
// پارامترها اینجا آرایه‌اند، نه p1 تا p10. درایه اول به {1} می‌نشیند، دومی به
// {2} و همین‌طور تا آخر: ترتیب از شماره جای‌گاه می‌آید، نه از جایی که در متن
// قالب دیده می‌شود.
//
// جز Gson به چیزی وابسته نیست. جاوا در کتابخانه استاندارد پارسر JSON ندارد و
// این تنها زبانی است که نمونه‌هایش یک وابستگی می‌خواهند.
//
//   ./lib/get-gson.sh
//   PAYAM_RESAN_API_KEY=... java -cp lib/gson.jar examples/v3/send-token-multi.java

// docs:start
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SendTokenMulti {
    public static void main(String[] args) throws Exception {
        // قالب نمونه: «مرسوله شما از {2} تحویل پست شد. بارکد مرسوله پستی: {1}»
        JsonArray recipients = new JsonArray();
        recipients.add(recipient(9121112222L, 1001L, "BARCODE-AAA", "شیراز"));
        recipients.add(recipient(9121113333L, 1002L, "BARCODE-BBB", "تبریز"));

        JsonObject payload = new JsonObject();
        payload.addProperty("ApiKey", System.getenv("PAYAM_RESAN_API_KEY"));
        payload.addProperty("TemplateKey", "postcode");
        payload.add("Recipients", recipients);

        HttpClient http = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.sms-webservice.com/api/V3/SendTokenMulti"))
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
            System.out.printf("%s => %s%n", message.get("UserTraceId"), message.get("FinalText").getAsString());
        });
    }

    static JsonObject recipient(long destination, long userTraceId, String... parameters) {
        JsonArray values = new JsonArray();
        for (String value : parameters) {
            values.add(value);
        }

        JsonObject one = new JsonObject();
        one.addProperty("Destination", destination);
        one.addProperty("UserTraceId", userTraceId);
        one.add("Parameters", values);
        return one;
    }
}
// docs:end
