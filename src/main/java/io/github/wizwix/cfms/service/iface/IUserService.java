package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.request.auth.RequestLogin;
import io.github.wizwix.cfms.dto.request.auth.RequestRegister;
import io.github.wizwix.cfms.dto.response.auth.ResponseLogin;

public interface IUserService {
  ResponseLogin login(RequestLogin login);

  void register(RequestRegister request);
}
