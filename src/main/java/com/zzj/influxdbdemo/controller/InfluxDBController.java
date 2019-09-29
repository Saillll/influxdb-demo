package com.zzj.influxdbdemo.controller;

import com.zzj.influxdbdemo.config.InfluxDBConfig;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/influx")
public class InfluxDBController {

    @Value("${spring.influx.user}")
    private String userName;

    @Value("${spring.influx.password}")
    private String password;

    @Value("${spring.influx.url}")
    private String url;


    @Autowired
    private InfluxDB influxDB;

    @GetMapping("/get")
    public String test(){
        String sql = "select * from trackpoint";
        Query query = new Query(sql,"testdb");
        influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
        QueryResult queryResult = influxDB.query(query);


        InfluxDBConfig config = new InfluxDBConfig(userName,password,url,"testdb");
        InfluxDB dbConfig = config.getInfluxDB();
        queryResult = dbConfig.query(query);
        return "111111111";
    }
}
