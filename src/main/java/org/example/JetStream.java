package org.example;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import com.hazelcast.jet.pipeline.WindowDefinition;

import java.util.concurrent.TimeUnit;


public class JetStream {
    static final String filepath = "./logs/";
    static final String REQUESTS_COUNT_MAP = "RequestsCountMap";
    public static void start() {
        HazelcastInstance hz = Hazelcast.bootstrappedInstance();
        Pipeline pipeline = Pipeline.create();

        pipeline.readFrom(Sources.fileWatcher(filepath))
                .withIngestionTimestamps()
                .filter(line -> isValidRequest(line))
                .map(JetStream::extractResourcePath)
                .groupingKey(resource -> resource)
                .rollingAggregate(AggregateOperations.counting())
                .writeTo(Sinks.map(REQUESTS_COUNT_MAP));

        pipeline.readFrom(Sources.fileWatcher(filepath))
                .withIngestionTimestamps()
                .filter(line -> isValidRequest(line))
                .window(WindowDefinition.tumbling(TimeUnit.SECONDS.toMillis(30)))
                .aggregate(AggregateOperations.counting())
                .writeTo(Sinks.logger());

        hz.getJet().newJob(pipeline).join();
        hz.getMap("RequestsCountMap").forEach((key, value) -> System.out.println(key + ": " + value));

    }

    private static boolean isValidRequest(String logLine) {
        return logLine.matches("\\[.*\\] Request: GET .* HTTP/1\\.1");
    }

    private static String extractResourcePath(String logLine) {
        String[] parts = logLine.split(" ");
        return parts.length > 2 ? parts[2] : "/";
    }
}