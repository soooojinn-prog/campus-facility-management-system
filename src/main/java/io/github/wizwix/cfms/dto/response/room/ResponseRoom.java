package io.github.wizwix.cfms.dto.response.room;

import io.github.wizwix.cfms.model.enums.RoomType;

/// 호실 정보
///
/// @param id           [Long] 방 ID (순수 인덱싱용)
/// @param buildingName [String] 건물 이름
/// @param name         [String] 방 번호 (e.g. '104호')
/// @param floor        [Integer] 층
/// @param type         [RoomType] 방 유형
/// @param capacity     [Integer] 수용 인원
public record ResponseRoom(Long id, String buildingName, String name, Integer floor, RoomType type, Integer capacity) {}
