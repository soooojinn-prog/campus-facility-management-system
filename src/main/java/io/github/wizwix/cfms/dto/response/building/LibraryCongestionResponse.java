package io.github.wizwix.cfms.dto.response.building;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class LibraryCongestionResponse {
  private int overallRate;
  /** 층별 혼잡도 [{name, rate, capacity}] */
  private List<Map<String, Object>> floors;
  /** 시간대별 추이 [{hour, rate}] */
  private List<Map<String, Object>> hourlyTrend;
}
