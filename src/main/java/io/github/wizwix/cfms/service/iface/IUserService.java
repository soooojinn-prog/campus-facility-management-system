package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.request.auth.RequestLogin;
import io.github.wizwix.cfms.dto.request.auth.RequestPasswordResetConfirm;
import io.github.wizwix.cfms.dto.request.auth.RequestPasswordResetRequest;
import io.github.wizwix.cfms.dto.request.auth.RequestPasswordResetVerify;
import io.github.wizwix.cfms.dto.request.auth.RequestRegister;
import io.github.wizwix.cfms.dto.response.auth.ResponseLogin;

public interface IUserService {
  void confirmPasswordReset(RequestPasswordResetConfirm request);

  ResponseLogin login(RequestLogin login);

  void register(RequestRegister request);

  void requestPasswordReset(RequestPasswordResetRequest request);

  void verifyPasswordReset(RequestPasswordResetVerify request);
}
