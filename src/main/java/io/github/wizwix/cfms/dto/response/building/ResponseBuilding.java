package io.github.wizwix.cfms.dto.response.building;

import io.github.wizwix.cfms.model.enums.BuildingType;

/// 건물 정보
///
/// @param id       [Long] 건물 ID (내부 인덱싱용)
/// @param name     [String] 건물 이름 (e.g. 'IT대학동')
/// @param type     [BuildingType] 건물 유형
/// @param rentable [Boolean] 대여 가능 여부
public record ResponseBuilding(Long id, String name, BuildingType type, Boolean rentable) {}
