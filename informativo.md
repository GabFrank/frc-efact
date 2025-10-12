En este archivo explicare completamente las funcionalidades que necesitamos implementar

Este sistema es una plataforma donde vamos a poder crear empresas, vincualr informaciones de facturacion (manual o electronica) como timbrados, detalles etc. Tambien para cada empresa podremos crear productos que seran utilizados para ser facturados, teniendo los datos de facturacion y productos vamos a poder crear facturas legales, cada factura legal tiene los itenes de factura que serian los productos y las informaciones de cantidades y valor, para cada factura se crea un documento electronico que posee las informaciones principales para generar la facturacion electronica (envio a un sistema del gobierno), los DE (documento electronico) son agrupados dentro de lotes, son los lotes los que efectivamente son enviados al servidor de sifen utilizando una libreria especializada. Los DEs son agrupados en un lote, el lote es enviado. La consulta del estado de un lote o DE se debe de hacer de forma separada, de forma reactiva o utilizando schedulers que hagan las consultas periodicamente para actualizar los estados de lotes y DEs.
Necesitamos crear un sistema de asignacion dee roles para los usuarios
Un usuario puede tener acceso a varias empresas
Una empresa puede ser accesado por varios usuarios
Un usuario puede asignar a otro usuario como lector/administrador de una empresa

Necesitamos un dashboard para usuario:
- cantidad de empresas, ultimo acceso, ultimas actividades

Dashboard para empresas:
- Total de facturas emitidas
- Total en guaranies de facturas emitidas en el mes actual
- Total en guaranies de facturas en 10, 5 y 0
- Ranking clientes con mas total de facturas

Reportes (agregar todos los filtros que creas neccesarios en cada lista):
- Total de facturacion por empresa (total de venta y total iva en 10, 5 y 0)
- Lista de facturas creadas
- Lista de facturas por clientes
- Lista de facturas por productos
- Lista de facturas por usuarios
- Lista de historial de modificaciones

Pantallas (algunas princcipales, luego puedes agregar mas conforme encuentres necesario)
- Registro de usuario
- Registro y lista de empresas (configuracion completa de una empresa)
- Registro y lista de timbrados, timbrados detalles
- Registro y lista de productos
- Dashboards y reportes
- Listar facturas y crear/editar nuevas facturas
- Puedes calcular que otras pantallas seran necesarias

estructura de base de datos:
- dare una idea general de algunas entidades principales:

No tengo un ejemplo para la entidad Empresa, pero puedes deducir los datos de acuerdo a los datos necesarios como Razon Social, ruc, path del certificado.pfx

Una empresa puede poseer varios timbrados

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "timbrado", schema = "financiero")
public class Timbrado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private Empresa empresa;

    private String razonSocial;

    private String ruc;

    private String numero;

    private Boolean isElectronico;

    private String csc;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    // Campos para documento electrónico
    private String email;
    private String tipoSociedad;
    private String domicilioFiscalDepartamento;
    private String domicilioFiscalCiudad;
    private String domicilioFiscalCodigoCiudad;
    private String domicilioFiscalLocalidad;
    private String domicilioFiscalBarrio;
    private String domicilioFiscalDireccion;
    private String telefono;
    private String codActividadEconomicaPrincipal;
    private String descActividadEconomicaPrincipal;
    private String listCodigoActividadEconomicaSecundaria;
    private String listDescripcionActividadEconomicaSecundaria;

    private Boolean activo;

    @CreationTimestamp
    private LocalDateTime creadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "timbrado_detalle", schema = "financiero")
public class TimbradoDetalle implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "timbrado_id", nullable = true)
    private Timbrado timbrado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "punto_de_venta_id", nullable = true)
    private PuntoDeVenta puntoDeVenta;

    private String puntoExpedicion;

    private String codigoEstablecimientoFactura;

    private Long cantidad;

    private Long rangoDesde;

    private Long rangoHasta;

    private Long numeroActual;

    // Campos para documento electrónico
    private String departamento;
    private String ciudad;
    private String codigoCiudad;
    private String localidad;
    private String barrio;
    private String direccion;
    private String telefono;

    private Boolean activo;


    @CreationTimestamp
    private LocalDateTime creadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
}

