package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.response.building.ResponseBuilding;
import io.github.wizwix.cfms.dto.response.room.ResponseRoom;
import io.github.wizwix.cfms.repo.BuildingRepository;
import io.github.wizwix.cfms.repo.RoomRepository;
import io.github.wizwix.cfms.service.iface.IBuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BuildingService implements IBuildingService {
  private final BuildingRepository buildingRepository;
  private final RoomRepository roomRepository;

  @Override
  public List<ResponseRoom> getRoomsBySlug(String slug) {
    List<ResponseRoom> list = new ArrayList<>();
    roomRepository.getRoomsByBuildingSlug(slug).forEach(r -> {
      var resp = new ResponseRoom(r.getId(), r.getBuilding().getName(), r.getName(), r.getFloor(), r.getType(), r.getCapacity());
      list.add(resp);
    });
    return list;
  }

  public List<ResponseBuilding> listAll() {
    var list = new ArrayList<ResponseBuilding>();
    buildingRepository.findAll().forEach(b -> {
      var resp = new ResponseBuilding(b.getId(), b.getName(), b.getSlug(), b.getInfo(), b.getPoints(), b.getType(), b.getRentable());
      list.add(resp);
    });
    return list;
  }
}
