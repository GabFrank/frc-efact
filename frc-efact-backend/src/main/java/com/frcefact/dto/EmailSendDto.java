package com.frcefact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailSendDto {
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email es inválido")
    private String email;
    
    private boolean actualizarCliente;

    public EmailSendDto() {
    }

    public EmailSendDto(String email, boolean actualizarCliente) {
        this.email = email;
        this.actualizarCliente = actualizarCliente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActualizarCliente() {
        return actualizarCliente;
    }

    public void setActualizarCliente(boolean actualizarCliente) {
        this.actualizarCliente = actualizarCliente;
    }
}
