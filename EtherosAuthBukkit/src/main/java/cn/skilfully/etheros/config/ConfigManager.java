package cn.skilfully.etheros.config;

import cn.skilfully.etheros.EtherosAuthBukkit;
import cn.skilfully.etheros.config.entity.LanguageConfig;
import cn.skilfully.etheros.config.entity.PluginConfig;
import cn.skilfully.etheros.etherosframework.di.annotation.PostConstruct;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.etherosframework.di.annotation.Value;
import cn.skilfully.etheros.etherosframework.utils.ConfigFileUtil;
import cn.skilfully.etheros.utils.Messenger;
import lombok.Getter;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class ConfigManager {

    @Value("plugin.name")
    private String PLUGIN_NAME;

    @Getter
    private PluginConfig pluginConfig = new PluginConfig();
    @Getter
    private LanguageConfig pluginLanguage = new LanguageConfig();
    private final Map<String, LanguageConfig> languagesConfig = new HashMap<>();
    @Getter
    private final Set<String> commandWhitelist = new HashSet<>();

    @PostConstruct
    private void load() {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize config", e);
        }
    }

    public void init() throws IOException {
        loadConfig();
        loadAllLanguages();
        loadCommandWhitelist();
        Messenger.consoleInfo(pluginLanguage.getLoaded());
        Messenger.setInGamePrefix(pluginLanguage.getPrefix());
    }

    public void loadConfig() throws IOException {
        File f = ConfigFileUtil.createWorkDirectory(PLUGIN_NAME);
        if (f == null || !f.exists()) {
            throw new RuntimeException("无法创建工作目录 plugins/Etheros/EtherosGroup");
        }
        File settingFile = ConfigFileUtil.extractFileFromJarResources(
                EtherosAuthBukkit.class,
                "setting.yml",
                new File(f, "setting.yml"),
                false
        );
        if (settingFile == null) {
            throw new RuntimeException("加载配置文件失败");
        }
        CommentedConfigurationNode settingNode = buildLoader(settingFile).load();
        pluginConfig = settingNode.get(PluginConfig.class);
    }

    public void loadAllLanguages() throws IOException {
        if (pluginConfig == null) {
            throw new IllegalStateException("请先加载配置文件");
        }
        File workDirectory = ConfigFileUtil.createWorkDirectory(PLUGIN_NAME);
        if (workDirectory == null) {
            throw new RuntimeException("创建工作目录失败");
        }
        languagesConfig.clear();
        pluginLanguage = null;
        List<String> languages = pluginConfig.getServer().getLanguages();
        if (languages != null && !languages.isEmpty()) {
            for (String language : languages) {
                loadAndStoreLanguage(workDirectory, language);
            }

            File pluginLangFile = new File(
                    ConfigFileUtil.getWorkDirectory(PLUGIN_NAME),
                    "Languages/" + languages.get(0)
            );
            if (!pluginLangFile.exists()) {
                pluginLangFile = ConfigFileUtil.extractFileFromJarResources(
                        EtherosAuthBukkit.class,
                        "zh-CN.yml",
                        new File(workDirectory, "Languages/zh-CN.yml"),
                        false
                );
                if (pluginLangFile == null) {
                    throw new IllegalStateException("没有任何可用的语言文件");
                }
            }
            pluginLanguage = loadLanguage(pluginLangFile);
            Messenger.setInGamePrefix(pluginLanguage.getPrefix());
        } else {
            pluginLanguage = loadDefaultLanguage(workDirectory);
            Messenger.setInGamePrefix(pluginLanguage.getPrefix());
        }
    }

    public void loadCommandWhitelist() throws IOException {
        File workDirectory = ConfigFileUtil.createWorkDirectory(PLUGIN_NAME);
        if (workDirectory == null) {
            throw new RuntimeException("创建工作目录失败");
        }
        File whitelistFile = new File(workDirectory, "CommandWhitelist.txt");
        if (!whitelistFile.exists()) {
            whitelistFile = ConfigFileUtil.extractFileFromJarResources(
                    EtherosAuthBukkit.class,
                    "CommandWhitelist.txt",
                    whitelistFile,
                    false
            );
        }
        if (whitelistFile == null || !whitelistFile.exists()) {
            throw new RuntimeException("加载CommandWhitelist.txt失败");
        }
        List<String> lines = Files.readAllLines(whitelistFile.toPath());
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                commandWhitelist.add(trimmed);
            }
        }
    }

    public LanguageConfig getLanguage(String name) {
        return languagesConfig.get(name);
    }

    public Map<String, LanguageConfig> getLanguages() {
        return new HashMap<>(languagesConfig);
    }

    private void loadAndStoreLanguage(File workDirectory, String language) throws IOException {
        File langFile = ConfigFileUtil.extractFileFromJarResources(
                EtherosAuthBukkit.class,
                language,
                new File(workDirectory, "Languages/" + language),
                false
        );
        if (langFile == null) return;
        LanguageConfig langConfig = loadLanguage(langFile);
        Messenger.consoleInfo(langConfig.getLoadedAsLanguage());
        languagesConfig.put(language, langConfig);
    }

    private LanguageConfig loadDefaultLanguage(File workDirectory) throws IOException {
        File langFile = ConfigFileUtil.extractFileFromJarResources(
                EtherosAuthBukkit.class,
                "zh-CN.yml",
                new File(workDirectory, "Languages/zh-CN.yml"),
                false
        );
        if (langFile == null) {
            throw new IllegalStateException("没有任何可用的语言文件");
        }
        return loadLanguage(langFile);
    }


    private YamlConfigurationLoader buildLoader(File file) {
        return YamlConfigurationLoader.builder()
                .path(file.toPath())
                .build();
    }

    private LanguageConfig loadLanguage(File file) throws IOException {
        return buildLoader(file).load().get(LanguageConfig.class);
    }

}
