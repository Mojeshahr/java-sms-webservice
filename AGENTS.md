# Working with this repository

You are looking at runnable Java examples for the **Payam Resan** SMS web
service (`api.sms-webservice.com`, API V3), an Iranian SMS provider. Someone is
probably asking you to add SMS to their project.

Copy the example that matches the method, adapt it, and keep the rules below.
They are not style preferences — each one is a bug that this service produces if
you ignore it.

## Start here

```bash
./lib/get-gson.sh
export PAYAM_RESAN_API_KEY='123456-XXXXXXXXXXXXXXX'
java -cp lib/gson.jar examples/v3/account-info.java
```

`account-info.java` sends nothing, spends no credit, and answers even on a zero
balance, so run it first to prove the key and the connection work.

Java 11 or newer: that is where both `java.net.http.HttpClient` and single-file
source mode arrived.

## Rule 1: the file name and the class name differ on purpose

`send-bulk.java` contains `public class SendBulk`. A hyphen is not legal in a
Java class name, and the file name is a contract with the documentation site, so
the two deliberately disagree. This works because the examples run in
**single-file source mode**, where the launcher does not require the name to
match:

```bash
java -cp lib/gson.jar examples/v3/send-bulk.java
```

There is no `javac` step and no `.class` file. Do not "fix" this by renaming the
files. When you copy an example into a real project, rename it to `SendBulk.java`
there — the class name is already right.

## Rule 2: exactly one dependency, and only because Java forces it

Every other language in this organisation has dependency-free examples. Java is
the exception because **the JDK has no JSON parser**. HTTP is fine —
`java.net.http.HttpClient` has been in the platform since Java 11 — but reading
the response is not. The alternative was pulling fields out of the JSON with
string handling, which is the wrong thing to teach and worse to copy.

So: Gson, and nothing else. No HTTP client, no logging framework, no build tool.
If the user's project already has Jackson, keep the payload and the `Success`
check and swap only the parsing.

## Rule 3: check that `Success` is there, not just that it is true

The service answers `200` to everything, including a wrong key and an empty
account, so the status code proves nothing. But there is a second case Java
makes sharp: a URL that does not exist answers with a body carrying only
`Message`, and `get("Success")` then returns null.

```java
JsonObject response = JsonParser.parseString(raw).getAsJsonObject();

if (!response.has("Success") || !response.get("Success").getAsBoolean()) {
    System.err.printf("ناموفق. کد %s: %s%n", response.get("ErrorCode"), response.get("Error"));
    System.exit(1);
}
```

Without the `has` guard the example dies with a `NullPointerException` instead
of telling the reader what went wrong. `ErrorCode` is only meaningful when
`Success` is false.

Note the two separate failure channels the examples keep apart: transport and IO
problems escape as an exception from `main(String[] args) throws Exception`,
while a call the service rejected is a deliberate `System.exit(1)`.

## Rule 4: every id and phone number is a `long`

`9121112222` overflows `int`, and so does a message id. The `L` suffixes are
load-bearing:

```java
recipients.add(recipient(9121112222L, 1001L));
payload.addProperty("Sender", Long.parseLong(System.getenv("PAYAM_RESAN_SENDER")));
```

On the way back, print the raw `JsonElement` — `message.get("Id")` — rather than
calling `getAsInt()`, which truncates. `getAsInt()` is only safe on `StatusCode`.

## Rule 5: the key never leaves the server

`System.getenv("PAYAM_RESAN_API_KEY")`. Never compile it into a jar: a string in
a class file is trivially extracted. If the user's code is an Android app, this
repository is the wrong one — go to
[kotlin-sms-webservice](https://github.com/Mojeshahr/kotlin-sms-webservice),
and note that even there the call belongs on their backend, not in the app.

Avoid `send.java` and `send-token-single-get.java` in production. Those are the
`GET` methods, where the key sits in the URL and lands in the web server log and
the `Referer` header.

## Rule 6: pick the right method

| The user wants | Use | File |
|---|---|---|
| one text to many people | `SendBulk` | `send-bulk.java` |
| a different text per person | `SendMultiple` | `send-multiple.java` |
| a one-time password or code | `SendTokenSingle` | `send-token-single.java` |
| a template to many people | `SendTokenMulti` | `send-token-multi.java` |
| delivery status | `StatusByUserTraceId` | `status-by-user-trace-id.java` |
| balance and sender lines | `AccountInfo` | `account-info.java` |

**A one-time password goes through a template**, not free text — that is the
usual route for OTP, and the template fixes the sender line, which is why
`SendTokenSingle` takes no `Sender`. `token-list.java` lists the account's
templates; `Status` `2` means approved and sendable, `1` awaiting review, `3`
rejected.

## Rule 7: encode the query exactly once

The GET examples use `URLEncoder.encode(value, StandardCharsets.UTF_8)` in a
local helper. Encode the text yourself beforehand as well and the message
arrives full of `%D8` sequences. POST bodies carry
`Content-Type: application/json; charset=utf-8`.

## Rule 8: phone numbers have no leading zero

The service wants `9121112222` or `989121112222`. Users type `09121112222` or
`+989121112222`. Normalise before sending, or you get error `13`.

Ninety-nine recipients per request is the ceiling for `SendBulk`,
`SendMultiple` and `SendTokenMulti`.

## Rule 9: always send a `UserTraceId`

Use the user's own database id. After a timeout or error `100`, resending blind
may send twice — `StatusByUserTraceId` is the only safe way to learn whether the
message was registered. `StatusCode` of `8` there means the id is not in the
account, so it is safe to send again.

`SendTokenSingle` is the exception: it has no such input, so its `UserTraceId`
comes back null. If a trace id is needed for an OTP, use `SendTokenMulti` with a
single recipient.

## Rule 10: know which errors are worth retrying

These never succeed on retry — fix the cause; retrying only burns the rate limit
until the account hits error `20`:

`1`, `2`, `3`, `6`, `8`, `9`, `10`, `11`, `12`, `13`, `14`, `19`

`19` is an empty balance; `10` means the caller's IP is not on the account's
allowlist. Treat any unknown code the way you treat `100`: unclear outcome,
check with `StatusByUserTraceId` before resending.

## Rule 11: delivery status is a poll, not a callback

Status codes `0`, `1`, `2`, `3` and `10` mean still in flight — query again
later, and not more often than every few minutes or you will hit error `20`.
Everything else is final. Branch on `StatusCode`, never on the `Status` text,
which is Persian prose meant for humans and can change.

## Rule 12: `GetInbox` consumes what it returns

The service hands over each incoming message **once**. Never wire it to a
servlet or controller: every request consumes unread messages permanently. It
belongs in a scheduled job that writes straight to storage.

The sender field is called `Form`, not `From`. That is the service's spelling.

## Testing without spending credit

Replace `V3` with `V3SandBox` in the URL. No message is sent and no credit is
spent. `TokenList` is not implemented there.

The sandbox is a simulator, not a mirror of the account: credit is always
`1234567`, sender lines are invented, and **it accepts any key**. Success there
proves nothing about the user's real key.

## Where the authoritative answers are

- Method reference and error tables: <https://docs.payam-resan.com>
- Machine-readable OpenAPI: <https://github.com/Mojeshahr/sms-webservice-spec>

If the spec and these examples ever disagree, the spec wins — report it as a bug
rather than guessing.
