# Configuración de GitHub Packages para jsifenlib

## ¿Por qué es necesario?

La librería `jsifenlib` (fork de GabFrank) está publicada en GitHub Packages, no en Maven Central. Para descargarla, Maven necesita autenticarse con GitHub.

## Pasos de Configuración

### 1. Crear Personal Access Token (PAT)

1. Ve a https://github.com/settings/tokens
2. Click en **"Generate new token (classic)"**
3. Dale un nombre descriptivo (ej: "Maven GitHub Packages")
4. Selecciona el scope **`read:packages`**
5. Click en "Generate token"
6. **IMPORTANTE**: Copia el token generado (no podrás verlo de nuevo)

### 2. Configurar Maven Settings

Edita o crea el archivo `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github-jsifenlib</id>
            <username>TU_USUARIO_GITHUB</username>
            <password>TU_PERSONAL_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```

**Reemplaza**:
- `TU_USUARIO_GITHUB`: Tu nombre de usuario de GitHub
- `TU_PERSONAL_ACCESS_TOKEN`: El token que generaste en el paso 1

**Nota**: El `<id>` debe coincidir con el ID del repositorio en `pom.xml`.

### 3. Verificar Configuración

Ejecuta:

```bash
cd frc-efact-backend
mvn clean install
```

Si todo está bien configurado, Maven descargará la dependencia `jsifenlib`.

## Troubleshooting

### Error 401: Unauthorized

```
Could not transfer artifact io.github.gabfrank:jsifenlib:pom:0.2.4-frc.13
status code: 401, reason phrase: Unauthorized (401)
```

**Soluciones**:
1. Verifica que el token tenga el scope `read:packages`
2. Verifica que el `<id>` en `settings.xml` sea `github-jsifenlib`
3. Verifica que el token no haya expirado
4. Verifica que el username sea correcto

### No encuentra la dependencia

```bash
# Limpiar cache de Maven
mvn dependency:purge-local-repository

# O forzar actualización
mvn clean install -U
```

### Ver logs detallados

```bash
mvn clean install -X
```

## Configuración para CI/CD

### GitHub Actions

```yaml
- name: Setup Maven Settings
  run: |
    mkdir -p ~/.m2
    echo '<settings>
      <servers>
        <server>
          <id>github-jsifenlib</id>
          <username>${{ github.actor }}</username>
          <password>${{ secrets.GITHUB_TOKEN }}</password>
        </server>
      </servers>
    </settings>' > ~/.m2/settings.xml

- name: Build with Maven
  run: mvn clean install
```

### Render / Otras Plataformas

Agregar variables de entorno:
- `GITHUB_USERNAME`: Tu usuario de GitHub
- `GITHUB_TOKEN`: Tu Personal Access Token

Y configurar en el script de build:

```bash
mkdir -p ~/.m2
cat > ~/.m2/settings.xml << EOF
<settings>
  <servers>
    <server>
      <id>github-jsifenlib</id>
      <username>${GITHUB_USERNAME}</username>
      <password>${GITHUB_TOKEN}</password>
    </server>
  </servers>
</settings>
EOF

mvn clean install
```

## Alternativa: Instalar Localmente

Si no quieres configurar GitHub Packages, puedes clonar y compilar la librería localmente:

```bash
# Clonar el repositorio
git clone https://github.com/GabFrank/rshk-jsifenlib.git
cd rshk-jsifenlib

# Compilar e instalar en repositorio local de Maven
mvn clean install

# Ahora tu proyecto puede usar la dependencia sin GitHub Packages
```

Luego en tu `pom.xml`, puedes comentar el repositorio de GitHub:

```xml
<!-- <repositories>
    <repository>
        <id>github-jsifenlib</id>
        <url>https://maven.pkg.github.com/GabFrank/jsifenlib</url>
    </repository>
</repositories> -->
```

## Verificación

Para verificar que la dependencia se descargó correctamente:

```bash
# Ver el árbol de dependencias
mvn dependency:tree | grep jsifenlib

# Debería mostrar algo como:
# [INFO] +- io.github.gabfrank:jsifenlib:jar:0.2.4-frc.13:compile
```

## Referencias

- [GitHub Packages Documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
- [jsifenlib Repository](https://github.com/GabFrank/rshk-jsifenlib)
- [jsifenlib README](https://github.com/GabFrank/rshk-jsifenlib/blob/master/README.md)
