package fiftyone.device.example.illustration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fiftyone.device.example.Shared;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.DatasetBuilder;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.cache.*;

import java.io.IOException;
import java.util.Date;

import static fiftyone.mobile.detection.DatasetBuilder.CacheType.NodesCache;
import static fiftyone.mobile.detection.DatasetBuilder.CacheType.ProfilesCache;
import static fiftyone.mobile.detection.DatasetBuilder.NODES_CACHE_SIZE;
import static fiftyone.mobile.detection.DatasetBuilder.PROFILES_CACHE_SIZE;

/**
 * Example Illustrates using a Guava cache in place of the 51 Degrees internal LRU Cache
 */
class GuavaExample {

    @SuppressWarnings("WeakerAccess")
    public static class GuavaCacheAdaptor<K,V>  implements ICache<K,V> {
        protected final Cache<K,V> cache;

        public GuavaCacheAdaptor(Cache<K,V> cache) {
            this.cache = cache;
        }

        @Override
        public V get(K key) {
            return cache.getIfPresent(key);
        }

        @Override
        public long getCacheSize() {
            return cache.size();
        }

        @Override
        public long getCacheMisses() {
            return cache.stats().missCount();
        }

        @Override
        public long getCacheRequests() {
            return cache.stats().requestCount();
        }

        @Override
        public double getPercentageMisses() {
            return getCacheMisses()/getCacheRequests();
        }

        @Override
        public void resetCache() {
            cache.invalidateAll();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class PutCacheAdaptor<K,V> extends GuavaCacheAdaptor<K,V> implements IPutCache<K,V>{

        public PutCacheAdaptor(Cache<K, V> cache) {
            super(cache);
        }

        @Override
        public void put(K key, V value) {
            cache.put(key, value);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class UaCacheAdaptor <K,V> extends GuavaCacheAdaptor<K,V> implements ILoadingCache<K,V> {

        public UaCacheAdaptor(com.google.common.cache.Cache<K,V> cache) {
            super(cache);
        }

        @Override
        public V get(K key, IValueLoader<K, V> loader) throws IOException {
            V result = get(key);
            if (result == null) {
                result = loader.load(key);
                if (result != null) {
                    cache.put(key, result);
                }
            }
            return result;
        }

        @Override
        public V get(K key) {
            return cache.getIfPresent(key);
        }
    }

    static class GuavaCacheBuilder implements ICacheBuilder {
        public ICache build(int size){
            com.google.common.cache.Cache guavaCache = CacheBuilder.newBuilder()
                    .initialCapacity(size)
                    .maximumSize(size)
                    .concurrencyLevel(5) // set to number of threads that can access cache at same time
                    .build();

            return new PutCacheAdaptor(guavaCache);
        }
    }

    public static void main (String[] args) throws IOException {
        com.google.common.cache.Cache uaCache = CacheBuilder.newBuilder()
                .initialCapacity(100000)
                .maximumSize(100000)
                .concurrencyLevel(5) // set to number of threads that can access cache at same time
                .build();

        ICacheBuilder builder = new GuavaCacheBuilder();

        @SuppressWarnings("unchecked")
        Dataset dataset = DatasetBuilder.file()
                .setCacheBuilder(NodesCache, builder)
                .setCacheBuilder(ProfilesCache, builder)
                .lastModified(new Date())
                .build(Shared.getLitePatternV32());

        @SuppressWarnings("unchecked")
        Provider provider = new Provider(dataset, new UaCacheAdaptor(uaCache));

        Match match = provider.match("Hello World");
        System.out.printf("%s", match.getSignature());
    }
}
