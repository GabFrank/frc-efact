export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  enableHttps: false,
  secureOnly: false,
  apiTimeout: 30000, // 30 seconds
  enableLogging: true,
  version: '1.0.0-dev',
  auth0: {
    domain: 'dev-gp1w0u2bgw35q6v5.us.auth0.com',
    clientId: 'ozA1x7MTVu8yVuOhYc3nUHWdLCPr6L6s',
    authorizationParams: {
      redirect_uri: window.location.origin,
      audience: 'https://api.frcefact.com',
      scope: 'openid profile email offline_access'
    }
  }
};
