package com.storm.hdfsBolt.randomWords;
 
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
 
import backtype.storm.tuple.Fields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.hdfs.common.security.HdfsSecurityUtil;

import com.storm.hdfsBolt.randomWords.RandomWordSpout;
 
public class RandomWordsTopology {
    public static void main(String[] args) throws Exception {
 
        //It is a comma separated value file (although only one word per line is written)
        RecordFormat format = new DelimitedRecordFormat().withFieldDelimiter(",");
        SyncPolicy syncPolicy = new CountSyncPolicy(1000);
 
        //Rotate files after 127MB (Hortonworks default fileblock size is 128MB)
        FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(127.0f, FileSizeRotationPolicy.Units.MB);
 
        DefaultFileNameFormat fileNameFormat = new DefaultFileNameFormat();
        //The files are written in this HDFS folder
        fileNameFormat.withPath("/ez-ambari-qa");
  //       fileNameFormat.withPath("/user/cdrbd/test_distcp_encryption");
        //Files start with the following filename prefix
        fileNameFormat.withPrefix("RandomWords_");
        //Files end with the following suffix
        fileNameFormat.withExtension(".csv");
 
        //HDFS bolt
        HdfsBolt bolt =
              new HdfsBolt().withFsUrl("hdfs://dcpstage")
  //          new HdfsBolt().withFsUrl("hdfs://EBDASTAGING")

                .withFileNameFormat(fileNameFormat)
                .withRecordFormat(format)
                .withConfigKey("hdfs.config")
                .withRotationPolicy(rotationPolicy)
                .withSyncPolicy(syncPolicy);
 
        TopologyBuilder builder = new TopologyBuilder();
 
        //Spout for accessing random words with parallelism of 1
        builder.setSpout("random-words-spout", new RandomWordSpout(), 1);
 
        //bolt for writing text to HDFS  with parallelism of 1 - it writes into 1 file
        builder.setBolt("hdfs-csv-bolt", bolt, 1).shuffleGrouping("random-words-spout");
        
      /*  List<String> auto_tgts = new ArrayList<String>(); 
        auto_tgts.add("org.apache.storm.hdfs.common.security.AutoHDFS"); 
        
        List<String> auto_cred = new ArrayList<String>();
        auto_cred.add("org.apache.storm.hdfs.common.security.AutoHDFS");
        
        List<String> auto_ren = new ArrayList<String>();
        auto_ren.add("org.apache.storm.hdfs.common.security.AutoHDFS"); */
        
        
        Map<String, Object> mapHdfs = new HashMap<String,Object>();
          mapHdfs.put("fs.defaultFS", "hdfs://dcpstage");
  //      mapHdfs.put("fs.defaultFS", "hdfs://EBDASTAGING");
        mapHdfs.put("hdfs.keytab.file","/etc/security/keytabs/cdrbd.application.keytab");
        mapHdfs.put("hdfs.kerberos.principal","cdrbd@QUANTUM.COM");  
        //mapHdfs.put(HdfsSecurityUtil.STORM_KEYTAB_FILE_KEY, "/etc/security/keytabs/cdrbd.application.keytab");
        //mapHdfs.put(HdfsSecurityUtil.STORM_USER_NAME_KEY,"cdrbd@HDPQUANTUMDEV.COM");
        
        mapHdfs.put("dfs.nameservices", "dcpstage");
        mapHdfs.put("dfs.ha.namenodes.dcpstage", "nn1,nn2");
        mapHdfs.put("dfs.namenode.rpc-address.dcpstage.nn1", "pxnhc139.hadoop.local:8020");
        mapHdfs.put("dfs.namenode.rpc-address.dcpstage.nn2", "pxnhc138.hadoop.local:8020");
        mapHdfs.put("dfs.client.failover.proxy.provider.dcpstage", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        mapHdfs.put("hadoop.security.key.provider.path", "kms://http@pxnhc137.hadoop.local:9292/kms");
        mapHdfs.put("dfs.encryption.key.provider.uri", "kms://http@pxnhc137.hadoop.local:9292/kms");
        mapHdfs.put("hadoop.kms.authentication.kerberos.keytab", " /etc/security/keytabs/spnego.service.keytab");
        mapHdfs.put("hadoop.kms.authentication.kerberos.name.rules", "DEFAULT");
        mapHdfs.put("hadoop.kms.authentication.kerberos.principal", "HTTP/pxnhc139.hadoop.local@QUANTUM.COM");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider", "random");
        mapHdfs.put("hadoop.kms.authentication.type", "kerberos");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.kerberos.keytab", "/etc/security/keytabs/spnego.service.keytab");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.auth.type", "kerberos");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.kerberos.principal", "HTTP/pxnhc139.hadoop.local@QUANTUM.COM");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.path", "/hadoop-kms/hadoop-auth-signature-secret");
        mapHdfs.put("hadoop.kms.security.authorization.manager", "org.apache.ranger.authorization.kms.authorizer.RangerKmsAuthorizer");
        mapHdfs.put("hadoop.security.keystore.JavaKeyStoreProvider.password", "none");
        mapHdfs.put("hadoop.kms.proxyuser.HTTP.hosts", "pxnhc137.hadoop.local");
        mapHdfs.put("hadoop.kms.proxyuser.HTTP.users", "*");
        mapHdfs.put("dfs.namenode.keytab.file", "/etc/security/keytabs/nn.service.keytab");
        mapHdfs.put("dfs.secondary.namenode.keytab.file", "/etc/security/keytabs/nn.service.keytab");
        mapHdfs.put("dfs.web.authentication.kerberos.keytab", "/etc/security/keytabs/spnego.service.keytab");
        mapHdfs.put("dfs.secondary.namenode.kerberos.principal", "nn/_HOST@QUANTUM.COM");
        mapHdfs.put("dfs.namenode.kerberos.principal", "nn/_HOST@QUANTUM.COM");
        
