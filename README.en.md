<div align="center">

<a href="https://payam-resan.com">
  <img src=".github/assets/logo.svg" width="64" height="64" alt="Payam Resan">
</a>

<h1>Java examples for the Payam Resan SMS web service</h1>

Talk to the <a href="https://payam-resan.com"><b>Payam Resan SMS panel</b></a> from Java<br>
One runnable file per API method

[![API](https://img.shields.io/badge/API-V3-0a7cbd)](https://payam-resan.com)
[![Java](https://img.shields.io/badge/Java-11%2B-f89820)](https://adoptium.net)
[![Dependency](https://img.shields.io/badge/dependency-gson-2ea44f)](https://github.com/google/gson)
[![License](https://img.shields.io/badge/license-MIT-6e7781)](LICENSE)

<a href="README.md">فارسی</a> · <b>English</b>

</div>

<sub>Looking for another language? The same examples exist for the others at
[github.com/Mojeshahr](https://github.com/Mojeshahr).</sub>

---

## Quick start

```bash
git clone https://github.com/Mojeshahr/java-sms-webservice.git
cd java-sms-webservice

./lib/get-gson.sh

export PAYAM_RESAN_API_KEY='123456-XXXXXXXXXXXXXXX'
export PAYAM_RESAN_SENDER='30004040'

java -cp lib/gson.jar examples/v3/account-info.java
```

Start with `account-info.java`. It sends nothing, spends no credit, and if it
answers then both the key and the connection are fine.

## Why one dependency

The examples for every other language in this organisation have none. Java is
the exception for one reason: **the JDK ships no JSON parser**. HTTP is fine,
`java.net.http` has been in the platform since Java 11, but reading the response
without a library means picking JSON apart with string handling, which is the
wrong thing to teach and worse to copy.

So one small jar, Gson, fetched by `lib/get-gson.sh`. In a real project you
would declare it in Maven or Gradle instead of running that script.

## Before sending anything real

There is a sandbox server that answers exactly like production but sends no
message and spends no credit. Swap `V3` for `V3SandBox` in the URL. The one
exception is `TokenList`, which the sandbox does not implement.

## The methods

| Example | Method | What it does |
|---|---|---|
| [account-info.java](examples/v3/account-info.java) | `AccountInfo` | Credit and active lines |
| [send.java](examples/v3/send.java) | `Send` | Simple send over `GET` |
| [send-bulk.java](examples/v3/send-bulk.java) | `SendBulk` | One text to many recipients, with tracking ids |
| [send-multiple.java](examples/v3/send-multiple.java) | `SendMultiple` | A separate text per recipient |
| [token-list.java](examples/v3/token-list.java) | `TokenList` | The account's templates |
| [send-token-single.java](examples/v3/send-token-single.java) | `SendTokenSingle` | Send a template to one number |
| [send-token-single-get.java](examples/v3/send-token-single-get.java) | `SendTokenSingle` | The same, over `GET` |
| [send-token-multi.java](examples/v3/send-token-multi.java) | `SendTokenMulti` | One template, many recipients |
| [status-by-id.java](examples/v3/status-by-id.java) | `StatusById` | Status by the service's id |
| [status-by-user-trace-id.java](examples/v3/status-by-user-trace-id.java) | `StatusByUserTraceId` | Status by your own id |
| [get-inbox.java](examples/v3/get-inbox.java) | `GetInbox` | Messages people sent to your lines |

## The file name and the class name differ

The file is `send-bulk.java` and the class inside is `SendBulk`. A hyphen is not
legal in a Java class name, and in single-file source mode the launcher does not
require the two to match.

The file name is deliberately the documentation page's slug, so leave it alone.

## Using this in your own project

The examples depend on nothing in this repository, so copying the body of a file
into your own service is enough. Pull Gson in with Maven or Gradle:

```xml
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.11.0</version>
</dependency>
```

If your project already uses Jackson, change only the response-reading part; the
request shape is the same.

## Things that will save you time

**Do not read the HTTP status code.** The service answers `200` to everything,
including a wrong key. Decide on the `Success` field.

**Check that `Success` is there at all.** A wrong URL returns a body carrying
only `Message`, and without that check the example dies with a
NullPointerException instead of saying what went wrong. Every example here
checks.

**Recipient numbers carry no leading zero.** Use `9121112222`, or
`989121112222` with the country code. A number that does not start with `9` or
`989` returns error code `13`.

**Do not encode the text twice.** In `send.java`, `URLEncoder.encode` already
does it once. Encode it yourself beforehand and the message arrives full of
`%D8`.

**Send a unique `UserTraceId` per recipient.** After a timeout it is the only
way to learn whether the message was registered.

## Key safety

The key is a secret. It does not belong in a code repository, in browser
JavaScript, or in a mobile app bundle. It belongs in an environment variable,
which is where every example here reads it from.

If a key leaks, issue a new one from the panel. A deleted key never comes back.

## Layout

| Path | What it holds |
|---|---|
| `examples/v3/` | One self-contained example per service operation |
| `lib/get-gson.sh` | Fetches the one dependency; `lib/` itself is gitignored |
| `.env.example` | The environment variables the examples read |

The `v3` in the path is deliberate. A new service version means a new
`examples/v<n>/`, with the existing folder left alone.

## Documentation and support

The full guide is at [docs.payam-resan.com](https://docs.payam-resan.com). The
machine-readable OpenAPI description is in
[sms-webservice-spec](https://github.com/Mojeshahr/sms-webservice-spec).

## License

MIT. Full text in [`LICENSE`](LICENSE).
