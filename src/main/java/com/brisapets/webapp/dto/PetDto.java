package com.brisapets.webapp.dto;

public record PetDto(
    Long id,
    String nome,
    String especie,
    String raca,
    Integer idade,
    Double weightKg,
    String observacoes,
    Long ownerId,
    String ownerName
) {}