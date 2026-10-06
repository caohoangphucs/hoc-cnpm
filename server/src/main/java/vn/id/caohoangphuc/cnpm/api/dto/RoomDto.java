package vn.id.caohoangphuc.cnpm.api.dto;

/** Room catalogue data, including persisted values needed by an administrator edit form. */
public record RoomDto(
    String id,
    String buildingId,
    String roomType,
    String roomTypeId,
    String gender,
    String number,
    Integer floor,
    Long basePrice,
    String status,
    Long capacity,
    Long availableBeds
) {}
