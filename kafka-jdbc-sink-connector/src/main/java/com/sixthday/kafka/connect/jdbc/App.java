package com.sixthday.kafka.connect.jdbc;

import com.sixthday.kafka.connect.jdbc.task.CustomJDBCSinkTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class App {
    @SneakyThrows
    public static void main(String[] args) {

        Map<String, String> map = new HashMap<>();

        map.put("batch.size", "3");
        map.put("connector.class", "com.sixthday.kafka.connect.jdbc.CustomJDBCSinkConnector");
        map.put("database.catalog", "mydb");
        map.put("database.host", "127.0.0.1");
        map.put("database.password", "pgadmin");
        map.put("database.port", "5432");
        map.put("database.schema", "poc");
        map.put("database.username", "postgres");
        map.put("errors.deadletterqueue.context.headers.enable", "true");
        map.put("errors.deadletterqueue.topic.name", "dlt_topic");
        map.put("key.converter", "org.apache.kafka.connect.json.JsonConverter");
        map.put("key.converter.schemas.enable", "false");
        map.put("login.timeout.seconds", "15");
        map.put("max.retries", "5");
        map.put("producer.bootstrap.servers", "localhost:9092");
        map.put("producer.key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        map.put("producer.value.serializer", "com.sixthday.kafka.connect.jdbc.serialization.JsonSerialization");
        map.put("retry.backoff.ms", "10000");
        map.put("tables", "group, subgroup");
        map.put("tables.group.payload", "$");
        map.put("tables.group.mode", "insert");
        map.put("tables.group.mode.insert.exclude.columns", "auto");
        map.put("tables.subgroup.columns.mapping", "subgroup_json=$.subgroup_json_val");
        map.put("tables.subgroup.mode", "insert");
        map.put("tables.subgroup.mode.insert.exclude.columns", "subgroup_id");
        map.put("tables.subgroup.mode.update.clause", "subgroup.aud_insrt_id = @aud_insrt_id");
        map.put("tables.subgroup.mode.update.columns", "aud_updt_tmstp=aud_insrt_tmstp, subgroup_json");
        map.put("tables.subgroup.mode.update.keys", "sub_group_id, srce_sys_cd, group_id");
        map.put("tables.subgroup.payload", "$");
        map.put("tasks.max", "1");
        map.put("topics", "sink_topic");
        map.put("value.converter", "org.apache.kafka.connect.json.JsonConverter");
        map.put("value.converter.schemas.enable", "false");

        ObjectMapper om = new ObjectMapper();

        CustomJDBCSinkTask customJdbcSinkTask = new CustomJDBCSinkTask();
        customJdbcSinkTask.start(map);

        String oo1 = "{\"times\":\"1\",\"srce_sys_cd\":\"1\",\"sub_group_id\":\"1\",\"group_id\":\"1\",\"subgroup_json_val\":{\"number\":13},\"aud_insrt_id\":\"insert-10\",\"aud_insrt_tmstp\":\"2011-10-02 18:48:05.69434 \",\"aud_updt_id\":\"update-10\",\"aud_updt_tmstp\":\"2022-01-24 16:02:25.69434\"}";
        String oo2 = "{\"times\":\"2011-10-02 20:48:05.69434\",\"srce_sys_cd\":\"1\",\"sub_group_id\":\"2\",\"group_id\":\"2\",\"subgroup_json_val\":{\"number\":13},\"aud_insrt_id\":\"insert-10\",\"aud_insrt_tmstp\":\"2011-10-02 18:48:05.69434\",\"aud_updt_id\":\"update-10\",\"aud_updt_tmstp\":\"2022-01-24 16:02:25.69434\"}";
        String oo3 = "{\"times\":\"2011-10-02 18:48:05.69434\",\"srce_sys_cd\":\"1\",\"sub_group_id\":\"3\",\"group_id\":\"3\",\"subgroup_json_val\":{\"number\":13},\"aud_insrt_id\":\"insert-10\",\"aud_insrt_tmstp\":\"2011-10-02 18:48:05.69434\",\"aud_updt_id\":\"update-10\",\"aud_updt_tmstp\":\"2022-01-24 16:02:25.69434\"}s";
        SinkRecord sinkRecord1 = new SinkRecord("sa", 1, null, null, null, om.readValue(oo1, Object.class), 9);
        SinkRecord sinkRecord2 = new SinkRecord("sa", 1, null, null, null, om.readValue(oo2, Object.class), 9);
        SinkRecord sinkRecord3 = new SinkRecord("sa", 1, null, null, null, om.readValue(oo3, Object.class), 9);

        String oo4 = "{\"srce_sys_cd\":\"2\",\"sub_group_id\":\"1\",\"group_id\":\"1\",\"subgroup_json_val\":{\"number\":13},\"aud_insrt_id\":\"insert-10\",\"aud_insrt_tmstp\":\"2011-10-02 18:48:05.69434\",\"aud_updt_id\":\"update-10\",\"aud_updt_tmstp\":\"2022-01-24 16:02:25.69434\"}";
        String oo5 = "{\"times\":\"2011-10-02 19:48:05.6943\",\"srce_sys_cd\":\"12\",\"sub_group_id\":\"2\",\"group_id\":\"2\",\"subgroup_json_val\":{\"number\":13},\"aud_insrt_id\":\"insert-10\",\"aud_insrt_tmstp\":\"2011-10-02 18:48:05.69434\",\"aud_updt_id\":\"update-10\",\"aud_updt_tmstp\":\"2022-01-24 16:02:25.69434\"}";
        String oo6 = "{\"times\":\"6\",\"srce_sys_cd\":\"13\",\"sub_group_id\":\"3\",\"group_id\":\"3\",\"subgroup_json_val\":{\"number\":13},\"aud_insrt_id\":\"insert-10\",\"aud_insrt_tmstp\":\"2011-10-02 18:48:05.69434\",\"aud_updt_id\":\"update-10\",\"aud_updt_tmstp\":\"2022-01-24 16:02:25.69434\"}";
        SinkRecord sinkRecord4 = new SinkRecord("sa", 1, null, null, null, om.readValue(oo4, Object.class), 9);
        SinkRecord sinkRecord5 = new SinkRecord("sa", 1, null, null, null, om.readValue(oo5, Object.class), 9);
        SinkRecord sinkRecord6 = new SinkRecord("sa", 1, null, null, null, om.readValue(oo6, Object.class), 9);
        customJdbcSinkTask.put(Arrays.asList(sinkRecord4, sinkRecord6, sinkRecord5));
        customJdbcSinkTask.put(Arrays.asList(sinkRecord1, sinkRecord2, sinkRecord3));
        customJdbcSinkTask.stop();
    }
}
