# 💼 Mejora de Actividad Económica en Formulario de Empresas

## Resumen

Se mejoró la sección de actividad económica del formulario de empresas para tener una interfaz más intuitiva con campos separados para código y descripción, y la capacidad de agregar múltiples actividades secundarias dinámicamente.

## Cambios Implementados

### 1. Diseño Mejorado de Campos

**Actividad Principal:**
- Campo de código: 30% del ancho
- Campo de descripción: 70% del ancho
- Ambos campos requeridos

**Actividades Secundarias:**
- Campos dinámicos que se pueden agregar/eliminar
- Mismo diseño 30/70 para código/descripción
- Botón "+" para agregar nuevas actividades
- Botón de eliminar (icono de basura) para cada actividad

### 2. Estructura Visual

```
┌─────────────────────────────────────────────────┐
│ Actividad Económica                             │
├─────────────────────────────────────────────────┤
│ Actividad Principal *                           │
│ ┌──────────┬──────────────────────────────────┐ │
│ │ Código * │ Descripción *                    │ │
│ │ (30%)    │ (70%)                            │ │
│ └──────────┴──────────────────────────────────┘ │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ Actividades Secundarias              [+]   │ │
│ ├─────────────────────────────────────────────┤ │
│ │ ┌────────┬────────────────────────┬───┐    │ │
│ │ │ Código │ Descripción            │ 🗑 │    │ │
│ │ └────────┴────────────────────────┴───┘    │ │
│ │ ┌────────┬────────────────────────┬───┐    │ │
│ │ │ Código │ Descripción            │ 🗑 │    │ │
│ │ └────────┴────────────────────────┴───┘    │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### 3. Funcionalidad Implementada

**Agregar Actividad Secundaria:**
```typescript
agregarActividadSecundaria(): void {
  this.actividadesSecundarias.push({ codigo: '', descripcion: '' });
}
```

**Eliminar Actividad Secundaria:**
```typescript
eliminarActividadSecundaria(index: number): void {
  this.actividadesSecundarias.splice(index, 1);
  this.onActividadSecundariaChange();
}
```

**Sincronización Automática:**
```typescript
onActividadSecundariaChange(): void {
  // Convierte el array de actividades en strings separados por coma
  const codigos = this.actividadesSecundarias
    .filter(a => a.codigo.trim() !== '')
    .map(a => a.codigo.trim());
  
  const descripciones = this.actividadesSecundarias
    .filter(a => a.descripcion.trim() !== '')
    .map(a => a.descripcion.trim());

  this.actividadEconomica.get('codigosSecundarios')?.setValue(codigos.join(', '));
  this.actividadEconomica.get('descripcionesSecundarias')?.setValue(descripciones.join(', '));
}
```

### 4. Estilos CSS

**Campos de Actividad:**
```css
.codigo-field {
  flex: 0 0 30%;
  min-width: 150px;
}

.descripcion-field {
  flex: 1;
}
```

**Sección de Secundarias:**
```css
.actividades-secundarias-section {
  margin-top: 24px;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.actividad-secundaria {
  background-color: white;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 12px;
  border: 1px solid #e0e0e0;
}
```

**Estado Vacío:**
```css
.empty-state {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  color: #666;
  font-style: italic;
  background-color: white;
  border-radius: 4px;
  border: 1px dashed #ccc;
}
```

### 5. Carga de Datos en Modo Edición

Al editar una empresa, las actividades secundarias se cargan automáticamente:

```typescript
private patchFormValues(empresa: Empresa): void {
  // ... otros campos ...

  // Cargar actividades secundarias en el array
  if (empresa.actividadEconomica.codigosSecundarios && 
      empresa.actividadEconomica.descripcionesSecundarias) {
    const codigos = empresa.actividadEconomica.codigosSecundarios;
    const descripciones = empresa.actividadEconomica.descripcionesSecundarias;
    
    this.actividadesSecundarias = codigos.map((codigo, index) => ({
      codigo: codigo,
      descripcion: descripciones[index] || ''
    }));
  }
}
```

## Flujo de Usuario

### Crear Nueva Empresa

1. Usuario completa actividad principal (código y descripción)
2. Si necesita actividades secundarias, hace clic en el botón "+"
3. Aparece un nuevo par de campos (código/descripción)
4. Usuario completa los campos
5. Puede agregar más actividades con el botón "+"
6. Puede eliminar actividades con el botón de basura
7. Al guardar, los datos se convierten automáticamente en strings separados por coma

### Editar Empresa Existente

1. Se cargan los datos de la empresa
2. Actividad principal se muestra en sus campos
3. Actividades secundarias se cargan automáticamente como campos individuales
4. Usuario puede agregar/eliminar/modificar actividades
5. Al guardar, los cambios se sincronizan

## Formato de Datos

### En el Formulario (UI)
```typescript
actividadesSecundarias = [
  { codigo: '4711', descripcion: 'VENTA AL POR MENOR' },
  { codigo: '4719', descripcion: 'COMERCIO GENERAL' }
]
```

### En el Backend (Guardado)
```json
{
  "actividadEconomica": {
    "codigoPrincipal": "4711",
    "descripcionPrincipal": "VENTA AL POR MENOR EN COMERCIOS",
    "codigosSecundarios": ["4711", "4719"],
    "descripcionesSecundarias": ["VENTA AL POR MENOR", "COMERCIO GENERAL"]
  }
}
```

### En el FormControl (Interno)
```typescript
{
  codigosSecundarios: "4711, 4719",
  descripcionesSecundarias: "VENTA AL POR MENOR, COMERCIO GENERAL"
}
```

## Validaciones

- ✅ Actividad principal: código y descripción requeridos
- ✅ Actividades secundarias: opcionales
- ✅ Campos vacíos se filtran automáticamente
- ✅ Conversión automática a mayúsculas

## Imports Agregados

```typescript
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
```

## Beneficios

✅ **UX Mejorada** - Interfaz más intuitiva y visual
✅ **Flexibilidad** - Agregar/eliminar actividades fácilmente
✅ **Validación Visual** - Campos claramente separados
✅ **Proporción Adecuada** - 30% código, 70% descripción
✅ **Sincronización Automática** - No hay que preocuparse por el formato
✅ **Compatibilidad** - Funciona con el backend existente

## Testing

Para probar la funcionalidad:

1. **Crear empresa nueva:**
   - Completar actividad principal
   - Agregar 2-3 actividades secundarias
   - Guardar y verificar

2. **Editar empresa:**
   - Abrir empresa con actividades secundarias
   - Verificar que se cargan correctamente
   - Agregar/eliminar actividades
   - Guardar y verificar

3. **Validaciones:**
   - Intentar guardar sin actividad principal
   - Dejar actividades secundarias vacías
   - Verificar que se filtran correctamente

---

**Fecha**: Octubre 2025
**Componente**: `empresa-form.component.ts`
**Estado**: ✅ Implementado y funcionando
