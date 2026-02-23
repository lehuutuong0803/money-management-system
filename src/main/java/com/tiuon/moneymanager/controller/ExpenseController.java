package com.tiuon.moneymanager.controller;

import com.tiuon.moneymanager.dto.ExpenseDto;
import com.tiuon.moneymanager.dto.IncomeDto;
import com.tiuon.moneymanager.entity.ProfileEntity;
import com.tiuon.moneymanager.service.IEmailService;
import com.tiuon.moneymanager.service.IExcelService;
import com.tiuon.moneymanager.service.IExpenseService;
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
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final IExpenseService iExpenseService;
    private final IExcelService iExcelService;
    private final IEmailService iEmailService;
    private final IProfileService iProfileService;

    @PostMapping
    public ResponseEntity<ExpenseDto> addExpense(@RequestBody ExpenseDto expenseDto) {
        ExpenseDto savedExpenseDto = iExpenseService.addExpense(expenseDto);
        return  ResponseEntity.status(HttpStatus.CREATED).body(savedExpenseDto);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getExpenses() {
        List<ExpenseDto> expenseDtoList = iExpenseService.getCurrentMonthExpensesForCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(expenseDtoList);
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        iExpenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<ByteArrayResource> exportToExcel() {
        try {
            // Fetch data from the current month
            List<ExpenseDto> expenseDtoList = iExpenseService.getCurrentMonthExpensesForCurrentUser();

            // Generate Excel file
            byte[] excelData = iExcelService.generateExcel(expenseDtoList, "expense");

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
            List<ExpenseDto> incomeDtoList = iExpenseService.getCurrentMonthExpensesForCurrentUser();
            iEmailService.sendCurrentIncomeReportEmail(profile.getEmail(), false, null, incomeDtoList);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(HttpStatus.OK).body("Email to user successfully!");
    }
}
