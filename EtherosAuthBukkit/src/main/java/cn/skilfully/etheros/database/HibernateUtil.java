package cn.skilfully.etheros.database;

import cn.skilfully.etheros.config.ConfigManager;
import cn.skilfully.etheros.config.entity.PluginConfig;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.PreDestroy;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import cn.skilfully.etheros.etherosframework.di.annotation.Value;
import cn.skilfully.etheros.etherosframework.utils.ConfigFileUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class HibernateUtil {

    @Value("plugin.name")
    private String PLUGIN_NAME;

    static {
        System.setProperty("org.jboss.logging.provider", "jdk");
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("com.zaxxer.hikari").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("org.hibernate.orm.jdbc.bind").setLevel(java.util.logging.Level.OFF);
        try {
            Class<?> configurator = Class.forName("org.apache.logging.log4j.core.config.Configurator");
            Class<?> levelClass = Class.forName("org.apache.logging.log4j.Level");
            Object off = levelClass.getField("OFF").get(null);
            configurator.getMethod("setLevel", String.class, levelClass)
                    .invoke(null, "com.zaxxer.hikari", off);
        } catch (Throwable ignored) {
        }
    }

    @Autowired
    private ConfigManager configManager;

    private SessionFactory sessionFactory;
    private final List<Class<?>> entityClasses = new ArrayList<>();

    public void registerEntityClass(Class<?> clazz) {
        if (sessionFactory != null) {
            throw new IllegalStateException("SessionFactory已构建，无法注册新的实体类");
        }
        entityClasses.add(clazz);
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    private SessionFactory buildSessionFactory() {
        PluginConfig config = configManager.getPluginConfig();
        if (config == null || config.getAuthentication() == null || config.getAuthentication().getLocal() == null) {
            throw new IllegalStateException("认证配置未正确加载");
        }
        PluginConfig.Authentication.Local.Database dbConfig = config.getAuthentication().getLocal().getDatabase();
        if (dbConfig == null) {
            throw new IllegalStateException("数据库配置未正确加载");
        }
        String type = dbConfig.getType().toLowerCase();

        Configuration configuration = new Configuration();
        Properties props = new Properties();

        props.put(AvailableSettings.HBM2DDL_AUTO, "update");
        props.put(AvailableSettings.SHOW_SQL, false);
        props.put(AvailableSettings.FORMAT_SQL, false);

        switch (type) {
            case "sqlite" -> configureSQLite(props, dbConfig);
            case "mysql" -> configureMySQL(props, dbConfig);
            case "mariadb" -> configureMariaDB(props, dbConfig);
            case "postgresql" -> configurePostgreSQL(props, dbConfig);
            default -> throw new IllegalArgumentException("不支持的数据库类型: " + type);
        }

        props.put("hibernate.hikari.connectionTimeout", "30000");
        props.put("hibernate.hikari.maximumPoolSize", "10");
        props.put("hibernate.hikari.minimumIdle", "2");

        configuration.setProperties(props);
        for (Class<?> clazz : entityClasses) {
            configuration.addAnnotatedClass(clazz);
        }
        SessionFactory sf = configuration.buildSessionFactory();
        executeInitScript(sf, type);
        return sf;
    }

    private void configureSQLite(Properties props, PluginConfig.Authentication.Local.Database dbConfig) {
        String path = dbConfig.getSqlite().getAddress();
        File workDir = ConfigFileUtil.createWorkDirectory(PLUGIN_NAME);
        if (workDir != null && !new File(path).isAbsolute()) {
            path = new File(workDir, path).getAbsolutePath();
        }
        props.put(AvailableSettings.DIALECT, "org.hibernate.community.dialect.SQLiteDialect");
        props.put(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.sqlite.JDBC");
        props.put(AvailableSettings.JAKARTA_JDBC_URL, "jdbc:sqlite:" + path);
    }

    private void configureMySQL(Properties props, PluginConfig.Authentication.Local.Database dbConfig) {
        var mysql = dbConfig.getMysql();
        props.put(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQLDialect");
        props.put(AvailableSettings.JAKARTA_JDBC_DRIVER, "com.mysql.cj.jdbc.Driver");
        props.put(AvailableSettings.JAKARTA_JDBC_URL, buildMySQLUrl(mysql.getAddress(), mysql.getDatabase(), mysql.isSsl()));
        props.put(AvailableSettings.JAKARTA_JDBC_USER, mysql.getUsername());
        props.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, mysql.getPassword());
    }

    private void configureMariaDB(Properties props, PluginConfig.Authentication.Local.Database dbConfig) {
        var mariaDB = dbConfig.getMariaDB();
        props.put(AvailableSettings.DIALECT, "org.hibernate.dialect.MariaDBDialect");
        props.put(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.mariadb.jdbc.Driver");
        props.put(AvailableSettings.JAKARTA_JDBC_URL, buildMariaDBUrl(mariaDB.getAddress(), mariaDB.getDatabase(), mariaDB.isSsl()));
        props.put(AvailableSettings.JAKARTA_JDBC_USER, mariaDB.getUsername());
        props.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, mariaDB.getPassword());
    }

    private void configurePostgreSQL(Properties props, PluginConfig.Authentication.Local.Database dbConfig) {
        var postgreSQL = dbConfig.getPostgreSQL();
        props.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        props.put(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.postgresql.Driver");
        props.put(AvailableSettings.JAKARTA_JDBC_URL, buildPostgreSQLUrl(postgreSQL.getAddress(), postgreSQL.getDatabase(), postgreSQL.isSsl()));
        props.put(AvailableSettings.JAKARTA_JDBC_USER, postgreSQL.getUsername());
        props.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, postgreSQL.getPassword());
    }

    private String buildMySQLUrl(String address, String database, boolean ssl) {
        return "jdbc:mysql://" + address + "/" + database + "?useSSL=" + ssl + "&allowPublicKeyRetrieval=true";
    }

    private String buildMariaDBUrl(String address, String database, boolean ssl) {
        return "jdbc:mariadb://" + address + "/" + database + "?useSSL=" + ssl;
    }

    private String buildPostgreSQLUrl(String address, String database, boolean ssl) {
        return "jdbc:postgresql://" + address + "/" + database + "?ssl=" + ssl;
    }

    private void executeInitScript(SessionFactory sf, String type) {
        String resourcePath = "db/" + type + ".sql";
        String sql = loadSqlResource(resourcePath);
        if (sql == null || sql.isBlank()) {
            return;
        }
        try (Session session = sf.openSession()) {
            session.doWork(connection -> {
                try (Statement stmt = connection.createStatement()) {
                    for (String statement : sql.split(";")) {
                        String trimmed = statement.trim();
                        if (!trimmed.isEmpty()) {
                            stmt.execute(trimmed);
                        }
                    }
                }
            });
        }
    }

    private String loadSqlResource(String resourcePath) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    @PreDestroy
    private void destroy() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
