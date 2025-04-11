package com.nisimsoft.auth_system.services;

import com.nisimsoft.auth_system.services.providers.AuthenticationProvider;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AuthProviderFactory {
  private final Map<String, AuthenticationProvider> providers;

  public AuthProviderFactory(List<AuthenticationProvider> providerList) {
    // Mapea cada proveedor por su nombre (clave: nombre del proveedor, valor:
    // instancia)
    providers =
        providerList.stream()
            .collect(
                Collectors.toMap(AuthenticationProvider::getProviderName, Function.identity()));
  }

  public AuthenticationProvider getProvider(String providerName) {
    return providers.get(providerName);
  }
}
