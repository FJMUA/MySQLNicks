package co.ignitus.mysqlnicks.cache;

import co.ignitus.mysqlnicks.util.DataUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NameCacheService extends CacheService<UUID, String> {

    public NameCacheService() {
        super("NAME");
    }

    @Override
    protected ConcurrentMap<UUID, Optional<String>> prepareCache() {
        ConcurrentMap<UUID, Optional<String>> map = new ConcurrentHashMap<>();
        DataUtil.getSavedNicknames().forEach((k, v) -> map.put(k, Optional.ofNullable(v)));
        return map;
    }

    @Override
    protected @NotNull Optional<String> load(@NotNull UUID key) {
        return Optional.ofNullable(DataUtil.getNickname(key));
    }

}
