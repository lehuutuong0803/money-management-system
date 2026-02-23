package com.tiuon.moneymanager.service;

import com.tiuon.moneymanager.dto.ExpenseDto;
import com.tiuon.moneymanager.dto.IncomeDto;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface IEmailService {
    void sendEmail(String to, String subject, String body);
    void sendCurrentIncomeReportEmail(String toEmail, boolean isIncome, List<IncomeDto> incomeDtoList, List<ExpenseDto> expenseDtoList)
            throws MessagingException, IOException;
}
