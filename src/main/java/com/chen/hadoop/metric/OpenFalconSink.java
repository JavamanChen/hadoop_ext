package com.chen.hadoop.metric;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.SubsetConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.metrics2.AbstractMetric;
import org.apache.hadoop.metrics2.MetricsRecord;
import org.apache.hadoop.metrics2.MetricsSink;
import org.apache.hadoop.metrics2.MetricsTag;
import com.tycs.jsonrpc4go.rpccli.RpcClient;

public class OpenFalconSink implements MetricsSink, Closeable {

	public final Log LOG = LogFactory.getLog(this.getClass());
	private static final String HOSTNAME = "servers";
	private static final String PROT = "port";
	private RpcClient client;
	Integer period;
	String endpoint;

	public void init(SubsetConfiguration conf) {
		String hostname = conf.getString(HOSTNAME);
		Integer port = conf.getInt(PROT);
		period = conf.getInteger("period", 60);

		LOG.info("Initializing the OpenFalconSink for OpenFalcon metrics, " + hostname + ":" + port);
		client = new RpcClient(hostname, port);
		client.connect();
	}

	public void putMetrics(MetricsRecord record) {
		List<Object> list = new ArrayList<Object>();
		if (endpoint == null || "".equals(endpoint)) {
			for (MetricsTag tag : record.tags()) {
				if ("hostname".equalsIgnoreCase(tag.name())) {
					endpoint = tag.value();
				}
			}
		}
		for (AbstractMetric metric : record.metrics()) {
			list.add(new MetricItem(endpoint, "hbase." + metric.name(), record.timestamp(), period, 
					metric.value().doubleValue(), metric.type().toString(), ""));
		}
		
		if (getClient() == null) {
			LOG.error("connect to OpenFalcon failed, give up metrics sent!");
			return;
		}
		
		if (!client.sendToSvrSync("Transfer.Update", list, 5000)) {
			LOG.error("send item failed: " + list);
		}
	}

	public void flush() {
		// nothing to do as we are not buffering data
	}

	private RpcClient getClient() {
		if ("Open".equals(client.status())) {
			return client;
		} else {
			LOG.warn("open falcon client has disconnect, connect again!");
			if (client.connect()) {
				return client;
			} else {
				return null;
			}
		}
	}

	public void close() throws IOException {
		client.stopRpcCli();
	}
}
