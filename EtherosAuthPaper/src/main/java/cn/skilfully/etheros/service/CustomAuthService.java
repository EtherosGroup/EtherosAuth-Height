package cn.skilfully.etheros.service;

import cn.skilfully.etheros.etherosframework.di.annotation.Service;

import java.util.UUID;

@Service
public class CustomAuthService {

    public boolean isBanned(UUID uuid) {
        return false;
    }

}