       /* mapHdfs.put("dfs.nameservices", "EBDASTAGING");
        mapHdfs.put("dfs.ha.namenodes.EBDASTAGING", "nn1,nn2");
        mapHdfs.put("dfs.namenode.rpc-address.EBDASTAGING.nn1", "pxnhm538.hadoop.local:8020");
        mapHdfs.put("dfs.namenode.rpc-address.EBDASTAGING.nn2", "pxnhf103.hadoop.local:8020");
        mapHdfs.put("dfs.client.failover.proxy.provider.EBDASTAGING", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        mapHdfs.put("hadoop.security.key.provider.path", "kms://http@pxnhf111.hadoop.local;pxnhf213.hadoop.local:9292/kms");
        mapHdfs.put("dfs.encryption.key.provider.uri", "kms://http@pxnhf111.hadoop.local;pxnhf213.hadoop.local:9292/kms");
        mapHdfs.put("hadoop.kms.authentication.kerberos.keytab", " /etc/security/keytabs/janak.application.keytab");
        mapHdfs.put("hadoop.kms.authentication.kerberos.name.rules", "DEFAULT");
        mapHdfs.put("hadoop.kms.authentication.kerberos.principal", "*");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider", "random");
        mapHdfs.put("hadoop.kms.authentication.type", "kerberos");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.kerberos.keytab", "/etc/security/keytabs/spnego.service.keytab");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.auth.type", "kerberos");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.kerberos.principal", "HTTP/_HOST@HDPQUANTUMDEV.COM");
        mapHdfs.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.path", "/hadoop-kms/hadoop-auth-signature-secret");
        mapHdfs.put("hadoop.kms.key.provider.uri", "dbks://http@localhost:9292/kms");
        mapHdfs.put("hadoop.kms.security.authorization.manager", "org.apache.ranger.authorization.kms.authorizer.RangerKmsAuthorizer");
        mapHdfs.put("hadoop.security.keystore.JavaKeyStoreProvider.password", "none");
        */
        
        Config conf = new Config();
        conf.setDebug(true);
        conf.setNumWorkers(1);
        conf.put("hdfs.config", mapHdfs);
        conf.put("hadoop.kms.authentication.kerberos.keytab", " /etc/security/keytabs/spnego.service.keytab");
        conf.put("hadoop.kms.authentication.kerberos.name.rules", "DEFAULT");
        conf.put("hadoop.kms.authentication.kerberos.principal", "HTTP/pxnhc139.hadoop.local@QUANTUM.COM");
        conf.put("hadoop.kms.authentication.signer.secret.provider", "random");
        conf.put("hadoop.kms.authentication.type", "kerberos");
        conf.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.kerberos.keytab", "/etc/security/keytabs/spnego.service.keytab");
        conf.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.auth.type", "kerberos");
        conf.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.kerberos.principal", "HTTP/pxnhc139.hadoop.local@QUANTUM.COM");
        conf.put("hadoop.kms.authentication.signer.secret.provider.zookeeper.path", "/hadoop-kms/hadoop-auth-signature-secret");
        conf.put("hadoop.kms.security.authorization.manager", "org.apache.ranger.authorization.kms.authorizer.RangerKmsAuthorizer");
        conf.put("hadoop.security.keystore.JavaKeyStoreProvider.password", "none");
        conf.put("hadoop.kms.proxyuser.HTTP.hosts", "pxnhc137.hadoop.local");
        conf.put("hadoop.kms.proxyuser.HTTP.users", "*");
        conf.put("dfs.namenode.keytab.file", "/etc/security/keytabs/nn.service.keytab");
        conf.put("dfs.secondary.namenode.keytab.file", "/etc/security/keytabs/nn.service.keytab");
        conf.put("dfs.web.authentication.kerberos.keytab", "/etc/security/keytabs/spnego.service.keytab");
        conf.put("dfs.secondary.namenode.kerberos.principal", "nn/_HOST@QUANTUM.COM");
        conf.put("dfs.namenode.kerberos.principal", "nn/_HOST@QUANTUM.COM");
        
        
       // conf.put(HdfsSecurityUtil.STORM_KEYTAB_FILE_KEY, "/etc/security/keytabs/cdrbd.application.keytab");
       // conf.put(HdfsSecurityUtil.STORM_USER_NAME_KEY, "cdrbd@HDPQUANTUMDEV.COM");
       // conf.put(Config.TOPOLOGY_AUTO_CREDENTIALS, auto_tgts); 
       // conf.put(Config.NIMBUS_AUTO_CRED_PLUGINS, auto_cred);
       // conf.put(Config.NIMBUS_CREDENTIAL_RENEWERS, auto_ren);
        
        List<String> autoCreds= new ArrayList<String>();
	    
	  //  autoCreds.add("org.apache.storm.hdfs.common.security.AutoHDFS");
	  //  autoCreds.add("org.apache.storm.hbase.security.AutoHBase");
	    
	  //  conf.put(Config.TOPOLOGY_AUTO_CREDENTIALS, autoCreds);
        
        
        //Submit topology and choose its name
        StormSubmitter.submitTopology("RandomWordsHdfsTopology", conf, builder.createTopology());
    }
}