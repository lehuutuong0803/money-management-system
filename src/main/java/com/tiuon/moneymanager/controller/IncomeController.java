package com.tiuon.moneymanager.controller;

import com.tiuon.moneymanager.dto.ExpenseDto;
import com.tiuon.moneymanager.dto.IncomeDto;
import com.tiuon.moneymanager.entity.IncomeEntity;
import com.tiuon.moneymanager.entity.ProfileEntity;
import com.tiuon.moneymanager.service.IEmailService;
import com.tiuon.moneymanager.service.IExcelService;
import com.tiuon.moneymanager.service.IIcomeService;
import com.tiuon.moneymanager.service.IProfileService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeController {

    private final IIcomeService iIcomeService;
    private final IExcelService iExcelService;
    private final IEmailService iEmailService;
    private final IProfileService iProfileService;

    @PostMapping
    public ResponseEntity<IncomeDto> addExpense(@RequestBody IncomeDto incomeDto) {
        IncomeDto savedIncomeDto = iIcomeService.addIncome(incomeDto);
        return  ResponseEntity.status(HttpStatus.CREATED).body(savedIncomeDto);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDto>> getIncomes() {

        List<IncomeDto> incomeDtoList = iIcomeService.getCurrentMonthIncomesForCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(incomeDtoList);
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long incomeId) {
        iIcomeService.deleteIncome(incomeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<ByteArrayResource> exportToExcel() {
        try {
            // Fetch data from the current month
            List<IncomeDto> incomeDtoList = iIcomeService.getCurrentMonthIncomesForCurrentUser();

            // Generate Excel file
            byte[] excelData = iExcelService.generateExcel(incomeDtoList, "income");

            // Create filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "Income_Report_" + timestamp + ".xlsx";

            // Prepare response
            ByteArrayResource resource = new ByteArrayResource(excelData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelData.length)
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/email")
    public ResponseEntity<String> emailCurrentIncomes() {
        try {
            ProfileEntity profile = iProfileService.getCurrentProfile();
            List<IncomeDto> incomeDtoList = iIcomeService.getCurrentMonthIncomesForCurrentUser();
            iEmailService.sendCurrentIncomeReportEmail(profile.getEmail(), true, incomeDtoList, null);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(HttpStatus.OK).body("Email to user successfully!");
    }
}
