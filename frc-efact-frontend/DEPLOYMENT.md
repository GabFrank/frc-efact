# FRC eFact Frontend - Deployment Guide

## Deployment to Render

### Prerequisites
- GitHub account with repository access
- Render account (free tier available)
- Backend API deployed and accessible

### Option 1: Deploy using render.yaml (Recommended)

1. **Update Backend API URL**
   
   Edit `src/environments/environment.prod.ts`:
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://your-backend-url.onrender.com/api'
   };
   ```

2. **Push code to GitHub**
   ```bash
   git add .
   git commit -m "Configure for Render deployment"
   git push origin main
   ```

3. **Create New Static Site on Render**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click "New +" → "Blueprint"
   - Connect your GitHub repository
   - Render will automatically detect `render.yaml` and configure the service

### Option 2: Manual Static Site Setup

1. **Create Static Site**
   - Dashboard → "New +" → "Static Site"
   - Connect repository: `frc-efact-frontend`
   - Name: `frc-efact-frontend`
   - Branch: `main`
   - Build Command: `npm ci && npm run build:prod`
   - Publish Directory: `dist/frc-efact-frontend/browser`

2. **Configure Redirects for SPA**
   
   The `_redirects` file is automatically included in the build and handles SPA routing.

3. **Configure Environment Variables** (Optional)
   ```
   NODE_ENV=production
   ```

## Build Optimization

### Production Build Features

The production build includes:
- ✅ **AOT (Ahead-of-Time) Compilation** - Faster rendering
- ✅ **Tree Shaking** - Removes unused code
- ✅ **Minification** - Reduces bundle size
- ✅ **Code Splitting** - Lazy loading for better performance
- ✅ **Source Map Removal** - Smaller bundle size
- ✅ **CSS Optimization** - Minified and inlined critical CSS

### Build Commands

```bash
# Development build
npm run build

# Production build (optimized)
npm run build:prod

