import packageJson from '../../package.json';

export const environment = {
  production: true,
  apiUrl: 'https://frc-efact-backend.onrender.com/api',
  enableHttps: true,
  secureOnly: true,
  apiTimeout: 30000, // 30 seconds
  enableLogging: false,
  version: packageJson.version,
  auth0: {
    domain: 'dev-gp1w0u2bgw35q6v5.us.auth0.com',
    clientId: 'ozA1x7MTVu8yVuOhYc3nUHWdLCPr6L6s',
    authorizationParams: {
      redirect_uri: 'https://frc-efact-frontend.onrender.com',
      audience: 'https://api.frcefact.com',
      scope: 'openid profile email offline_access'
    }
  }
};
