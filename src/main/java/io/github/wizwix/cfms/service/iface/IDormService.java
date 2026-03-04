package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.request.dorm.RequestDormApply;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormApplyResult;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormFloor;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormMyApplication;
import io.github.wizwix.cfms.model.enums.DormApplicationStatus;
import io.github.wizwix.cfms.model.enums.Gender;

import java.util.List;

public interface IDormService {
  ResponseDormApplyResult apply(String userNumber, RequestDormApply request);

  void cancelApplication(String number, Long applicationId);

  List<ResponseDormFloor> getDormRooms(Gender gender);

  List<ResponseDormMyApplication> getMyApplications(String userNumber);

  List<ResponseDormMyApplication> getDormApplicationsByStatus(DormApplicationStatus status);

  void updateDormApplicationStatus(Long id, DormApplicationStatus status, String rejectReason, String adminNumber);
}
