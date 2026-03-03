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
@Table(name = "cfms_cafeteria_meal_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CafeteriaMealItem {
  /// 메뉴 항목 ID
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 메뉴명
  private String name;
  /// 할인 사유 (nullable)
  private String discountLabel;
  /// 할인가 (nullable)
  private Integer discountPrice;
  /// 소속 끼니
  @ManyToOne(fetch = FetchType.LAZY)
  @JsonBackReference
  private CafeteriaMeal meal;
  /// 정가 (원)
  private Integer price;
}