# Analyze bundle size
npm run analyze
```

### Bundle Size Targets

| Bundle Type | Warning | Error |
|-------------|---------|-------|
| Initial | 500 KB | 1 MB |
| Component Styles | 2 KB | 4 KB |

## SPA Routing Configuration

### How it Works

El routing SPA en Render se resuelve **principalmente vía `render.yaml`**, con una regla
`routes` de tipo `rewrite` que reescribe todas las rutas a `index.html`:

```yaml
# render.yaml → servicio frc-efact-frontend
routes:
  - type: rewrite
    source: /*
    destination: /index.html
```

Adicionalmente, `src/_redirects` existe como respaldo (útil en otros hosts estáticos):

```
/*    /index.html   200
```

Este esquema:
- Reescribe todas las rutas a `index.html` (preserva el path)
- Deja que Angular Router maneje la navegación del lado del cliente

### Testing Routing Locally

```bash
# Install http-server
npm install -g http-server

# Build production
npm run build:prod

# Serve with SPA fallback
cd dist/frc-efact-frontend/browser
http-server -p 8080 --proxy http://localhost:8080?
```

## Security Headers

> ⚠️ **NO configurados (verificado 2026).** `render.yaml` **no tiene sección `headers`**
> para el frontend — solo `buildCommand`, `staticPublishPath` y `routes` (rewrite).
> No se envía ningún header de seguridad. Es deuda de seguridad pendiente; ver
> [SECURITY.md](./SECURITY.md) para el estado real y cómo agregarlos.

## Environment Configuration

> **`apiUrl` es compile-time, no runtime.** Se resuelve al construir el bundle según la
> configuración de build (`development` usa `environment.ts`, `production` usa
> `environment.prod.ts` via `fileReplacements` en `angular.json`). **No** se lee de una
> variable de entorno en Render en tiempo de ejecución: para cambiar la URL del backend
> hay que editar `environment.prod.ts` y volver a buildear/deployar. La variable
> `NODE_ENV` en Render no afecta `apiUrl`.
>
> Ambos `environment.*.ts` también incluyen la config de Auth0 (`domain`, `clientId`,
> `audience`) y `version` (leída de `package.json`).

### Development Environment
```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  enableHttps: false,
  secureOnly: false,
  apiTimeout: 30000,
  enableLogging: true,
  version: '1.0.0-dev'
};
```

### Production Environment
```typescript
// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://frc-efact-backend.onrender.com/api',
  enableHttps: true,
  secureOnly: true,
  apiTimeout: 30000,
  enableLogging: false,
  version: '1.0.0'
};
```

## Performance Optimization

### Lazy Loading

Routes are configured for lazy loading to reduce initial bundle size:
```typescript
const routes: Routes = [
  {
    path: 'feature',
    loadComponent: () => import('./feature/feature.component')
  }
];
```

### Image Optimization

Use Angular's built-in image optimization:
```html
<img ngSrc="assets/logo.png" width="200" height="100" priority>
```

### Caching Strategy

Static assets are cached with the following strategy:
- HTML files: No cache (always fresh)
- JS/CSS files: Immutable cache (1 year) with content hashing
- Images: Long-term cache with versioning

## Monitoring and Analytics

### Performance Monitoring

Monitor Core Web Vitals:
- **LCP (Largest Contentful Paint)**: < 2.5s
- **FID (First Input Delay)**: < 100ms
- **CLS (Cumulative Layout Shift)**: < 0.1

### Error Tracking

Implement error tracking in production:
```typescript
// app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    {
      provide: ErrorHandler,
      useClass: GlobalErrorHandler
    }
  ]
};
```

## Troubleshooting

### Build Fails

**Issue**: Build command fails
```bash
npm ci && npm run build:prod
```

**Solutions**:
1. Check Node.js version (requires 18.x or higher)
2. Clear cache: `rm -rf node_modules package-lock.json && npm install`
3. Check for TypeScript errors: `npm run build:prod`

### Routing Not Working

**Issue**: Direct URL access returns 404

**Solutions**:
1. Verify `_redirects` file is in `src/` directory
2. Check `angular.json` includes `_redirects` in assets
3. Verify Render publish path: `dist/frc-efact-frontend/browser`

### API Connection Issues

**Issue**: Cannot connect to backend API

**Solutions**:
1. Verify backend URL in `environment.prod.ts`
2. Check CORS configuration on backend
3. Ensure backend is deployed and healthy
4. Check browser console for CORS errors

### Blank Page After Deployment

**Issue**: Application shows blank page

**Solutions**:
1. Check browser console for errors
2. Verify base href in `index.html`: `<base href="/">`
3. Check build output path matches publish directory
4. Verify all assets are included in build

## Continuous Deployment

### Auto-Deploy Setup

1. **Enable Auto-Deploy**
   - Dashboard → Your Service → Settings
   - Enable "Auto-Deploy" for main branch
   - Every push to main triggers automatic deployment

2. **Disparar deploys — SOLO vía `git push`**

   > ⚠️ **Regla del proyecto:** los deploys se disparan **únicamente** con `git push` a
   > `main` (auto-deploy). **No** usar la API de Render, deploy hooks, ni el botón
   > "Manual Deploy" del dashboard — rompe la trazabilidad commit ↔ deploy.
   >
   > Para forzar un redeploy del mismo commit:
   > ```bash
   > git commit --allow-empty -m "chore: trigger redeploy"
   > git push origin main
   > ```
   >
   > Las tools de Render MCP (`list_deploys`, `get_deploy`, `list_logs`, etc.) se pueden
   > usar para **inspeccionar/diagnosticar**, nunca para mutar estado de deploys.

### Preview Deployments

Pull request previews are enabled by default:
- Each PR gets a unique preview URL
- Automatically deployed on PR creation
- Deleted when PR is closed

## Cost Optimization

### Free Tier Limits
- **Bandwidth**: 100 GB/month
- **Build Minutes**: 500 minutes/month
- **Storage**: Unlimited for static sites

### Optimization Tips
1. Enable compression (automatic on Render)
2. Optimize images before deployment
3. Use lazy loading for routes
4. Minimize bundle size

## Rollback Strategy

### Manual Rollback
1. Dashboard → Your Service → Deploys
2. Find previous successful deploy
3. Click "Rollback to this version"

### Git-Based Rollback
```bash
# Revert to previous commit
git revert HEAD
git push origin main

# Or reset to specific commit
git reset --hard <commit-hash>
git push --force origin main
```

## Custom Domain Setup

1. **Add Custom Domain**
   - Dashboard → Your Service → Settings → Custom Domains
   - Add your domain (e.g., `app.example.com`)

2. **Configure DNS**
   - Add CNAME record pointing to Render URL
   - Wait for DNS propagation (up to 48 hours)

3. **SSL Certificate**
   - Automatic SSL certificate via Let's Encrypt
   - Auto-renewal every 90 days

## Health Check

Verify deployment:
```bash
# Check if site is accessible
curl -I https://frc-efact-frontend.onrender.com

# Expected response
HTTP/2 200
content-type: text/html
```

## Performance Benchmarks

Target metrics for production:
- **First Contentful Paint**: < 1.5s
- **Time to Interactive**: < 3.5s
- **Total Bundle Size**: < 500 KB (initial)
- **Lighthouse Score**: > 90

## Security Checklist

- ✅ HTTPS enabled (automatic on Render)
- ❌ Security headers configured — **PENDIENTE** (no están en `render.yaml`; ver [SECURITY.md](./SECURITY.md))
- ✅ No sensitive data in environment files
- ✅ API keys not exposed in frontend code
- ❌ Content Security Policy configured — **PENDIENTE** (nunca se configuró)
- ✅ XSS protection enabled (sanitización automática de Angular)
- ✅ CORS properly configured on backend

## Support and Resources

- [Render Documentation](https://render.com/docs/static-sites)
- [Angular Deployment Guide](https://angular.io/guide/deployment)
- [Angular Performance Guide](https://angular.io/guide/performance-best-practices)
