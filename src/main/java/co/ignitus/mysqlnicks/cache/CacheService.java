package co.ignitus.mysqlnicks.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
public abstract class CacheService<TK, TV> {

    private final String id;

    @Getter
    protected final LoadingCache<TK, Optional<TV>> loadingCache;

    protected final ListeningExecutorService listeningDecorator;

    protected CacheService(String id) {
        this(id, new ThreadPoolExecutor(
                0,
                Runtime.getRuntime().availableProcessors(),
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>())
        );
    }

    protected CacheService(String id, ExecutorService service) {
        this.id = id;
        this.listeningDecorator = MoreExecutors.listeningDecorator(service);
        this.loadingCache = CacheBuilder.newBuilder()
                .refreshAfterWrite(1, TimeUnit.HOURS)
                .build(creatCacheLoader());
        init();
    }

    protected void init() {
        ConcurrentMap<TK, Optional<TV>> prepareCache = prepareCache();
        loadingCache.putAll(prepareCache);
        log.debug("[CACHE_{}] 缓存初始化完成, 共加载 {} 个数据", id, prepareCache.size());
    }

    /**
     * 缓存初始化 - 预加载缓存
     * */
    protected abstract ConcurrentMap<TK, Optional<TV>> prepareCache();

    /**
     * 缓存加载逻辑
     * */
    @NotNull
    protected abstract Optional<TV> load(@NotNull TK key) throws Exception;

    /**
     * 缓存增量更新
     * */
    public void incrementalUpdate(Map<TK, TV> dataMap) {
        for (Map.Entry<TK, TV> entry : dataMap.entrySet()) {
            loadingCache.put(entry.getKey(), Optional.ofNullable(entry.getValue()));
        }
    }

    @NotNull
    public Optional<TV> getCache(TK key) {
        Optional<TV> value = loadingCache.getIfPresent(key);
        if (value == null) {
            try {
                return loadingCache.getUnchecked(key);
            } catch (Exception e) {
                log.error("[CACHE_{}] 获取缓存key {} 异常", id, key, e);
            }
            return Optional.empty();
        }
        return value;
    }

    protected CacheLoader<TK, Optional<TV>> creatCacheLoader() {
        return new CacheLoader<TK, Optional<TV>>() {
            @Override
            public @NotNull Optional<TV> load(@NotNull TK key) throws Exception {
                return CacheService.this.load(key);
            }

            @Override
            public @NotNull ListenableFuture<Optional<TV>> reload(@NotNull TK key, @NotNull Optional<TV> oldValue) throws Exception {
                return listeningDecorator.submit(() -> {
                    Optional<TV> optional = load(key);
                    if (!optional.isPresent()) {
                        log.debug("[CACHE_{}] 当前key {} 不存在，即被删除", CacheService.this.id, key);
                    } else {
                        TV newCache = optional.get();
                        TV oldCache = null;
                        boolean logFlag = false;

                        if (!oldValue.isPresent()) {
                            logFlag = true;
                        } else {
                            oldCache = oldValue.get();
                            if (!newCache.equals(oldCache)) {
                                logFlag = true;
                            }
                        }
                        if (logFlag) {
                            log.debug("[CACHE_{}] 当前key {} 即被更新,原值={},新值={}", CacheService.this.id, key, oldCache, newCache);
                        }
                    }
                    return optional;
                });
            }
        };
    }

}
