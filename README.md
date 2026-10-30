# Case: Secret Storage

## Background

You've just joined a small team that has built a simple internal service. The application is a Spring Boot web app that requires authentication to access. It's packaged as a Docker container and run locally.

Your tech lead has asked you to take a look at the repository and address any security concerns you find with how the application is configured — particularly around how secrets are handled.

## The problem

The original app stored the admin password in `application.properties` (`SuperSecretPassword123!`) and hardcoded the username in `HelloController`. That is not a local convenience; it is a secret in source control.

Anyone with the repo, a backup, a CI log, or a built JAR/image can recover it. Git keeps it after you delete the line, so the old value should be treated as compromised and rotated. Baking credentials into `Dockerfile` `ENV` or copying a `.env` into the image has the same effect: the secret is in a layer forever.

## The fix in this repo

Credentials are runtime configuration:

- `app.admin-username` / `app.admin-password` come from `ADMIN_USERNAME` and `ADMIN_PASSWORD`
- There are no default values in committed config
- The app refuses to start if either value is missing or blank
- Docker does not bake secrets into the image; pass them with `-e` at `docker run`
- Local examples live in `.env.example` and `config/application-local.properties.example`
- Real local files (`.env`, `config/application-local.properties`) are gitignored

This is enough for a small service. Spring's environment abstraction already maps env vars to properties, so we do not need a secrets library in this codebase.

## What production would look like

Do not put production passwords in git, image layers, or chat. Store them in the platform's secret store and inject them as env vars or files at deploy time:

- Kubernetes Secrets / CSI drivers
- AWS Secrets Manager, Azure Key Vault, GCP Secret Manager
- HashiCorp Vault
- CI/CD secret variables (GitHub Actions, GitLab, Jenkins credentials)

Rotate anything that ever lived in git. Prefer short-lived credentials and restrict who can read the secret store. Keep a distinct, non-production password on developer machines.

## Running the Application

### Docker

Build and run with credentials supplied at runtime:

```sh
docker build -t secret-case .
docker run -p 8080:8080 -e ADMIN_USERNAME=admin -e ADMIN_PASSWORD=change-me secret-case
```

Starting the container without those variables should fail fast instead of serving empty credentials.

Then visit [http://localhost:8080](http://localhost:8080). You will be prompted for credentials.

Or test with curl:

```sh
curl -u admin:change-me http://localhost:8080
```

### Local Maven

Copy the example local profile (gitignored after copy):

```sh
cp config/application-local.properties.example config/application-local.properties
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Or export `ADMIN_USERNAME` and `ADMIN_PASSWORD` in your shell and run without the local profile.

## Tests

```sh
mvn test
```
