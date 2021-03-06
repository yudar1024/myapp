package com.flink.samples.window;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

public class SlidingWindow {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> localhost = executionEnvironment.socketTextStream("localhost", 9000);
//        输入 spark,3
//        hadoop,2
        SingleOutputStreamOperator<Tuple2<String, Integer>> maped = localhost.map((String x) -> {
            String[] split = x.split(",");
            return Tuple2.of(split[0], Integer.parseInt(split[1]));
        }).returns(Types.TUPLE(Types.STRING, Types.INT));

        KeyedStream<Tuple2<String, Integer>, Tuple> keyed = maped.keyBy(0);
//        keyed.timeWindow(Time.seconds(6),Time.seconds(3)).sum(1).print(); 与下面一句等价
        keyed.window(SlidingProcessingTimeWindows.of(Time.seconds(6), Time.seconds(3))).sum(1).print();
        executionEnvironment.execute("SlidingWindow");
    }

    public static void eventTimeSlidingWindow() throws Exception {
        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();
        executionEnvironment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //        输入1583118000 spark,3  第一位是时间戳，注意是格林威治时间
//        1583118002 hadoop,2
        SingleOutputStreamOperator<String> localhost = executionEnvironment.socketTextStream("localhost", 9000).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor<String>(Time.seconds(0)) {
            @Override
            public long extractTimestamp(String element) {
                String[] split = element.split(",");
                return Long.parseLong(split[0]);
            }
        });

        SingleOutputStreamOperator<Tuple2<String, Integer>> maped = localhost.map((String x) -> {
            String[] split = x.split(",");
            return Tuple2.of(split[1], Integer.parseInt(split[2]));
        }).returns(Types.TUPLE(Types.STRING, Types.INT));

        KeyedStream<Tuple2<String, Integer>, Tuple> keyed = maped.keyBy(0);
//        keyed.timeWindow(Time.seconds(6),Time.seconds(3)).sum(1).print(); 与下面一句等价
        keyed.window(SlidingEventTimeWindows.of(Time.seconds(6),Time.seconds(3))).sum(1).print();
        executionEnvironment.execute("eventTimeSlidingWindow");
    }
}
