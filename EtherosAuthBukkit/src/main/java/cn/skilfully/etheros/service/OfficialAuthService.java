package cn.skilfully.etheros.service;

import cn.skilfully.etheros.etherosframework.di.annotation.Service;

import java.util.UUID;

@Service
public class OfficialAuthService {

    public boolean isBanned(UUID uuid) {
        return false;
    }

    public boolean isGlobalBanned(UUID uuid) {
        return false;
    }

}
