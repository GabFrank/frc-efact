// Detectar si estamos accediendo desde una IP local (no localhost)
function getApiUrl(): string {
  const hostname = window.location.hostname;
  
  // Si es localhost, usar localhost para el backend
  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return 'http://localhost:8080/api';
  }
  
  // Si es una IP local (192.168.x.x, 10.x.x.x, 172.x.x.x), usar HTTP para el backend
  // Nota: En desarrollo local usamos HTTP para evitar problemas de contenido mixto
  if (hostname.match(/^(192\.168\.|10\.|172\.(1[6-9]|2[0-9]|3[01])\.)/)) {
    return `http://${hostname}:8080/api`;
  }
  
  // Por defecto, usar localhost
  return 'http://localhost:8080/api';
}

export const environment = {
  production: false,
  apiUrl: getApiUrl(),
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
