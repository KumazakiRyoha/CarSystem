package com.morron.bus.mapper;

import com.morron.bus.domain.Car;
import com.morron.bus.vo.CarVo;

import java.util.List;

public interface CarMapper {
    int deleteByPrimaryKey(String carnumber);

    int insert(Car record);

    int insertSelective(Car record);

    Car selectByPrimaryKey(String carnumber);

    int updateByPrimaryKeySelective(Car record);

    int updateByPrimaryKey(Car record);

    List<Car> queryAllCar(CarVo carVo);
}