package vn.id.caohoangphuc.cnpm.api.dto;

/** Bed catalogue data, including position for editing the stored bed configuration. */
public record BedDto(String id, String number, String position, String status, Long version) {}
