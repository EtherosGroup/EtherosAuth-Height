package cn.skilfully.etheros.config.entity;

import lombok.Data;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@Data
@ConfigSerializable
public class PluginConfig {

    @Setting
    private Server server;
    @Setting
    private Authentication authentication;

    @Data
    @ConfigSerializable
    public static class Server {
        @Setting
        private String id;
        @Setting
        private String name;
        @Setting("language")
        private List<String> languages;
    }

    @Data
    @ConfigSerializable
    public static class Authentication {
        @Setting
        private boolean enabled;
        @Setting
        private String type;
        @Setting("login-timeout")
        private int loginTimeout = 120;
        @Setting
        private Action action;
        @Setting
        private Local local;
        @Setting
        private Official official;
        @Setting
        private Custom custom;
        @Setting
        private Register register;

        @Data
        @ConfigSerializable
        public static class Action {
            @Setting("locate")
            private Locate locate;

            @Data
            @ConfigSerializable
            public static class Locate {
                private String world;
                @Setting
                private Double x;
                @Setting
                private Double y;
                @Setting
                private Double z;
                @Setting
                private Float yaw;
                @Setting
                private Float pitch;
                @Setting("auto-return")
                private Boolean autoReturn;
            }
        }

        @Data
        @ConfigSerializable
        public static class Local {

            @Setting
            private Database database;

            @Data
            @ConfigSerializable
            public static class Database {
                @Setting
                private String type;
                @Setting("MySQL")
                private MySQL mysql;
                @Setting("MariaDB")
                private MariaDB mariaDB;
                @Setting("PostgreSQL")
                private PostgreSQL postgreSQL;
                @Setting("SQLite")
                private SQLite sqlite;

                @Data
                @ConfigSerializable
                public static class MySQL {
                    @Setting
                    private String address;
                    @Setting
                    private String username;
                    @Setting
                    private String password;
                    @Setting
                    private boolean ssl;
                    @Setting
                    private String database;
                }

                @Data
                @ConfigSerializable
                public static class MariaDB {
                    @Setting
                    private String address;
                    @Setting
                    private String username;
                    @Setting
                    private String password;
                    @Setting
                    private boolean ssl;
                    @Setting
                    private String database;
                }

                @Data
                @ConfigSerializable
                public static class PostgreSQL {
                    @Setting
                    private String address;
                    @Setting
                    private String username;
                    @Setting
                    private String password;
                    @Setting
                    private boolean ssl;
                    @Setting
                    private String database;
                }

                @Data
                @ConfigSerializable
                public static class SQLite {
                    @Setting
                    private String address;
                }
            }

        }

        @Data
        @ConfigSerializable
        public static class Official {
            @Setting
            private String identity;
            @Setting("authentication-source")
            private AuthenticationSource authenticationSource;

            @Data
            @ConfigSerializable
            public static class AuthenticationSource {
                @Setting
                private String address;
                @Setting
                private String username;
                @Setting
                private String password;
            }
        }

        @Data
        @ConfigSerializable
        public static class Custom {
            @Setting
            private String type;
            @Setting
            private String extension;
            @Setting
            private String script;
        }

        @Data
        @ConfigSerializable
        public static class Register {

            @Setting
            private Boolean enabled;

            @Setting
            private Restrictions restrictions;

            @Data
            @ConfigSerializable
            public static class Restrictions {
                @Setting
                private Regular regular;

                @Data
                @ConfigSerializable
                public static class Regular {
                    @Setting
                    private String name;
                    @Setting
                    private String password;
                }
            }
        }

    }

}
