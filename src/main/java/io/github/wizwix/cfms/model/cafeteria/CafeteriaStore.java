package io.github.wizwix.cfms.model.cafeteria;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "cfms_cafeteria_store")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CafeteriaStore {
  /// 가게 ID
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 가게명 (e.g. '한솥도시락')
  private String name;
  /// 카테고리 (한식/분식/면류/치킨/돈까스/양식/백반/카페)
  private String category;
  /// 설명 (e.g. '든든한 한 끼, 다양한 도시락 메뉴')
  private String description;
  /// 운영시간 (e.g. '10:00 ~ 20:00')
  private String hours;
  /// 메뉴
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "store", orphanRemoval = true)
  @JsonManagedReference
  private List<CafeteriaStoreMenu> menus;
  /// 대표 메뉴명
  private String representative;
}
