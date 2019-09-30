package com.zzj.influxdbdemo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.sun.org.apache.xpath.internal.SourceTree;
import com.zzj.influxdbdemo.config.InfluxDBConfig;
import com.zzj.influxdbdemo.entity.TrackPoint;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        //毫秒输出
        QueryResult queryResult = influxDB.query(query, TimeUnit.MILLISECONDS);
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
    @GetMapping("/insert")
    public void insert(){
        TrackPoint trackPoint = new TrackPoint();
        trackPoint.setCpuid("666");
        trackPoint.setCputype("F");
        trackPoint.setLat(12.335555f);
        trackPoint.setLon(55.125011f);
        trackPoint.setState("on");
        Map<String,Object> bean = new HashMap();
        try {
            //BeanUtils.describe(trackPoint);//这个只能转String,String 不符合要求
            //这个转换也不符合要求，因为转换map过程中，字段类型发生变化，在后面写入influx中，会变成创建新的filed 或者 直接出错。
            //可以自写反射
            String jsonString = JSON.toJSONString(trackPoint);
            bean = JSON.parseObject(jsonString);
            Point po = Point.measurement("trackpoint").fields(bean).build();
            //influxDB.setDatabase("testdb").write(po);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //一种
        Point.Builder builder = Point.measurement("trackpoint");
        builder.time(System.currentTimeMillis(),TimeUnit.MICROSECONDS);
        builder.addField("lat",12.441234f);
        builder.addField("lon",56.512399f);
        builder.addField("state","off");
        builder.tag("cpuid","666888");
        builder.tag("cputype","F");
        Point point = builder.build();
        influxDB.setDatabase("testdb").write(point);
    }
}
