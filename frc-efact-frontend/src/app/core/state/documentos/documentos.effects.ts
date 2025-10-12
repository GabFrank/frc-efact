import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, exhaustMap } from 'rxjs/operators';
import { DocumentoElectronicoApiService } from '../../api/documento-electronico-api.service';
import * as DocumentosActions from './documentos.actions';

@Injectable()
export class DocumentosEffects {
  private readonly actions$ = inject(Actions);
  private readonly documentoApi = inject(DocumentoElectronicoApiService);

  loadDocumentos$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DocumentosActions.loadDocumentos),
      exhaustMap(({ estado }) =>
        this.documentoApi.getAll(estado).pipe(
          map((documentos) => DocumentosActions.loadDocumentosSuccess({ documentos })),
          catchError((error) =>
            of(DocumentosActions.loadDocumentosFailure({ error: error.message }))
          )
        )
      )
    )
  );

  consultarEstado$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DocumentosActions.consultarEstado),
      exhaustMap(({ id }) =>
        this.documentoApi.consultarEstado(id).pipe(
          map((documento) => DocumentosActions.consultarEstadoSuccess({ documento })),
          catchError((error) =>
            of(DocumentosActions.consultarEstadoFailure({ error: error.message }))
          )
        )
      )
    )
  );

  cancelarDocumento$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DocumentosActions.cancelarDocumento),
      exhaustMap(({ id, motivo }) =>
        this.documentoApi.cancelar(id, motivo).pipe(
          map(() => DocumentosActions.cancelarDocumentoSuccess({ id })),
          catchError((error) =>
            of(DocumentosActions.cancelarDocumentoFailure({ error: error.message }))
          )
        )
      )
    )
  );
}
