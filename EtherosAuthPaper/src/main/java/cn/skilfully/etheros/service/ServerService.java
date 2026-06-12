package cn.skilfully.etheros.service;

import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import lombok.Getter;
import lombok.Setter;

@Service
public class ServerService {

    @Getter
    @Setter
    private boolean isLoading = true;

    @Getter
    @Setter
    private boolean isUpdating = false;

}
