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
  };
}
