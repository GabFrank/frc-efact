export interface AuthResponse {
  token: string;
  refreshToken: string;
  type?: string;
  usuario: {
    id: number;
    username: string;
    email: string;
    roles: string[];
    createdAt: string;
    lastLogin?: string;
    isActive: boolean;
    updatedAt: string;
    failedLoginAttempts: number;
    lockedUntil?: string;
    empresas?: any[];
    createdBy?: number;
    updatedBy?: number;
  };
}
