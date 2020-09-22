package com.morron.bus.service;

import com.morron.bus.domain.Car;
import com.morron.bus.vo.CarVo;
import com.morron.sys.utils.DataGridView;

public interface ICarService {

    /**
     * 查询所有车辆
     * @param carVo
     * @return
     */
    public DataGridView queryAllCar(CarVo carVo);

    /**
     * 添加车辆
     * @param carVo
     */
    public void addCar(CarVo carVo);

    /**
     * 修改车辆
     * @param carVo
     */
    public void updateCar(CarVo carVo);

    /**
     * 根据id删除车辆
     * @param carnumber
     */
    public void deleteCar(String carnumber);

    /**
     * 批量删除车辆
     * @param carnumbers
     */
    public void deleteBatchCar(String[] carnumbers);

    /**
     * 根据车牌号查询
     * @param carnumber
     * @return
     */
    Car queryCarByCarNumber(String carnumber);
}
