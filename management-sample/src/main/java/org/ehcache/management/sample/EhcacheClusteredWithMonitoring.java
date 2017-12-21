package org.ehcache.management.sample;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.clustered.client.config.ClusteredStoreConfiguration;
import org.ehcache.clustered.client.config.builders.ClusteringServiceConfigurationBuilder;
import org.ehcache.clustered.common.Consistency;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.management.registry.DefaultManagementRegistryConfiguration;
import org.ehcache.management.registry.DefaultManagementRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terracotta.management.model.context.Context;
import org.terracotta.management.model.stats.ContextualStatistics;

import java.net.URI;
import java.time.LocalDate;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.ehcache.clustered.client.config.builders.ClusteredResourcePoolBuilder.clusteredDedicated;

public class EhcacheClusteredWithMonitoring {

  private static final Logger LOGGER = LoggerFactory.getLogger(EhcacheClusteredWithMonitoring.class);

  public static void main(String[] args) throws InterruptedException {
    LOGGER.info("***Configuring CLUSTERED cache manager****");

    DefaultManagementRegistryService managementRegistry1 = new DefaultManagementRegistryService(
        new DefaultManagementRegistryConfiguration()
            .setCacheManagerAlias("cacheManager")
            .addTags("caching", "client")
    );
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(ClusteringServiceConfigurationBuilder.cluster(URI.create("terracotta://localhost:9410/MyCMEntity"))
            .autoCreate())
        .using(managementRegistry1)
        .withCache("session",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(2, MemoryUnit.MB)))
        .withCache("sessionReverse",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(100, MemoryUnit.KB)
                    .offheap(1, MemoryUnit.MB)
                    .with(clusteredDedicated("primary-server-resource", 2, MemoryUnit.MB))
            ).add(new ClusteredStoreConfiguration(Consistency.EVENTUAL)))
        .build(true);

    LOGGER.info("***Successfully configured cache manager****");

    //Then, you can periodically ask the registry about the stats :
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    executor.scheduleAtFixedRate(() -> {
      Context context1 = Context.empty()
          .with("cacheManagerName", "cacheManager")
          .with("cacheName", "sessionReverse");
      ContextualStatistics statistics1 = managementRegistry1.withCapability("StatisticsCapability")
          .queryAllStatistics()
          .on(context1)
          .build()
          .execute()
          .getResult(context1);
      LOGGER.info("Statistics : \n" + new TreeMap(statistics1.getStatistics()).toString());

    }, 2L, 2L, TimeUnit.SECONDS);

    int i = 0;
    while (i < 100_000) {
      Cache<String, String> sessionCache = cacheManager.getCache("sessionReverse", String.class, String.class);
      // miss
      sessionCache.get("hello" + i);
      // put
      sessionCache.put("hello" + i, "value" + LocalDate.now());
      //hit
      sessionCache.get("hello" + i);
      TimeUnit.MILLISECONDS.sleep(100L);
    }
  }
}
