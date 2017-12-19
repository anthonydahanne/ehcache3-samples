# Ehcache3 Management sample
Although it is not official, you can access some statistics from ehcache3.

Make sure you add to your classpath the modules named ehcache-management and nms-common.
If you're not relying on a Java modules repository, you'll need a kit :

https://github.com/ehcache/ehcache3/releases/download/v3.4.0/ehcache-clustered-3.4.0-kit.zip

And some additionnal jars :

http://repo.terracotta.org/maven2/org/terracotta/management/dist/mnm-common/5.3.1/mnm-common-5.3.1.jar
https://search.maven.org/remotecontent?filepath=org/ehcache/modules/ehcache-management/3.3.1/ehcache-management-3.3.1.jar

Otherwise, be guided via our pom.xml.

Now build it :

    mvn clean install

And then run it :

    java -jar target/management-sample-1.0-SNAPSHOT-jar-with-dependencies.jar

You should see something like :

    INFO  o.e.m.sample.EhcacheWithMonitoring - Statistics :
    {Cache:EvictionCount=0, Cache:ExpirationCount=0, Cache:HitCount=383, Cache:MissCount=1, Cache:PutCount=192, Cache:RemovalCount=0, Cache:UpdateCount=191, OffHeap:AllocatedByteSize=5120, OffHeap:EvictionCount=0, OffHeap:ExpirationCount=0, OffHeap:HitCount=383, OffHeap:MappingCount=1, OffHeap:MissCount=1, OffHeap:OccupiedByteSize=104, OffHeap:PutCount=192, OffHeap:RemovalCount=0, OffHeap:UpdateCount=191}