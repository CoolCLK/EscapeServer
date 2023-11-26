package coolclk.escape;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import coolclk.escape.api.SerializedMap;
import coolclk.escape.until.StreamUntil;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class DataHandler {
    public static class Account {
        public enum LoginResult {
            SUCCESS(new SerializedMap()),
            WRONG_ACCOUNT,
            WRONG_PASSWORD,
            ERROR;

            private SerializedMap detail;

            LoginResult() {
                this(new SerializedMap());
            }

            LoginResult(SerializedMap d) {
                this.detail = d;
            }

            SerializedMap getDetail() {
                return this.detail;
            }

            void setDetail(SerializedMap d) {
                this.detail = d;
            }
        }

        @Getter private final String account;
        @Getter @Setter private String nickname;

        public Account(String account) {
            this.account = account;
            this.nickname = account;
        }

        public static LoginResult login(String account, String password) {
            AtomicReference<LoginResult> result = new AtomicReference<>(LoginResult.ERROR);
            try {
                AtomicReference<Account> accountObject = new AtomicReference<>(null);
                getLocalData().get("accounts").getAsJsonArray().forEach(e -> {
                    if (Objects.equals(e.getAsJsonObject().get("account").getAsString(), account)) {
                        if (Objects.equals(e.getAsJsonObject().get("password").getAsString(), password)) {
                            Account object = new Account(account);
                            if (e.getAsJsonObject().has("nickname")) {
                                object.setNickname(e.getAsJsonObject().get("nickname").getAsString());
                            }
                            accountObject.set(object);
                        } else {
                            result.set(LoginResult.WRONG_PASSWORD);
                        }
                    }
                });
                if (accountObject.get() != null) {
                    result.set(LoginResult.SUCCESS);
                    result.get().setDetail(new SerializedMap(new SerializedMap.MapObject("account", accountObject.get())));
                } else {
                    result.set(LoginResult.WRONG_ACCOUNT);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return result.get();
        }
    }

    public static JsonObject getLocalData() throws IOException {
        File dataFile = new File("data.json");
        if (!dataFile.exists() && dataFile.createNewFile()) {
            try (FileOutputStream stream = new FileOutputStream(dataFile)) {
                StreamUntil.transform(Main.class.getResourceAsStream("/DEFAULT/data.json"), stream);
            }
        }

        return new Gson().fromJson(new FileReader(dataFile), JsonObject.class);
    }
}
