# Configuración de Auth0 para FRC eFact

Esta guía detalla los pasos necesarios para configurar un tenant de Auth0 y obtener las credenciales necesarias para la integración con FRC eFact.

## 1. Crear Cuenta y Tenant

1.  Ve a [auth0.com](https://auth0.com) y regístrate (o inicia sesión).
2.  Crea un nuevo **Tenant** (o usa uno existente).
    *   Nombre del tenant: `frc-efact-dev` (o similar).
    *   Región: Elige la más cercana a tus usuarios (ej. US).

## 2. Crear Aplicación (Frontend)

Esta aplicación representará al frontend Angular.

1.  En el menú lateral, ve a **Applications** -> **Applications**.
2.  Haz clic en **Create Application**.
3.  Nombre: `FRC eFact Frontend`.
4.  Tipo: **Single Page Web Applications**.
5.  Haz clic en **Create**.
6.  Ve a la pestaña **Settings** de la nueva aplicación.
7.  Copia el **Domain** y el **Client ID**. Los necesitarás más tarde.
8.  En **Application URIs**, configura lo siguiente (ajusta los puertos si es necesario):
    *   **Allowed Callback URLs**: `http://localhost:4200`
    *   **Allowed Logout URLs**: `http://localhost:4200`
    *   **Allowed Web Origins**: `http://localhost:4200`
    *   **IMPORTANTE**: Deja vacío el campo **Application Login URI**. Si intentas poner `http://localhost:4200` ahí, dará un error porque ese campo requiere HTTPS. No es necesario para nuestro flujo de trabajo.
9.  **Sobre Cross-Origin Authentication**:
    *   Generalmente **NO** necesitas habilitar el toggle específico de "Cross-Origin Authentication" en las configuraciones avanzadas si usas el Login Universal (redirección).
    *   Sin embargo, es **CRÍTICO** que `http://localhost:4200` esté en el campo **Allowed Web Origins** mencionado arriba.
10. Haz clic en **Save Changes** al final de la página.

## 3. Crear API (Backend)

Esta configuración permite que el backend (Spring Boot) valide los tokens emitidos por Auth0.

1.  En el menú lateral, ve a **Applications** -> **APIs**.
2.  Haz clic en **Create API**.
3.  Nombre: `FRC eFact API`.
4.  Identifier: `https://api.frcefact.com` (Este será tu `audience`).
    *   *Nota: No necesita ser una URL real y accesible, es solo un identificador lógico.*
5.  Signing Algorithm: `RS256`.
6.  Haz clic en **Create**.

## 4. Configurar Conexiones Sociales (Google)

Para permitir login con Google, debes configurarlo tanto en Google Cloud como en Auth0.

### Paso A: Crear Credenciales en Google Cloud (Consola en Español)
1.  Ve a [Google Cloud Console](https://console.cloud.google.com/).
2.  Crea un proyecto nuevo (o usa uno existente).
3.  Ve a **APIs y servicios** -> **Credenciales**.
    *   *Nota: Si vas a "Pantalla de consentimiento de OAuth" y ves un mensaje diciendo "Aún no configuraste ningún cliente...", ignóralo por un momento. El flujo correcto suele ser ir a "Credenciales" y desde ahí te pedirá configurar el consentimiento si no lo has hecho.*
    *   Haz clic en **+ Crear credenciales** -> **ID de cliente de OAuth**.
    *   Si te pide "Configurar pantalla de consentimiento" primero, haz clic en ese botón.
        *   Selecciona **Externos** y **Crear**.
        *   Rellena **Nombre de la aplicación** (`FRC eFact`), **Correo de asistencia** y **Datos de contacto**.
        *   Haz clic en **Guardar y continuar**.
        *   En **Permisos**, puedes solo dar a **Guardar y continuar** (o añadir `email` y `profile`).
        *   En **Usuarios de prueba**, añade TU correo de Gmail. **Guardar y continuar**.
        *   Vuelve a **Credenciales**.
4.  Ahora sí, en **Credenciales**, haz clic en **+ Crear credenciales** -> **ID de cliente de OAuth**.
5.  **Tipo de aplicación**: Selecciona **Aplicación web**.
6.  **Nombre**: `Auth0 Login` (o el que prefieras).
7.  **Orígenes autorizados de JavaScript**:
    *   **DEJAR VACÍO**.
    *   (No es necesario porque la autenticación la maneja Auth0 desde el servidor, no un JS directo desde tu navegador a Google).
8.  **URIs de redireccionamiento autorizados** (Authorized redirect URIs):
    *   **AQUÍ ES DONDE VA LA MAGIA**. Debes poner la URL de callback de tu tenant de Auth0.
    *   Formato: `https://TU_DOMINIO_AUTH0/login/callback`
    *   Ejemplo: `https://dev-gp1w0u2bgw35q6v5.us.auth0.com/login/callback`
    *   (Encontrarás este dominio (`dev-xyz...`) en el dashboard de Auth0, arriba a la izquierda o en la configuración de tu aplicación).
9.  Haz clic en **Crear**.
10. Copia el **ID de cliente** (Client ID) y el **Secreto de cliente** (Client Secret) que aparecerán en la ventana emergente.

### Paso B: Configurar la Conexión en Auth0
1.  En Auth0 Dashboard, ve a **Authentication** -> **Social**.
2.  Haz clic en **Create Connection**.
3.  Busca y selecciona **Google / Gmail**.
4.  Pega el **Client ID** y **Client Secret** que obtuviste de Google.
5.  (Opcional) Marca "Sync User Profile Attributes" si deseas que Auth0 actualice los datos del usuario en cada login.
6.  Haz clic en **Create**.
7.  **IMPORTANTE**: Ve a la pestaña **Applications** (dentro de la configuración de la conexión de Google) y activa el switch para `FRC eFact Frontend`.

## 5. Resumen de Credenciales

Una vez completado, deberías tener los siguientes datos para configurar la aplicación:

*   **Auth0 Domain**: (ej. `dev-xyz.us.auth0.com`)
*   **Auth0 Client ID**: (ej. `AbC123...`)
*   **Auth0 Audience**: `https://api.frcefact.com` (o el que hayas elegido)

## 6. Configuración de Roles (Opcional / Futuro)

Para mapear roles de Auth0 a roles de FRC eFact, se puede configurar una "Action" en Auth0 para incluir roles en el token, pero por ahora usaremos la vinculación de cuentas en el backend.
