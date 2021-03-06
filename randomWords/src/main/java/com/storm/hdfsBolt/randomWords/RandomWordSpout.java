package com.storm.hdfsBolt.randomWords;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
 
import java.util.Map;
import java.util.Random;
 
public class RandomWordSpout extends BaseRichSpout {
  SpoutOutputCollector _collector;
  Random _rand;
 
  @Override
  public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
    _collector = collector;
    _rand = new Random();
  }
 
 @Override
  public void nextTuple() {
    Utils.sleep(100);
    String[] words = new String[]{
      "Hortonworks",
      "MapR",
      "Cloudera",
      "Storm",
      "Hadoop",
      "Kafka",
      "Spark"
      };
 
    String word = words[_rand.nextInt(words.length)];
    _collector.emit(new Values(word));
  }
 
  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("word"));
  }
 
}