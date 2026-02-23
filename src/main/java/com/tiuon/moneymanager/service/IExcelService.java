package com.tiuon.moneymanager.service;

import com.tiuon.moneymanager.dto.IncomeDto;
import com.tiuon.moneymanager.entity.IncomeEntity;

import java.io.IOException;
import java.util.List;

public interface IExcelService {
    public byte[] generateExcel(List<?> transactions, String type) throws IOException;
}
