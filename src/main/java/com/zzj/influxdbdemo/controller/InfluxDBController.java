package com.zzj.influxdbdemo.controller;

import com.sun.org.apache.xpath.internal.SourceTree;
import com.zzj.influxdbdemo.config.InfluxDBConfig;
import com.zzj.influxdbdemo.entity.TrackPoint;
import org.apache.commons.beanutils.BeanUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<QueryResult.Result> resultList =  queryResult.getResults();

        String sss = queryResult.toString();
        //两种方式都可以
        InfluxDBConfig config = new InfluxDBConfig(userName,password,url,"testdb");
        InfluxDB dbConfig = config.getInfluxDB();
        queryResult = dbConfig.query(query);

        //把查询出的结果集转换成对应的实体对象，聚合成list
        List<TrackPoint> trackPoints = new ArrayList<>();
        for(QueryResult.Result result:resultList){
            List<QueryResult.Series> seriesList = result.getSeries();
            for(QueryResult.Series series : seriesList){
                String name = series.getName();
                Map<String, String> tags = series.getTags();
                List<String> columns = series.getColumns();
                String[] keys =  columns.toArray(new String[columns.size()]);
                List<List<Object>> values = series.getValues();
                for(List<Object> value:values){
                    Map beanMap = new HashMap();
                    TrackPoint point = new TrackPoint();
                    for (int i = 0; i < keys.length; i++) {
                        beanMap.put(keys[i],value.get(i));
                    }
                    try {
                        //查询的时候没用，写入point的时候用
                        Point po = Point.measurement("trackpoint").fields(beanMap).build();
                        //time 默认存入的是UTC格式  2019-09-29T22:58:23.58978834，默认转换成String
                        //{"cpuid":"11","cputype":"H","lat":"10.111222","lon":"78.000111","state":"on","time":"2019-09-29T22:57:06.732701567Z"}
                        BeanUtils.populate(point,beanMap );
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    System.out.println(point.toString());
                    trackPoints.add(point);
                }
            }
        }

        return trackPoints.toString();
    }
}
