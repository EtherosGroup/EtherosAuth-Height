package cn.skilfully.etheros;

import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.database.HibernateUtil;
import cn.skilfully.etheros.database.entity.PlayerAccountEntity;
import cn.skilfully.etheros.database.entity.PlayerLocationEntity;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.service.ServerService;
import cn.skilfully.etheros.utils.Messenger;

@Service
public class Initializer {

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private ServerService serverService;

    @Autowired
    private HibernateUtil hibernateUtil;

    @PostConstruct
    public void init() {
        try {
            hibernateUtil.registerEntityClass(PlayerAccountEntity.class);
            hibernateUtil.registerEntityClass(PlayerLocationEntity.class);
            hibernateUtil.getSessionFactory();
            serverService.setLoading(false);
        } catch (Exception e) {
            Messenger.consoleError(e.getMessage());
            EtherosAuthPaper.disable();
        }
    }

}
