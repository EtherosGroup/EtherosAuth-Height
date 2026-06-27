package cn.skilfully.etheros.config.entity;

import lombok.Data;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Data
@ConfigSerializable
public class LanguageConfig {

    @Setting
    private String prefix;
    @Setting("loaded-as-language")
    private String loadedAsLanguage;
    @Setting("reject-message")
    private RejectMessage rejectMessage;
    @Setting
    private Login login;
    @Setting
    private String loaded;
    @Setting
    private Error error;
    @Setting
    private Failed failed;
    @Setting
    private Command command;

    @Data
    @ConfigSerializable
    public static class RejectMessage {
        @Setting
        private String loading;
        @Setting
        private String loggedIn;
        @Setting
        private String updating;
        @Setting("remote-logout")
        private String remoteLogout;
        @Setting("remote-rejected")
        private String remoteRejected;
        @Setting("banned-local")
        private String bannedLocal;
        @Setting("banned-global")
        private String bannedGlobal;
        @Setting("internal-error")
        private String internalError;
        @Setting("not-logged-in")
        private String notLoggedIn;
        @Setting("login-timeout")
        private String loginTimeout;
    }

    @Data
    @ConfigSerializable
    public static class Login {
        @Setting
        private Local local;
        @Setting
        private Official official;
        @Setting
        private Custom custom;

        @Data
        @ConfigSerializable
        public static class Local {
            @Setting
            private String title;
            @Setting
            private String subtitle;
            @Setting
            private String message;
        }

        @Data
        @ConfigSerializable
        public static class Official {
            @Setting
            private String title;
            @Setting
            private String subtitle;
            @Setting
            private String message;
        }

        @Data
        @ConfigSerializable
        public static class Custom {
            @Setting
            private String title;
            @Setting
            private String subtitle;
            @Setting
            private String message;
        }
    }

    @Data
    @ConfigSerializable
    public static class Error {
        @Setting("unknown-auth-type")
        private String unknownAuthType;
        @Setting("register-command")
        private String registerCommand;
        @Setting("unknown-world")
        private String unknownWorld;

    }

    @Data
    @ConfigSerializable
    public static class Failed {
        @Setting
        private String reload;
        @Setting
        private String tp;
    }

    @Data
    @ConfigSerializable
    public static class Command {
        @Setting("incorrect")
        private String incorrect;
        @Setting("player-only")
        private String playerOnly;
        @Setting
        private CmdLogin login;
        @Setting
        private CmdRegister register;
        @Setting("reset-password")
        private CmdResetPassword resetPassword;

        @Data
        @ConfigSerializable
        public static class CmdLogin {
            @Setting
            private String ok;
            @Setting("no-account")
            private String noAccount;
            @Setting("wrong-password")
            private String wrongPassword;
        }

        @Data
        @ConfigSerializable
        public static class CmdRegister {
            @Setting
            private String ok;
            @Setting("invalid-name")
            private String invalidName;
            @Setting("account-exists")
            private String accountExists;
            @Setting("invalid-password")
            private String invalidPassword;
            @Setting("password-mismatch")
            private String passwordMismatch;
            @Setting("no-enabled")
            private String noEnabled;
        }

        @Data
        @ConfigSerializable
        public static class CmdResetPassword {
            @Setting
            private String ok;
            @Setting("no-account")
            private String noAccount;
            @Setting("wrong-old-password")
            private String wrongOldPassword;
            @Setting("same-password")
            private String samePassword;
            @Setting("invalid-password")
            private String invalidPassword;
        }
    }

}
