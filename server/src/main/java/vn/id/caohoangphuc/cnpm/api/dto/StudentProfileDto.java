package vn.id.caohoangphuc.cnpm.api.dto;

import java.time.LocalDate;

/** Non-secret personal and dormitory details for the authenticated student. */
public record StudentProfileDto(
        String studentId,
        String name,
        LocalDate dateOfBirth,
        String gender,
        String email,
        String phone,
        String className,
        String faculty,
        String hometown,
        String roomId,
        String status) {
}
