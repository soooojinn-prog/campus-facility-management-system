package io.github.wizwix.cfms.model.library;

import io.github.wizwix.cfms.model.enums.LibraryFloor;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cfms_library_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibraryReadingRoom {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  @Enumerated(EnumType.STRING)
  private LibraryFloor floor;
  private Integer totalSeats;
  private Integer usedRate;
}
