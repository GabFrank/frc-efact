package com.frcefact.dto;

/**
 * DTO para solicitud de búsqueda de usuarios.
 * Utilizado para filtrar y buscar usuarios en el sistema.
 */
public class UserSearchRequest {

    private String searchTerm;
    private Boolean isActive;
    private String role;
    private Boolean isLocked;
    private int page = 0;
    private int size = 20;
    private String sortBy = "username";
    private String sortDirection = "ASC";

    // Constructores
    public UserSearchRequest() {
    }

    public UserSearchRequest(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    // Getters y Setters
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public String toString() {
        return "UserSearchRequest{" +
                "searchTerm='" + searchTerm + '\'' +
                ", isActive=" + isActive +
                ", role='" + role + '\'' +
                ", isLocked=" + isLocked +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}