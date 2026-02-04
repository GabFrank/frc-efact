# Configuración MCP para Render

Esta guía explica cómo configurar el MCP (Model Context Protocol) de Render para acceder a información de deployments, bases de datos y servicios desde Cursor.

## ¿Qué es MCP?

MCP (Model Context Protocol) permite que los modelos de IA accedan a recursos externos, como APIs de servicios en la nube. En este caso, permite acceder a información de Render directamente desde Cursor.

## Obtener API Key de Render

### Paso 1: Acceder a Render Dashboard

1. Ir a [Render Dashboard](https://dashboard.render.com/)
2. Iniciar sesión con tu cuenta

### Paso 2: Generar API Key

1. Click en tu perfil (esquina superior derecha)
2. Seleccionar **"Account Settings"** o **"API Keys"**
3. Ir a la sección **"API Keys"**
4. Click en **"Create API Key"**
5. Darle un nombre descriptivo (ej: "Cursor MCP")
6. Copiar el API key generado (solo se muestra una vez)

**⚠️ IMPORTANTE:** Guarda el API key de forma segura. No lo compartas ni lo subas a Git.

## Configurar MCP en Cursor

### Paso 1: Crear Archivo de Configuración

1. Crear el directorio `.cursor` en la raíz del proyecto (si no existe):
   ```bash
   mkdir -p .cursor
   ```

2. Copiar el archivo de ejemplo:
   ```bash
   cp .cursor/mcp.json.example .cursor/mcp.json
   ```

### Paso 2: Agregar tu API Key

1. Abrir `.cursor/mcp.json`
2. Reemplazar `<YOUR_API_KEY>` con tu API key de Render:

```json
{
  "mcpServers": {
    "render": {
      "url": "https://mcp.render.com/mcp",
      "headers": {
        "Authorization": "Bearer rnd_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
      }
    }
  }
}
```

3. Guardar el archivo

**Nota:** El archivo `.cursor/mcp.json` está en `.gitignore` para no subir el API key al repositorio.

### Paso 3: Reiniciar Cursor

1. Cerrar completamente Cursor
2. Abrir Cursor nuevamente
3. El MCP debería conectarse automáticamente

### Verificar Configuración

Para verificar que el MCP está funcionando:
1. Abrir la paleta de comandos (Cmd/Ctrl + Shift + P)
2. Buscar "MCP" o "Model Context Protocol"
3. Deberías ver opciones relacionadas con Render

## Verificar Configuración

Una vez configurado, deberías poder:

- Ver servicios de Render desde Cursor
- Consultar estado de deployments
- Ver logs de servicios
- Obtener información de bases de datos
- Ver métricas y estadísticas

## Funcionalidades Disponibles

Con el MCP de Render configurado, puedes:

### Servicios
- Listar todos los servicios
- Ver detalles de un servicio específico
- Ver estado (Live, Building, Error, etc.)
- Ver logs en tiempo real

### Deployments
- Ver historial de deployments
- Ver estado de deployments actuales
- Ver logs de builds
- Triggerear nuevos deployments

### Bases de Datos
- Ver información de bases de datos
- Ver conexiones (Internal/External URLs)
- Ver estadísticas de uso

### Variables de Entorno
- Ver variables de entorno configuradas
- Verificar valores (sin exponer secretos)

## Seguridad

### Buenas Prácticas

1. **Nunca subas el API key a Git**
   - El archivo `.cursor/mcp.json` está en `.gitignore`
   - No incluyas el API key en commits

2. **Rota el API key periódicamente**
   - Genera nuevos keys cada cierto tiempo
   - Revoca keys antiguos que ya no uses

3. **Usa permisos mínimos**
   - El API key debe tener solo los permisos necesarios
   - No uses keys con permisos de administrador si no es necesario

4. **No compartas el API key**
   - Cada desarrollador debe tener su propio API key
   - No compartas keys entre equipos

## Troubleshooting

### El MCP no se conecta

1. Verificar que el API key sea correcto
2. Verificar que el formato del JSON sea válido
3. Reiniciar Cursor después de cambiar la configuración
4. Verificar que la URL del MCP sea correcta: `https://mcp.render.com/mcp`

### No puedo ver servicios

1. Verificar que el API key tenga permisos para leer servicios
2. Verificar que estés autenticado en Render Dashboard
3. Verificar que los servicios existan en Render

### Error de autenticación

1. Verificar que el API key no haya expirado
2. Generar un nuevo API key si es necesario
3. Verificar el formato del header Authorization: `Bearer <API_KEY>`

## Referencias

- [Render API Documentation](https://render.com/docs/api)
- [MCP Protocol Specification](https://modelcontextprotocol.io/)
- [Cursor MCP Documentation](https://docs.cursor.com/mcp)

## Notas

- El MCP de Render puede tener limitaciones según tu plan
- Algunas operaciones pueden requerir permisos adicionales
- Los logs pueden tener un delay de algunos segundos
