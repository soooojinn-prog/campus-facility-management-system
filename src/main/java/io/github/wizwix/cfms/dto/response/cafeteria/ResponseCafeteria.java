package io.github.wizwix.cfms.dto.response.cafeteria;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/// 식당 메뉴 정보
///
/// @param date           [LocalDate] 날짜
/// @param menuByCategory [Map] 카테고리별 식단 (중식, 석식 등)
public record ResponseCafeteria(LocalDate date, Map<String, List<String>> menuByCategory) {}
