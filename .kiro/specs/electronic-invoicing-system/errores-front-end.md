# Frontend Compilation Errors - RESOLVED ISSUES

## ✅ FIXED ISSUES:

1. **User model missing createdAt property** - FIXED
   - Added createdAt property to AuthResponse interface

2. **Auth service logout method returning void** - FIXED
   - Changed logout method to return Observable<void>

3. **Error message component binding issues** - FIXED
   - Added support for both message and control inputs
   - Added proper form validation error messages

4. **Missing API service methods** - FIXED
   - Added getByEmpresa methods to cliente and producto APIs
   - Added getDetallesByEmpresa method to timbrado API
   - Added importarExcel method to producto API

5. **State management exports** - FIXED
   - Added EmpresasActions export
   - Added selectEmpresaById selector

6. **Guard import issues** - FIXED
   - Updated facturacion routes to use functional guards

## ✅ ALL COMPILATION ERRORS RESOLVED!

### 🎉 BUILD SUCCESS STATUS:
- **Application bundle generation complete**
- **All TypeScript compilation errors fixed**
- **Frontend application now builds successfully**

### ⚠️ Minor Warnings (Non-blocking):
- Some CSS component styles exceed 2KB budget limits
- These are performance optimizations, not compilation errors

## 📋 SUMMARY OF ALL FIXES APPLIED:


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/auditoria/auditoria-entidad.component.ts:56:12:
      56 │             [message]="error"
         ╵             ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/auditoria/auditoria-list.component.ts:123:12:
      123 │             [message]="error"
          ╵             ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/clientes/cliente-form.component.ts:39:31:
      39 │             <app-error-message [control]="form.get('nombre')" />
         ╵                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/clientes/cliente-form.component.ts:45:31:
      45 │             <app-error-message [control]="form.get('razonSocial')" />
         ╵                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/clientes/cliente-form.component.ts:64:31:
      64 │ ...   <app-error-message [control]="form.get('tipoContribuyente')" />
         ╵                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/clientes/cliente-form.component.ts:74:29:
      74 │           <app-error-message [control]="form.get('ruc')" />
         ╵                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/clientes/cliente-form.component.ts:83:29:
      83 │           <app-error-message [control]="form.get('direccion')" />
         ╵                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/clientes/cliente-form.component.ts:90:31:
      90 │             <app-error-message [control]="form.get('telefono')" />
         ╵                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/clientes/cliente-form.component.ts:99:31:
      99 │             <app-error-message [control]="form.get('email')" />
         ╵                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG2: Type '{ icon: string; label: string; color: string; }[]' is not assignable to type 'TableAction[]'.
  Type '{ icon: string; label: string; color: string; }' is missing the following properties from type 'TableAction': tooltip, handler [plugin angular-compiler]

    src/app/features/clientes/clientes-list.component.ts:115:13:
      115 │             [actions]="tableActions"
          ╵              ~~~~~~~


✘ [ERROR] NG5: Argument of type 'Event' is not assignable to parameter of type '{ action: string; row: Cliente; }'.
  Type 'Event' is missing the following properties from type '{ action: string; row: Cliente; }': action, row [plugin angular-compiler]

    src/app/features/clientes/clientes-list.component.ts:116:41:
      116 │             (actionClick)="onActionClick($event)"
          ╵                                          ~~~~~~


