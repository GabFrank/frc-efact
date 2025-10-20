import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DepartamentoDto {
  id: number;
  codigo: string;
  nombre: string;
  paisCodigo: string;
  paisNombre: string;
  activo: boolean;
}

export interface DistritoDto {
  id: number;
  codigo: string;
  nombre: string;
  departamentoCodigo: string;
  departamentoNombre: string;
  activo: boolean;
}

export interface CiudadDto {
  id: number;
  codigo: string;
  nombre: string;
  distritoCodigo: string;
  distritoNombre: string;
  departamentoCodigo: string;
  departamentoNombre: string;
  activo: boolean;
}

export interface BarrioDto {
  id: number;
  codigo: string;
  nombre: string;
  ciudadCodigo: string;
  ciudadNombre: string;
  activo: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SifenService {
  private apiUrl = `${environment.apiUrl}/api/geografia`;

  constructor(private http: HttpClient) {}

  // ========== DEPARTAMENTOS ==========

  getDepartamentos(): Observable<DepartamentoDto[]> {
    return this.http.get<DepartamentoDto[]>(`${this.apiUrl}/departamentos`);
  }

  searchDepartamentos(query: string): Observable<DepartamentoDto[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<DepartamentoDto[]>(`${this.apiUrl}/departamentos/buscar`, { params });
  }

  getDepartamentoByCodigo(codigo: string): Observable<DepartamentoDto> {
    return this.http.get<DepartamentoDto>(`${this.apiUrl}/departamentos/${codigo}`);
  }

  // ========== DISTRITOS ==========

  getDistritosByDepartamento(departamentoCodigo: string): Observable<DistritoDto[]> {
    return this.http.get<DistritoDto[]>(`${this.apiUrl}/departamentos/${departamentoCodigo}/distritos`);
  }

  searchDistritos(query: string, departamentoCodigo?: string): Observable<DistritoDto[]> {
    let params = new HttpParams().set('q', query);
    if (departamentoCodigo) {
      params = params.set('departamento', departamentoCodigo);
    }
    return this.http.get<DistritoDto[]>(`${this.apiUrl}/distritos/buscar`, { params });
  }

  getDistritoByCodigo(codigo: string): Observable<DistritoDto> {
    return this.http.get<DistritoDto>(`${this.apiUrl}/distritos/${codigo}`);
  }

  // ========== CIUDADES ==========

  getCiudadesByDistrito(distritoCodigo: string): Observable<CiudadDto[]> {
    return this.http.get<CiudadDto[]>(`${this.apiUrl}/distritos/${distritoCodigo}/ciudades`);
  }

  searchCiudades(query: string, distritoCodigo?: string): Observable<CiudadDto[]> {
    let params = new HttpParams().set('q', query);
    if (distritoCodigo) {
      params = params.set('distrito', distritoCodigo);
    }
    return this.http.get<CiudadDto[]>(`${this.apiUrl}/ciudades/buscar`, { params });
  }

  getCiudadByCodigo(codigo: string): Observable<CiudadDto> {
    return this.http.get<CiudadDto>(`${this.apiUrl}/ciudades/${codigo}`);
  }

  getCiudadById(id: number): Observable<CiudadDto> {
    return this.http.get<CiudadDto>(`${this.apiUrl}/ciudades/id/${id}`);
  }

  // ========== BARRIOS ==========

  getBarriosByCiudad(ciudadCodigo: string): Observable<BarrioDto[]> {
    return this.http.get<BarrioDto[]>(`${this.apiUrl}/ciudades/${ciudadCodigo}/barrios`);
  }

  searchBarrios(query: string, ciudadCodigo?: string): Observable<BarrioDto[]> {
    let params = new HttpParams().set('q', query);
    if (ciudadCodigo) {
      params = params.set('ciudad', ciudadCodigo);
    }
    return this.http.get<BarrioDto[]>(`${this.apiUrl}/barrios/buscar`, { params });
  }

  getBarrioByCodigo(codigo: string): Observable<BarrioDto> {
    return this.http.get<BarrioDto>(`${this.apiUrl}/barrios/${codigo}`);
  }
}