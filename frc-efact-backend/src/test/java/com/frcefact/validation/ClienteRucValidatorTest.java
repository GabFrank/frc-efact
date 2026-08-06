package com.frcefact.validation;

import com.frcefact.dto.ClienteDto;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test de la regla de negocio del RUC según el tipo de cliente.
 *
 * <p>La validación del RUC son <b>dos capas</b>:
 * <ul>
 *   <li>{@code @ValidRuc} sobre el campo — formato y, si hay guion, corrección del DV.</li>
 *   <li>{@code @ValidClienteRuc} sobre el DTO — decide si el guion es obligatorio o prohibido,
 *       según {@code tipoClienteSifen}.</li>
 * </ul>
 *
 * <p>La regla: <b>contribuyente ⇒ {@code numero-DV} obligatorio</b>; no contribuyente ⇒ solo
 * números, sin guion. Este archivo cubre esa segunda capa, que no tenía tests.
 */
class ClienteRucValidatorTest {

    private ClienteRucValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new ClienteRucValidator();
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }

    private ClienteDto cliente(String tipoClienteSifen, String ruc) {
        ClienteDto dto = new ClienteDto();
        dto.setTipoClienteSifen(tipoClienteSifen);
        dto.setRuc(ruc);
        return dto;
    }

    @Test
    @DisplayName("Contribuyente con numero-DV: válido")
    void contribuyenteConDv() {
        assertTrue(validator.isValid(cliente("PERSONA_FISICA", "80099482-5"), context));
    }

    @Test
    @DisplayName("Contribuyente sin DV: rechazado — el guion es obligatorio")
    void contribuyenteSinDv() {
        assertFalse(validator.isValid(cliente("PERSONA_FISICA", "80099482"), context),
                "Un contribuyente debe declarar el RUC como numero-DV");
    }

    @Test
    @DisplayName("Contribuyente sin RUC: rechazado")
    void contribuyenteSinRuc() {
        assertFalse(validator.isValid(cliente("PERSONA_FISICA", null), context));
        assertFalse(validator.isValid(cliente("PERSONA_FISICA", "   "), context));
    }

    @Test
    @DisplayName("No contribuyente con solo números: válido")
    void noContribuyenteSinDv() {
        assertTrue(validator.isValid(cliente("NO_CONTRIBUYENTE", "1234567"), context));
    }

    @Test
    @DisplayName("No contribuyente sin RUC: válido — el RUC es opcional")
    void noContribuyenteSinRuc() {
        assertTrue(validator.isValid(cliente("NO_CONTRIBUYENTE", null), context));
    }

    @Test
    @DisplayName("No contribuyente con formato de contribuyente: rechazado")
    void noContribuyenteConDv() {
        assertFalse(validator.isValid(cliente("NO_CONTRIBUYENTE", "80099482-5"), context),
                "Un no contribuyente no debe declarar DV");
    }

    @Test
    @DisplayName("GUBERNAMENTAL también es contribuyente: exige DV")
    void gubernamentalExigeDv() {
        assertTrue(validator.isValid(cliente("GUBERNAMENTAL", "80099482-5"), context));
        assertFalse(validator.isValid(cliente("GUBERNAMENTAL", "80099482"), context));
    }

    @Test
    @DisplayName("EXTRANJERO no es contribuyente: no exige DV")
    void extranjeroNoExigeDv() {
        assertTrue(validator.isValid(cliente("EXTRANJERO", null), context));
        assertTrue(validator.isValid(cliente("EXTRANJERO", "1234567"), context));
    }

    @Test
    @DisplayName("DTO null: válido — lo maneja @NotNull")
    void dtoNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("tipoClienteSifen desconocido: cae al campo legacy `tributa`")
    void fallbackATributa() {
        ClienteDto contribuyente = cliente("VALOR_QUE_NO_EXISTE", "80099482");
        contribuyente.setTributa(true);
        assertFalse(validator.isValid(contribuyente, context),
                "Con tributa=true debe exigir DV igual que un contribuyente");

        ClienteDto noContribuyente = cliente("VALOR_QUE_NO_EXISTE", "1234567");
        noContribuyente.setTributa(false);
        assertTrue(validator.isValid(noContribuyente, context));
    }
}
