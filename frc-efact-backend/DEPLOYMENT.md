# FRC eFact Backend - Deployment Guide

## Deployment to Render

### Prerequisites
- GitHub account with repository access
- Render account (free tier available)

### Option 1: Deploy using render.yaml (Recommended)

1. **Push code to GitHub**
   ```bash
   git add .
   git commit -m "Configure for Render deployment"
   git push origin main
   ```

2. **Create New Web Service on Render**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click "New +" → "Blueprint"
   - Connect your GitHub repository
   - Render will automatically detect `render.yaml` and configure services

3. **Environment Variables** (Auto-configured via render.yaml)
   - `DATABASE_URL` - Auto-populated from managed PostgreSQL
   - `JWT_SECRET` - Auto-generated secure value
   - `SPRING_PROFILES_ACTIVE` - Set to `prod`

### Option 2: Manual Web Service Setup

1. **Create PostgreSQL Database**
   - Dashboard → "New +" → "PostgreSQL"
   - Name: `frc-efact-db`
   - Plan: Free
   - Copy the Internal Database URL

2. **Create Web Service**
   - Dashboard → "New +" → "Web Service"
   - Connect repository: `frc-efact-backend`
   - Name: `frc-efact-backend`
   - Environment: `Java`
   - Region: `Oregon` (or closest to your users)
   - Branch: `main`
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/frc-efact-backend-*.jar`

3. **Configure Environment Variables**
   ```
   DATABASE_URL=<paste-internal-database-url>
   JWT_SECRET=<generate-secure-random-string-min-64-chars>
   SPRING_PROFILES_ACTIVE=prod
   JAVA_OPTS=-Xmx512m -Xms256m
   ```

4. **Configure Health Check**
   - Health Check Path: `/api/actuator/health`

### Option 3: Deploy using Docker

1. **Build Docker Image**
   ```bash
   docker build -t frc-efact-backend .
   ```

2. **Test Locally**
   ```bash
   docker run -p 8080:8080 \
     -e DATABASE_URL=<your-db-url> \
     -e JWT_SECRET=<your-secret> \
     -e SPRING_PROFILES_ACTIVE=prod \
     frc-efact-backend
   ```

3. **Deploy to Render**
   - Create Web Service
   - Select "Docker" as environment
   - Render will use the Dockerfile automatically

## Environment Variables Reference

| Variable | Description | Required | Example |
|----------|-------------|----------|---------|
| `DATABASE_URL` | PostgreSQL connection string | Yes | `postgresql://user:pass@host:5432/db` |
| `JWT_SECRET` | Secret key for JWT signing (min 64 chars) | Yes | `your-super-secret-key-min-64-characters-long` |
| `SPRING_PROFILES_ACTIVE` | Spring profile to activate | Yes | `prod` |
| `PORT` | Server port (auto-set by Render) | No | `8080` |
| `JAVA_OPTS` | JVM options | No | `-Xmx512m -Xms256m` |

## Generating JWT Secret

Generate a secure JWT secret using one of these methods:

```bash
# Using OpenSSL
openssl rand -base64 64

# Using Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"

# Using Python
python3 -c "import secrets; print(secrets.token_urlsafe(64))"
```

## Health Check Endpoints

- **Liveness**: `GET /api/actuator/health/liveness`
- **Readiness**: `GET /api/actuator/health/readiness`
- **General Health**: `GET /api/actuator/health`

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

## Database Migrations

Flyway migrations run automatically on application startup. Ensure:
- `spring.flyway.enabled=true` in production config
- Migration files are in `src/main/resources/db/migration/`
- Migrations follow naming convention: `V{version}__{description}.sql`

## Monitoring and Logs

### View Logs on Render
- Dashboard → Your Service → "Logs" tab
- Real-time log streaming available

### Available Metrics
- `/api/actuator/metrics` - Application metrics
- `/api/actuator/info` - Application info

## Troubleshooting

### Application Won't Start
1. Check logs for errors: `Dashboard → Logs`
2. Verify environment variables are set correctly
3. Ensure DATABASE_URL is accessible
4. Check Flyway migrations completed successfully

### Database Connection Issues
1. Verify DATABASE_URL format: `postgresql://user:password@host:port/database`
2. Check database is in same region as web service
3. Use Internal Database URL (not External) for better performance

### Health Check Failing
1. Verify path is `/api/actuator/health`
2. Check application started successfully in logs
3. Ensure database connection is working

### JWT Token Issues
1. Verify JWT_SECRET is set and at least 64 characters
2. Check JWT_SECRET is consistent across deployments
3. Ensure expiration times are configured correctly

## Performance Optimization

### JVM Tuning for Render Free Tier (512MB RAM)
```bash
JAVA_OPTS=-Xmx400m -Xms200m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC
```

### Database Connection Pool
- Max connections: 20 (configured in application-prod.yml)
- Min idle: 5
- Connection timeout: 30s

## Security Checklist

- ✅ JWT_SECRET is strong and unique
- ✅ DATABASE_URL uses internal connection (not external)
- ✅ HTTPS enforced (automatic on Render)
- ✅ CORS configured for frontend domain only
- ✅ Security headers enabled (HSTS, X-Frame-Options, etc.)
- ✅ Actuator endpoints secured
- ✅ Rate limiting enabled for auth endpoints

## Rollback Strategy

If deployment fails:
1. Render automatically keeps previous version running
2. Manual rollback: Dashboard → Deploys → Select previous deploy → "Rollback"
3. Database rollback: Restore from automatic daily backup

## Continuous Deployment

Enable auto-deploy:
1. Dashboard → Your Service → Settings
2. Enable "Auto-Deploy" for main branch
3. Every push to main triggers automatic deployment

## Cost Optimization

### Free Tier Limits
- 750 hours/month (enough for 1 service running 24/7)
- Services spin down after 15 minutes of inactivity
- Cold start time: ~30-60 seconds

### Upgrade Considerations
- Upgrade to Starter ($7/month) for:
  - No spin down
  - Faster cold starts
  - More resources (1GB RAM)
