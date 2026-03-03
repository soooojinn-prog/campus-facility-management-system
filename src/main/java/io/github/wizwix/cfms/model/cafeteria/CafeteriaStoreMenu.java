package io.github.wizwix.cfms.model.cafeteria;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cfms_cafeteria_store_menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CafeteriaStoreMenu {
  /// 메뉴 ID
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 메뉴명
  private String name;
  /// 할인 사유 (e.g. '학생 할인', nullable)
  private String discountLabel;
  /// 할인가 (nullable)
  private Integer discountPrice;
  /// 인기 메뉴 여부
  private Boolean popular;
  /// 정가 (원)
  private Integer price;
  /// 소속 가게
  @ManyToOne(fetch = FetchType.LAZY)
  @JsonBackReference
  private CafeteriaStore store;
}
