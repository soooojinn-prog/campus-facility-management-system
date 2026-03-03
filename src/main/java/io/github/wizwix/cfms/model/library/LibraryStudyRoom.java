package io.github.wizwix.cfms.model.library;

import io.github.wizwix.cfms.model.enums.LibraryFloor;
import io.github.wizwix.cfms.model.enums.LibraryStudyRoomStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "cfms_library_study_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryStudyRoom {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "cfms_library_study_room_amenities", joinColumns = @JoinColumn(name = "study_room_id"))
  @Column(name = "amenity")
  private List<String> amenities;
  private Integer capacity;
  @Enumerated(EnumType.STRING)
  private LibraryFloor floor;
  @Enumerated(EnumType.STRING)
  private LibraryStudyRoomStatus status;
}
