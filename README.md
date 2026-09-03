<div align="center">

<a href="https://payam-resan.com">
  <img src=".github/assets/logo.svg" width="64" height="64" alt="پیام رسان">
</a>

<h1>نمونه‌کدهای Java وب‌سرویس پیام رسان</h1>

اتصال به وب‌سرویس <a href="https://payam-resan.com"><b>پنل پیامکی پیام رسان</b></a> با Java<br>
یک فایل قابل اجرا به‌ازای هر متد سرویس

[![API](https://img.shields.io/badge/API-V3-0a7cbd)](https://payam-resan.com)
[![Java](https://img.shields.io/badge/Java-11%2B-f89820)](https://adoptium.net)
[![Dependency](https://img.shields.io/badge/dependency-gson-2ea44f)](https://github.com/google/gson)
[![License](https://img.shields.io/badge/license-MIT-6e7781)](LICENSE)

<b>فارسی</b> · <a href="README.en.md">English</a>

</div>

<sub>دنبال زبان دیگری هستید؟ همین نمونه‌ها برای زبان‌های دیگر هم در
[github.com/Mojeshahr](https://github.com/Mojeshahr) هست.</sub>

---

## شروع سریع

```bash
git clone https://github.com/Mojeshahr/java-sms-webservice.git
cd java-sms-webservice

./lib/get-gson.sh

export PAYAM_RESAN_API_KEY='123456-XXXXXXXXXXXXXXX'
export PAYAM_RESAN_SENDER='30004040'

java -cp lib/gson.jar examples/v3/account-info.java
```

با `account-info.java` شروع کنید: چیزی ارسال نمی‌کند، اعتباری مصرف نمی‌کند، و
اگر جواب داد یعنی کلید و اتصال هر دو سالم‌اند.

## چرا یک وابستگی

نمونه‌های بقیه زبان‌های این سازمان هیچ وابستگی ندارند. جاوا استثناست و دلیلش
ساده است: **JDK پارسر JSON ندارد.** بخش HTTP مشکلی نیست و `java.net.http` از
جاوا ۱۱ در خود پلتفرم است، ولی خواندن پاسخ بدون کتابخانه یعنی دست‌کاری رشته‌ای
JSON، که روش غلطی است و کپی‌شدنش بدتر.

پس یک jar کوچک، Gson، که `lib/get-gson.sh` می‌آوردش. در پروژه واقعی به‌جای آن
اسکریپت، وابستگی را در Maven یا Gradle تعریف می‌کنید.

## پیش از ارسال واقعی

یک سرور آزمایشی هست که مثل سرور عملیاتی جواب می‌دهد ولی پیامکی نمی‌فرستد و
اعتباری مصرف نمی‌کند. کافی است `V3` در نشانی را با `V3SandBox` عوض کنید. تنها
استثنا `TokenList` است که روی آن سرور پیاده نشده.

## متدها

<div dir="rtl">

| نمونه | متد | کار |
|---|---|---|
| [account-info.java](examples/v3/account-info.java) | `AccountInfo` | اعتبار و خطوط فعال |
| [send.java](examples/v3/send.java) | `Send` | ارسال ساده با `GET` |
| [send-bulk.java](examples/v3/send-bulk.java) | `SendBulk` | یک متن به چند گیرنده، با شناسه پی‌گیری |
| [send-multiple.java](examples/v3/send-multiple.java) | `SendMultiple` | متن جدا برای هر گیرنده |
| [token-list.java](examples/v3/token-list.java) | `TokenList` | فهرست قالب‌ها |
| [send-token-single.java](examples/v3/send-token-single.java) | `SendTokenSingle` | ارسال قالب به یک شماره |
| [send-token-single-get.java](examples/v3/send-token-single-get.java) | `SendTokenSingle` | همان، با `GET` |
| [send-token-multi.java](examples/v3/send-token-multi.java) | `SendTokenMulti` | یک قالب، چند گیرنده |
| [status-by-id.java](examples/v3/status-by-id.java) | `StatusById` | وضعیت با شناسه سامانه |
| [status-by-user-trace-id.java](examples/v3/status-by-user-trace-id.java) | `StatusByUserTraceId` | وضعیت با شناسه خودتان |
| [get-inbox.java](examples/v3/get-inbox.java) | `GetInbox` | پیامک‌های رسیده |

</div>

## نام فایل و نام کلاس فرق دارند

فایل `send-bulk.java` است و کلاس داخلش `SendBulk`. خط تیره در نام کلاس جاوا
مجاز نیست، ولی در حالت اجرا از سورس، لازم نیست نام فایل و کلاس یکی باشند.

نام فایل عمداً همان اسلاگ صفحه مستندات است، پس تغییرش ندهید.

## استفاده در پروژه خودتان

نمونه‌ها به هیچ چیز این مخزن وابسته نیستند، پس کپی‌کردن بدنه فایل داخل سرویس
خودتان کافی است. Gson را با Maven یا Gradle بیاورید:

```xml
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.11.0</version>
</dependency>
```

اگر در پروژه‌تان Jackson دارید، فقط بخش خواندن پاسخ را عوض کنید؛ شکل درخواست
همان است.

## چند نکته که وقت‌تان را می‌خرد

**کد وضعیت HTTP را نخوانید.** سرویس همیشه `200` برمی‌گرداند، حتی وقتی کلید
اشتباه است. تصمیم را از فیلد `Success` بگیرید.

**اول ببینید `Success` اصلاً هست.** نشانی اشتباه بدنه‌ای برمی‌گرداند که فقط
`Message` دارد، و بدون این بررسی برنامه با NullPointerException می‌افتد به‌جای
اینکه بگوید چه شده. هر نمونه اینجا همین کار را می‌کند.

**شماره گیرنده صفر ابتدایی ندارد.** یعنی `9121112222` یا با کد کشور
`989121112222`. شماره‌ای که با `9` یا `989` شروع نشود کد خطای `13` می‌گیرد.

**متن را دوباره encode نکنید.** در `send.java` تابع `URLEncoder.encode` خودش یک
بار این کار را می‌کند. اگر پیش از آن هم encode کنید، پیامک با نویسه‌های `%D8` به
گوشی می‌رسد.

**برای هر گیرنده یک `UserTraceId` یکتا بفرستید.** بعد از یک timeout، این تنها
راه فهمیدن این است که پیامک ثبت شده یا نه.

## امنیت کلید

کلید یک راز است. در مخزن کد، در جاوااسکریپت مرورگر و در بسته اپلیکیشن موبایل
نباید قرار بگیرد. جای آن متغیر محیطی است، همان‌طور که همه نمونه‌ها می‌خوانندش.

اگر کلیدی لو رفت، از پنل یکی تازه بسازید. کلید حذف‌شده برنمی‌گردد.

## ساختار

<div dir="rtl">

| مسیر | چه چیزی دارد |
|---|---|
| `examples/v3/` | یک نمونه مستقل به‌ازای هر عملیات سرویس |
| `lib/get-gson.sh` | آوردن تنها وابستگی. خود `lib/` کامیت نمی‌شود |
| `.env.example` | نمونه متغیرهای محیطی |

</div>

عدد `v3` در مسیر عمدی است. نسخه تازه سرویس یعنی پوشه `examples/v<n>/` تازه، و
پوشه موجود دست‌نخورده می‌ماند.

## مستندات و پشتیبانی

راهنمای کامل وب‌سرویس در [docs.payam-resan.com](https://docs.payam-resan.com)
است. توصیف ماشین‌خوان OpenAPI هم در
[sms-webservice-spec](https://github.com/Mojeshahr/sms-webservice-spec).

## مجوز

MIT. متن کامل در [`LICENSE`](LICENSE).