public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonView(JsonIdView.Id.class)
    private Long id;

    private nombre;

    private razonSocial;

    private Boolean tributa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "tipo_contribuyente")
    private Integer tipoContribuyente; //PF, PJ, EG (Entidad gubernamental)
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "factura_legal", schema = "financiero")
public class FacturaLegal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Empresa empresa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "timbrado_detalle_id", nullable = true)
    private TimbradoDetalle timbradoDetalle;

    @Column(name = "numeroFactura")
    private Integer numeroFactura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    private LocalDateTime fecha;
    private Boolean credito;
    private String nombre;
    private String ruc;
    private String direccion;

    @Column(name = "iva_parcial_0")
    private Double ivaParcial0;
    @Column(name = "iva_parcial_5")
    private Double ivaParcial5;
    @Column(name = "iva_parcial_10")
    private Double ivaParcial10;
    @Column(name = "total_parcial_0")
    private Double totalParcial0;
    @Column(name = "total_parcial_5")
    private Double totalParcial5;
    @Column(name = "total_parcial_10")
    private Double totalParcial10;

    private Double descuentoFinal;
    private Double totalParcial;
    private Double totalFinal;

    private Boolean activo;

    @CreationTimestamp
    private LocalDateTime creadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
}


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "factura_legal_item", schema = "financiero")
public class FacturaLegalItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_legal_id", nullable = true)

    private Producto producto;
    private FacturaLegal facturaLegal;
    private Float cantidad;
    private String descripcion;
    private Double precioUnitario;
    private Double total;


    @CreationTimestamp
    private LocalDateTime creadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(
        name = "estado_de_enum",
        typeClass = PostgreSQLEnumType.class
)
@Entity
@Table(name = "documento_electronico", schema = "financiero")
public class DocumentoElectronico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sucursalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_legal_id", nullable = false, unique = true)
    private FacturaLegal facturaLegal;

    // Información del documento electrónico
    private String cdc;
    private String urlQr;
    @Column(columnDefinition = "TEXT")
    private String xmlFirmado;
    @Column(columnDefinition = "TEXT")
    private String xmlOriginal;
    
    // Estado del documento
    @Enumerated(EnumType.STRING)
    @Type(type = "estado_de_enum")
    @Column(columnDefinition = "financiero.estado_de_enum")
    private EstadoDE estado;
    private String codigoRespuestaSifen;
    private String mensajeRespuestaSifen;
    
    // Información adicional
    private String numeroDocumento;
    private String tipoDocumento;
    private LocalDateTime fechaEmision;
    private LocalDateTime fechaRecepcionSifen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_de_id")
    private LoteDE loteDe;
    
    // Campos de auditoría
    private Boolean activo;

    @CreationTimestamp
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lote_de", schema = "financiero")
public class LoteDE implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EstadoLoteDE estado;

    private LocalDateTime fechaProcesado;
    private LocalDateTime fechaUltimoIntento;
    private Integer intentos;

    @Column(columnDefinition = "TEXT")
    private String respuestaSifen;
    private String protocolo;

    @OneToMany(mappedBy = "loteDe", fetch = FetchType.LAZY)
    private List<DocumentoElectronico> documentosElectronicos;

    @CreationTimestamp
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
}

/**
 * Entidad que representa un evento de cancelación de un Documento Electrónico en SIFEN.
 * 
 * Almacena tanto la solicitud de cancelación como la respuesta de SIFEN.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(
        name = "estado_evento_enum",
        typeClass = PostgreSQLEnumType.class
)
@Entity
@Table(name = "evento_cancelacion_de", schema = "financiero")
public class EventoCancelacionDE implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el documento electrónico cancelado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_electronico_id", nullable = false)
    private DocumentoElectronico documentoElectronico;

    // Datos del evento de cancelación enviado
    private String eventoId;           // ID único del evento
    private LocalDateTime fechaFirma;   // Fecha de firma del evento
    private String cdcDocumento;        // CDC del documento que se está cancelando
    private String motivoCancelacion;   // Motivo de la cancelación
    
    @Column(columnDefinition = "TEXT")
    private String xmlEvento;           // XML del evento enviado

    // Respuesta de SIFEN
    @Enumerated(EnumType.STRING)
    @Type(type = "estado_evento_enum")
    @Column(columnDefinition = "financiero.estado_evento_enum")
    private EstadoEvento estado;        // PENDIENTE, APROBADO, RECHAZADO
    
    private LocalDateTime fechaProcesamiento; // Fecha de procesamiento por SIFEN
    private String protocoloAutorizacion;     // Protocolo de autorización de SIFEN
    private String codigoRespuesta;           // Código de respuesta (ej: 0600)
    private String mensajeRespuesta;          // Mensaje de respuesta
    
    @Column(columnDefinition = "TEXT")
    private String respuestaBruta;      // Respuesta completa de SIFEN (XML)

    // Auditoría
    private Boolean activo;

    @CreationTimestamp
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@TypeDef(
        name = "tipo_conservacion",
        typeClass = PostgreSQLEnumType.class
)
@Table(name = "producto", schema = "productos")
public class Producto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    private String descripcion;
    private Integer iva;
    private Boolean balanza;
    private Boolean activo;
    private Double precio;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = true)
    private Usuario usuario;
}


Necesitamos crear un sistema de tracking de modificaciones, a cada accion crud que un usuario realiza, se pueda generar un historico que puede ser consultado posteriormente.

Estas entidades fueron retiradas de otro proyecto mas grande y posee campos que no seran necesarios para este proyecto.
En estos ejemplos hay el concepto de sucursales que hacia sentido en otro proyecto, en este proyecto podemos manejar las distintas sucursales como timbrados detalles.

tambien necesito que adiciones roles a los usuarios

En la primera etapa de desarrollo no vamos a conectar aun con el servidor de sifen, una vez que todo este funcionando correctamente, ahi sera el momento de conectar al servidor sifen.
La libreria que vamos a usar proviene de github packages:
<dependency>
  <groupId>io.github.gabfrank</groupId>
  <artifactId>jsifenlib</artifactId>
  <version>0.2.4-frc.13</version>
</dependency>

necesitamos agregar a github como repositorio, el token es: <REDACTADO-PAT-GITHUB>

