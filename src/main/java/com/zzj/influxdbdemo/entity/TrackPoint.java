package com.zzj.influxdbdemo.entity;

import lombok.Data;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 * InfluxDB中,measurement对应于传统关系型数据库中的table(database为配置文件中的log_management).
 * InfluxDB里存储的数据称为时间序列数据,时序数据有零个或多个数据点.
 * 数据点包括time(一个时间戳)，measurement(例如logInfo)，零个或多个tag，其对应于level,module,device_id),至少一个field(即日志内容,msg=something error).
 * InfluxDB会根据tag数值建立时间序列(因此tag数值不能选取诸如UUID作为特征值,易导致时间序列过多,导致InfluxDB崩溃),并建立相应索引,以便优化诸如查询速度.
 */
@Data
@Measurement(name = "trackpoint")
public class TrackPoint {
    @Column(name = "time")
    private long time;

    @Column(name = "cpuid",tag = true)
    private String cpuid;

    @Column(name = "cputype",tag = true)
    private String cputype;

    @Column(name = "lat")
    private String lat;

    @Column(name = "lon")
    private String lon;

    @Column(name = "state")
    private String state;
}
