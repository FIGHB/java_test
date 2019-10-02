package com.cskaoyan.mall.service;

import com.cskaoyan.mall.bean.Storage;
import com.cskaoyan.mall.mapper.StorageMapper;
import com.cskaoyan.mall.mapper.selfmapper.WxfStorageMapper;
import com.cskaoyan.mall.vo.BaseRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageServiceImpl implements StorageService {
    @Autowired
    WxfStorageMapper wxfStorageMapper;
    @Autowired
    StorageMapper storageMapper;
    @Override
    public BaseRespVo insert(Storage storage) {
        storageMapper.insertSelective(storage);
        Storage storage1 = wxfStorageMapper.selectByKey(storage.getKey());
        return BaseRespVo.ok(storage1);
    }
}