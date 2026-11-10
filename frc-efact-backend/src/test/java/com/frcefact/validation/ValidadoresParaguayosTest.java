package com.frcefact.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests básicos para validadores paraguayos.
 */
class ValidadoresParaguayosTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    static class TestRucDto {
        @ValidRuc
        private String ruc;

        public TestRucDto(String ruc) {
            this.ruc = ruc;
        }
    }

    static class TestTimbradoDto {
        @ValidTimbrado
        private String numero;

        public TestTimbradoDto(String numero) {
            this.numero = numero;
        }
    }

    static class TestCdcDto {
        @ValidCdc
        private String cdc;

        public TestCdcDto(String cdc) {
            this.cdc = cdc;
        }
    }

    static class TestIvaDto {
        @ValidIva
        private Integer iva;

        public TestIvaDto(Integer iva) {
            this.iva = iva;
        }
    }

    @Test
    void testRucValido() {
        // 80016875-5 es el DV correcto (módulo 11). La versión original de este test usaba
        // "80016875-1" creyendo que era el válido — estaban invertidos.
        TestRucDto dto = new TestRucDto("80016875-5");
        Set<ConstraintViolation<TestRucDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "RUC válido no debe tener violaciones");
    }

    @Test
    void testRucValidoConRucRealDeProduccion() {
        // Emisor con DE aprobados por SIFEN
        TestRucDto dto = new TestRucDto("80099482-5");
        Set<ConstraintViolation<TestRucDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Un RUC real de producción no debe tener violaciones");
    }

    @Test
    void testRucInvalido() {
        // 80016875-1 tiene el DV equivocado: el correcto es 5.
        TestRucDto dto = new TestRucDto("80016875-1");
        Set<ConstraintViolation<TestRucDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "RUC con dígito verificador incorrecto debe tener violaciones");
    }

    @Test
    void testTimbradoValido() {
        TestTimbradoDto dto = new TestTimbradoDto("12345678");
        Set<ConstraintViolation<TestTimbradoDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Timbrado de 8 dígitos debe ser válido");
    }

    @Test
    void testTimbradoInvalido() {
        TestTimbradoDto dto = new TestTimbradoDto("123456");
        Set<ConstraintViolation<TestTimbradoDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Timbrado con menos de 8 dígitos debe ser inválido");
    }

    @Test
    void testCdcValido() {
        TestCdcDto dto = new TestCdcDto("12345678901234567890123456789012345678901234");
        Set<ConstraintViolation<TestCdcDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "CDC de 44 dígitos debe ser válido");
    }

    @Test
    void testCdcInvalido() {
        TestCdcDto dto = new TestCdcDto("123456789012345678901234567890");
        Set<ConstraintViolation<TestCdcDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "CDC con menos de 44 caracteres debe ser inválido");
    }

    @Test
    void testIvaValido() {
        TestIvaDto dto0 = new TestIvaDto(0);
        TestIvaDto dto5 = new TestIvaDto(5);
        TestIvaDto dto10 = new TestIvaDto(10);
        
        assertTrue(validator.validate(dto0).isEmpty(), "IVA 0 debe ser válido");
        assertTrue(validator.validate(dto5).isEmpty(), "IVA 5 debe ser válido");
        assertTrue(validator.validate(dto10).isEmpty(), "IVA 10 debe ser válido");
    }

    @Test
    void testIvaInvalido() {
        TestIvaDto dto = new TestIvaDto(15);
        Set<ConstraintViolation<TestIvaDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "IVA 15 debe ser inválido");
    }
}
