package io.github.wizwix.cfms.dto.response.room;

/// 호실 정보
///
/// @param id           [Long] 호실 ID (순수 인덱싱용)
/// @param buildingName [String] 건물 이름
/// @param roomCode     [String] 방 정식 명칭 (e.g. 'IT대학동 104호')
/// @param roomNumber   [String] 방 번호 (e.g. '104')
public record ResponseRoom(Long id, String buildingName, String roomCode, String roomNumber) {}
