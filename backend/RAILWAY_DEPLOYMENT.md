# Railway Deployment

## Deploy

1. Push this repository to GitHub.
2. In Railway, create a new project and select **Deploy from GitHub repo**.
3. Add a Railway MySQL service to the same project.
4. In the backend service, set the variables below.
5. Open the backend service **Settings > Networking > Public Networking** and generate a domain.

Railway detects the root `Dockerfile` and uses `railway.toml` for the health check.

## Backend Variables

Set these in the backend service:

```text
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JPA_DDL_AUTO=update
TZ=Asia/Seoul
```

Do not set `SERVER_PORT` on Railway unless you need to override the default behavior. The app reads Railway's `PORT` variable first and falls back to `8080` locally.

## Verify

After deployment, check:

```text
https://<your-railway-domain>/health
https://<your-railway-domain>/swagger-ui/index.html
```
