# Agent guide

Runnable Java examples for the Payam Resan SMS web service. One file per API
method, and every file has to work on its own.

## Rule one: exactly one dependency, and only because Java forces it

Every other language in this organisation has examples with no dependencies at
all. Java is the exception, and it is worth knowing why so nobody "fixes" it:
**the JDK has no JSON parser**. HTTP is fine, `java.net.http.HttpClient` has
been in the platform since Java 11, but reading the response is not.

The alternative was extracting fields from the JSON with string handling, which
is the wrong thing to teach and worse to copy. So the examples take Gson, one
small jar, fetched by `lib/get-gson.sh` into a gitignored folder.

One dependency, and no more. No HTTP client, no logging framework, no build
tool. If something feels like it needs a second jar, it does not belong in an
example.

## Rule two: run from source, with the slug as the file name

Files are named after the documentation page they appear on: `send-bulk.java`,
`status-by-user-trace-id.java`. A hyphen is not legal in a Java class name, so
the class inside is `SendBulk` and the two deliberately differ.

That works because these run in single-file source mode, where the launcher
does not require the name to match:

```bash
java -cp lib/gson.jar examples/v3/send-bulk.java
```

Do not rename the files to match their classes. The name is a contract with the
documentation site.

## Rule three: the examples are the documentation

Each file carries `// docs:start` and `// docs:end`. The region between them is
lifted verbatim into the method's page on docs.payam-resan.com, so it is read by
people who have never seen this repository.

Two consequences:

- **Full-line comments are stripped** when the region is lifted. Anything the
  reader must see has to be code. The `Success` check is an `if`, not a note.
- A path with two variants gets two files, the plain name for `POST` and a
  `-get` suffix for `GET`.

The full contract lives in the `handbook` repository, section `docs-site`, file
`code-samples.md`.

## Rule four: check Success, and check it is there at all

The service answers `200` to everything, including a wrong key and an empty
account, so the HTTP status proves nothing.

The `has` call in front is not defensive clutter. A URL that does not exist
answers with a body carrying only `Message`, and without the check
`get("Success")` returns null and the example dies with a NullPointerException
instead of telling the reader what went wrong:

```java
if (!response.has("Success") || !response.get("Success").getAsBoolean()) {
    System.err.printf("ناموفق. کد %s: %s%n", response.get("ErrorCode"), response.get("Error"));
    System.exit(1);
}
```

## Rule five: a version is a folder

A new service version means a new `examples/v<n>/`. No file inside an existing
version folder is moved or renamed; older versions still have users.

## Secrets

The key comes from `PAYAM_RESAN_API_KEY` in the environment. No key, no real
phone number and no customer name goes into a file here, not even a dead one.
Example numbers are `9121112222` upward and the example key is
`123456-XXXXXXXXXXXXXXX`.

## Layout

| Path | What it holds |
|---|---|
| `examples/v3/` | one self-contained file per service operation |
| `lib/get-gson.sh` | fetches the one dependency; `lib/` is gitignored |
| `.env.example` | the environment variables the examples read |

## Before every commit

```bash
./lib/get-gson.sh
for f in examples/v3/*.java; do java -cp lib/gson.jar "$f" || echo "FAILED $f"; done
```

Point them at `api/V3SandBox/` first so no real message goes out.

## Git

Semantic messages, `type(scope): subject`, with no explanatory body and no
attribution trailer. Commits here are authored as Payam Resan.
