package io.github.wizwix.cfms.model.cafeteria;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.github.wizwix.cfms.model.enums.CafeteriaMealType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cfms_cafeteria_meal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CafeteriaMeal {
  /// 끼니 ID
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 날짜
  private LocalDate date;
  /// 아이콘 (e.g. '🌅')
  private String icon;
  /// 메뉴
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "meal", orphanRemoval = true)
  @JsonManagedReference
  private List<CafeteriaMealItem> items;
  /// 끼니 유형 (BREAKFAST/LUNCH/DINNER)
  @Enumerated(EnumType.STRING)
  private CafeteriaMealType mealType;
  /// 시간대 (e.g. '08:00 ~ 09:30')
  private String time;
}