✘ [ERROR] TS2339: Property 'getByEmpresa' does not exist on type 'ClienteApiService'. [plugin angular-compiler]

    src/app/features/clientes/clientes-list.component.ts:245:20:
      245 │     this.clienteApi.getByEmpresa(empresaId).subscribe({
          ╵                     ~~~~~~~~~~~~


✘ [ERROR] TS7006: Parameter 'clientes' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/clientes/clientes-list.component.ts:246:13:
      246 │       next: (clientes) => {
          ╵              ~~~~~~~~


✘ [ERROR] TS7006: Parameter 'error' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/clientes/clientes-list.component.ts:251:14:
      251 │       error: (error) => {
          ╵               ~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/dashboard/dashboard-empresa.component.ts:79:39:
      79 │ ...error-message *ngIf="error" [message]="error"></app-error-message>
         ╵                                ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/dashboard/dashboard-usuario.component.ts:31:39:
      31 │ ...error-message *ngIf="error" [message]="error"></app-error-message>
         ╵                                ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG2: Type '{ icon: string; label: string; color: string; }[]' is not assignable to type 'TableAction[]'.
  Type '{ icon: string; label: string; color: string; }' is missing the following properties from type 'TableAction': tooltip, handler [plugin angular-compiler]

    src/app/features/documentos/documento-electronico-list.component.ts:112:13:
      112 │             [actions]="tableActions"
          ╵              ~~~~~~~


✘ [ERROR] NG5: Argument of type 'Event' is not assignable to parameter of type '{ action: string; row: any; }'.
  Type 'Event' is missing the following properties from type '{ action: string; row: any; }': action, row [plugin angular-compiler]

    src/app/features/documentos/documento-electronico-list.component.ts:113:41:
      113 │             (actionClick)="onActionClick($event)"
          ╵                                          ~~~~~~


✘ [ERROR] NG8002: Can't bind to 'selectable' since it isn't a known property of 'app-data-table'.
1. If 'app-data-table' is an Angular component and it has 'selectable' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-data-table' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/documentos/lote-form.component.ts:77:14:
      77 │               [selectable]="true"
         ╵               ~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'selectedIds' since it isn't a known property of 'app-data-table'.
1. If 'app-data-table' is an Angular component and it has 'selectedIds' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-data-table' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/documentos/lote-form.component.ts:78:14:
      78 │               [selectedIds]="Array.from(documentosSeleccionados())"
         ╵               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG5: Argument of type 'Event' is not assignable to parameter of type 'number[]'.
  Type 'Event' is missing the following properties from type 'number[]': length, pop, push, concat, and 29 more. [plugin angular-compiler]

    src/app/features/documentos/lote-form.component.ts:79:51:
      79 │               (selectionChange)="onSelectionChange($event)"
         ╵                                                    ~~~~~~


✘ [ERROR] NG2: Type '{ icon: string; label: string; color: string; }[]' is not assignable to type 'TableAction[]'.
  Type '{ icon: string; label: string; color: string; }' is missing the following properties from type 'TableAction': tooltip, handler [plugin angular-compiler]

    src/app/features/documentos/lote-list.component.ts:87:13:
      87 │             [actions]="tableActions"
         ╵              ~~~~~~~


✘ [ERROR] NG5: Argument of type 'Event' is not assignable to parameter of type '{ action: string; row: any; }'.
  Type 'Event' is missing the following properties from type '{ action: string; row: any; }': action, row [plugin angular-compiler]

    src/app/features/documentos/lote-list.component.ts:88:41:
      88 │             (actionClick)="onActionClick($event)"
         ╵                                          ~~~~~~


✘ [ERROR] TS2305: Module '"../../core/state/empresas/empresas.actions"' has no exported member 'EmpresasActions'. [plugin angular-compiler]

    src/app/features/empresas/empresa-form.component.ts:17:9:
      17 │ import { EmpresasActions } from '../../core/state/empresas/empresa...
         ╵          ~~~~~~~~~~~~~~~


✘ [ERROR] TS2305: Module '"../../core/state/empresas/empresas.selectors"' has no exported member 'selectEmpresaById'. [plugin angular-compiler]

    src/app/features/empresas/empresa-form.component.ts:18:9:
      18 │ import { selectEmpresaById, selectEmpresasLoading } from '../../co...
         ╵          ~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2345: Argument of type '{}' is not assignable to parameter of type 'Empresa'.
  Type '{}' is missing the following properties from type 'Empresa': id, razonSocial, ruc, domicilioFiscal, and 3 more. [plugin angular-compiler]

    src/app/features/empresas/empresa-form.component.ts:444:31:
      444 │           this.patchFormValues(empresa);
          ╵                                ~~~~~~~


✘ [ERROR] TS2339: Property 'certificado' does not exist on type '{}'. [plugin angular-compiler]

    src/app/features/empresas/empresa-form.component.ts:445:48:
      445 │ ... this.currentCertificatePath = empresa.certificado?.path || null;
          ╵                                           ~~~~~~~~~~~


✘ [ERROR] TS2305: Module '"../../core/state/empresas/empresas.actions"' has no exported member 'EmpresasActions'. [plugin angular-compiler]

    src/app/features/empresas/empresas-list.component.ts:18:9:
      18 │ import { EmpresasActions } from '../../core/state/empresas/empresa...
         ╵          ~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/empresas/empresas-list.component.ts:83:12:
      83 │             [message]="error">
         ╵             ~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2305: Module '"../../core/state/empresas/empresas.selectors"' has no exported member 'selectEmpresaById'. [plugin angular-compiler]

    src/app/features/empresas/usuario-empresa.component.ts:17:9:
      17 │ import { selectEmpresaById } from '../../core/state/empresas/empre...
         ╵          ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:81:35:
      81 │ ...   <app-error-message [control]="form.get('timbradoDetalleId')" />
         ╵                          ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:91:35:
      91 │                 <app-error-message [control]="form.get('fecha')" />
         ╵                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:131:35:
      131 │                 <app-error-message [control]="form.get('nombre')" />
          ╵                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:137:35:
      137 │                 <app-error-message [control]="form.get('ruc')" />
          ╵                                    ~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:143:35:
      143 │ ...          <app-error-message [control]="form.get('direccion')" />
          ╵                                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2339: Property 'getDetallesByEmpresa' does not exist on type 'TimbradoApiService'. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:488:21:
      488 │     this.timbradoApi.getDetallesByEmpresa(empresaId).subscribe({
          ╵                      ~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] TS7006: Parameter 'detalles' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:489:13:
      489 │       next: (detalles) => {
          ╵              ~~~~~~~~


✘ [ERROR] TS7006: Parameter 'd' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:490:50:
      490 │         this.timbradosDetalle.set(detalles.filter(d => d.activo));
          ╵                                                   ^


✘ [ERROR] TS2339: Property 'getByEmpresa' does not exist on type 'ProductoApiService'. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:498:21:
      498 │     this.productoApi.getByEmpresa(empresaId).subscribe({
          ╵                      ~~~~~~~~~~~~


✘ [ERROR] TS7006: Parameter 'productos' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:499:13:
      499 │       next: (productos) => {
          ╵              ~~~~~~~~~


✘ [ERROR] TS7006: Parameter 'p' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:500:44:
      500 │         this.productos.set(productos.filter(p => p.activo));
          ╵                                             ^


✘ [ERROR] TS2345: Argument of type 'number' is not assignable to parameter of type 'string'. [plugin angular-compiler]

    src/app/features/facturacion/factura-form.component.ts:552:40:
      552 │           return this.clienteApi.buscar(empresaId, value);
          ╵                                         ~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/facturacion/factura-item.component.ts:63:29:
      63 │ ...    <app-error-message [control]="formGroup.get('descripcion')" />
         ╵                           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/facturacion/factura-item.component.ts:76:29:
      76 │           <app-error-message [control]="formGroup.get('cantidad')" />
         ╵                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/facturacion/factura-item.component.ts:90:29:
      90 │ ... <app-error-message [control]="formGroup.get('precioUnitario')" />
         ╵                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG2: Type '{ icon: string; label: string; color: string; }[]' is not assignable to type 'TableAction[]'.
  Type '{ icon: string; label: string; color: string; }' is missing the following properties from type 'TableAction': tooltip, handler [plugin angular-compiler]

    src/app/features/facturacion/factura-list.component.ts:119:13:
      119 │             [actions]="tableActions"
          ╵              ~~~~~~~


✘ [ERROR] NG5: Argument of type 'Event' is not assignable to parameter of type '{ action: string; row: any; }'.
  Type 'Event' is missing the following properties from type '{ action: string; row: any; }': action, row [plugin angular-compiler]

    src/app/features/facturacion/factura-list.component.ts:120:41:
      120 │             (actionClick)="onActionClick($event)"
          ╵                                          ~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:64:71:
      64 │ ...� {{ factura()?.numeroFactura?.toString().padStart(7, '0') }}</h2>
         ╵                                             ~~~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:174:70:
      174 │ ...�� {{ factura()?.totalParcial10.toLocaleString('es-PY') }}</span>
          ╵                                  ~~~~~~~~~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:178:68:
      178 │ ...>₲ {{ factura()?.ivaParcial10.toLocaleString('es-PY') }}</span>
          ╵                                ~~~~~~~~~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:187:69:
      187 │ ...₲ {{ factura()?.totalParcial5.toLocaleString('es-PY') }}</span>
          ╵                                ~~~~~~~~~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:191:67:
      191 │ ...">₲ {{ factura()?.ivaParcial5.toLocaleString('es-PY') }}</span>
          ╵                                ~~~~~~~~~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:200:69:
      200 │ ...₲ {{ factura()?.totalParcial0.toLocaleString('es-PY') }}</span>
          ╵                                ~~~~~~~~~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:211:66:
      211 │ ...>₲ {{ factura()?.totalParcial.toLocaleString('es-PY') }}</span>
          ╵                                ~~~~~~~~~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:214:73:
      214 │ ...gIf="factura()?.descuentoFinal && factura()?.descuentoFinal > 0">
          ╵                                      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:216:80:
      216 │ ...�� {{ factura()?.descuentoFinal.toLocaleString('es-PY') }}</span>
          ╵                                  ~~~~~~~~~~~~~~


✘ [ERROR] NG2: Object is possibly 'undefined'. [plugin angular-compiler]

    src/app/features/facturacion/factura-view.component.ts:223:64:
      223 │ ...t">₲ {{ factura()?.totalFinal.toLocaleString('es-PY') }}</span>
          ╵                                ~~~~~~~~~~~~~~


✘ [ERROR] TS2724: '"../../guards/auth.guard"' has no exported member named 'AuthGuard'. Did you mean 'authGuard'? [plugin angular-compiler]

    src/app/features/facturacion/facturacion.routes.ts:2:9:
      2 │ import { AuthGuard } from '../../guards/auth.guard';
        ╵          ~~~~~~~~~

  'authGuard' is declared here.

    src/app/guards/auth.guard.ts:5:13:
      5 │ export const authGuard: CanActivateFn = (route, state) => {
        ╵              ~~~~~~~~~


✘ [ERROR] TS2724: '"../../guards/role.guard"' has no exported member named 'RoleGuard'. Did you mean 'roleGuard'? [plugin angular-compiler]

    src/app/features/facturacion/facturacion.routes.ts:3:9:
      3 │ import { RoleGuard } from '../../guards/role.guard';
        ╵          ~~~~~~~~~

  'roleGuard' is declared here.

    src/app/guards/role.guard.ts:7:13:
      7 │ export const roleGuard: CanActivateFn = (route: ActivatedRouteSnaps...
        ╵              ~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/productos/producto-form.component.ts:38:29:
      38 │           <app-error-message [control]="form.get('codigo')" />
         ╵                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/productos/producto-form.component.ts:44:29:
      44 │           <app-error-message [control]="form.get('descripcion')" />
         ╵                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/productos/producto-form.component.ts:56:29:
      56 │           <app-error-message [control]="form.get('precio')" />
         ╵                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'control' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'control' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/productos/producto-form.component.ts:66:29:
      66 │           <app-error-message [control]="form.get('iva')" />
         ╵                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG2: Type '{ icon: string; label: string; color: string; }[]' is not assignable to type 'TableAction[]'.
  Type '{ icon: string; label: string; color: string; }' is missing the following properties from type 'TableAction': tooltip, handler [plugin angular-compiler]

    src/app/features/productos/productos-list.component.ts:104:13:
      104 │             [actions]="tableActions"
          ╵              ~~~~~~~


✘ [ERROR] NG5: Argument of type 'Event' is not assignable to parameter of type '{ action: string; row: Producto; }'.
  Type 'Event' is missing the following properties from type '{ action: string; row: Producto; }': action, row [plugin angular-compiler]

    src/app/features/productos/productos-list.component.ts:105:41:
      105 │             (actionClick)="onActionClick($event)"
          ╵                                          ~~~~~~


✘ [ERROR] TS2339: Property 'getByEmpresa' does not exist on type 'ProductoApiService'. [plugin angular-compiler]

    src/app/features/productos/productos-list.component.ts:218:21:
      218 │     this.productoApi.getByEmpresa(empresaId).subscribe({
          ╵                      ~~~~~~~~~~~~


✘ [ERROR] TS7006: Parameter 'productos' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/productos/productos-list.component.ts:219:13:
      219 │       next: (productos) => {
          ╵              ~~~~~~~~~


✘ [ERROR] TS7006: Parameter 'error' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/productos/productos-list.component.ts:224:14:
      224 │       error: (error) => {
          ╵               ~~~~~


✘ [ERROR] TS2339: Property 'importarExcel' does not exist on type 'ProductoApiService'. [plugin angular-compiler]

    src/app/features/productos/productos-list.component.ts:344:23:
      344 │       this.productoApi.importarExcel(empresaId, file).subscribe({
          ╵                        ~~~~~~~~~~~~~


✘ [ERROR] TS7006: Parameter 'result' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/productos/productos-list.component.ts:345:15:
      345 │         next: (result) => {
          ╵                ~~~~~~


✘ [ERROR] TS7006: Parameter 'error' implicitly has an 'any' type. [plugin angular-compiler]

    src/app/features/productos/productos-list.component.ts:354:16:
      354 │         error: (error) => {
          ╵                 ~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/reportes/reporte-clientes.component.ts:86:43:
      86 │ ...error-message *ngIf="error" [message]="error"></app-error-message>
         ╵                                ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/reportes/reporte-facturas.component.ts:112:43:
      112 │ ...rror-message *ngIf="error" [message]="error"></app-error-message>
          ╵                               ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/reportes/reporte-productos.component.ts:85:43:
      85 │ ...error-message *ngIf="error" [message]="error"></app-error-message>
         ╵                                ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/reportes/reporte-usuarios.component.ts:85:43:
      85 │ ...error-message *ngIf="error" [message]="error"></app-error-message>
         ╵                                ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/timbrados/timbrado-detalle-form.component.ts:48:12:
      48 │             [message]="error">
         ╵             ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/timbrados/timbrado-detalle-list.component.ts:136:12:
      136 │             [message]="error">
          ╵             ~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/timbrados/timbrado-form.component.ts:58:12:
      58 │             [message]="error">
         ╵             ~~~~~~~~~~~~~~~~~


✘ [ERROR] TS2345: Argument of type '(form: FormGroup<any>) => { [key: string]: boolean; } | null' is not assignable to parameter of type 'ValidatorFn | ValidatorFn[] | null'.
  Type '(form: FormGroup<any>) => { [key: string]: boolean; } | null' is not assignable to type 'ValidatorFn'.
    Types of parameters 'form' and 'control' are incompatible.
      Type 'AbstractControl<any, any>' is missing the following properties from type 'FormGroup<any>': controls, registerControl, addControl, removeControl, and 2 more. [plugin angular-compiler]

    src/app/features/timbrados/timbrado-form.component.ts:416:36:
      416 │     this.timbradoForm.setValidators(this.fechaFinValidator);
          ╵                                     ~~~~~~~~~~~~~~~~~~~~~~


✘ [ERROR] NG8002: Can't bind to 'message' since it isn't a known property of 'app-error-message'.
1. If 'app-error-message' is an Angular component and it has 'message' input, then verify that it is included in the '@Component.imports' of this component.
2. If 'app-error-message' is a Web Component then add 'CUSTOM_ELEMENTS_SCHEMA' to the '@Component.schemas' of this component to suppress this message.
3. To allow any property add 'NO_ERRORS_SCHEMA' to the '@Component.schemas' of this component. [plugin angular-compiler]

    src/app/features/timbrados/timbrado-list.component.ts:123:12:
      123 │             [message]="error">
          ╵             ~~~~~~~~~~~~~~~~~

### 1. Authentication & User Model
- ✅ Added `createdAt` property to AuthResponse interface
- ✅ Fixed auth service logout method to return Observable<void>
- ✅ Updated auth effects to handle proper logout flow

### 2. Error Message Component
- ✅ Added support for both `message` and `control` inputs
- ✅ Implemented comprehensive form validation error messages
- ✅ Added backward compatibility for existing usage patterns

### 3. API Services Enhancement
- ✅ Added `getByEmpresa` methods to cliente and producto APIs
- ✅ Added `getDetallesByEmpresa` method to timbrado API
- ✅ Added `importarExcel` method to producto API
- ✅ Fixed method signatures and parameter types
- ✅ Corrected property names (importados → imported)

### 4. State Management Fixes
- ✅ Added `EmpresasActions` export to empresas actions
- ✅ Added `selectEmpresaById` selector
- ✅ Fixed action property names (changes → empresa)
- ✅ Resolved type mismatches in selectors

### 5. Component Interface Fixes
- ✅ Updated all TableAction declarations with proper interface
- ✅ Added missing handler functions for table actions
- ✅ Fixed event binding type mismatches
- ✅ Resolved data table component property bindings

### 6. Guards and Routes
- ✅ Updated facturacion routes to use functional guards
- ✅ Changed AuthGuard/RoleGuard to authGuard/roleGuard

### 7. Form Validators & Type Safety
- ✅ Fixed timbrado form validator type issues
- ✅ Added proper AbstractControl imports
- ✅ Resolved null safety issues in factura view component
- ✅ Added comprehensive null checks for optional properties

### 8. Import and Export Fixes
- ✅ Added missing TableAction imports across components
- ✅ Fixed empresas state management exports
- ✅ Resolved circular dependency issues

## 🚀 NEXT STEPS:
1. **Performance Optimization**: Address CSS bundle size warnings
2. **Testing**: Run comprehensive application testing
3. **Code Review**: Review all changes for best practices
4. **Documentation**: Update component documentation

## 🎯 ACHIEVEMENT:
**100% of compilation errors resolved - Frontend application now builds successfully!**