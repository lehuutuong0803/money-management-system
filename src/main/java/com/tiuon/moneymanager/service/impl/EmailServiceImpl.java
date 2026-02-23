package com.tiuon.moneymanager.service.impl;

import com.tiuon.moneymanager.dto.ExpenseDto;
import com.tiuon.moneymanager.dto.IncomeDto;
import com.tiuon.moneymanager.dto.ProfileDto;
import com.tiuon.moneymanager.entity.ProfileEntity;
import com.tiuon.moneymanager.service.IEmailService;
import com.tiuon.moneymanager.service.IExcelService;
import com.tiuon.moneymanager.service.IIcomeService;
import com.tiuon.moneymanager.service.IProfileService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;
    private final IExcelService iExcelService;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    @Value("$app.email.name")
    private String fromName;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void sendEmailWithExcelAttachment(
            String toEmail,
            String subject,
            String body,
            byte[] excelData,
            String filename) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, fromName);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, true); // true = HTML content

        // Attach Excel file
        ByteArrayResource resource = new ByteArrayResource(excelData);
        helper.addAttachment(filename, resource);

        mailSender.send(message);
    }

    public void sendCurrentIncomeReportEmail(String toEmail, boolean isIncome, List<IncomeDto> incomeDtoList, List<ExpenseDto> expenseDtoList) throws MessagingException, IOException {
        byte[] currentDataExcel;
        if (isIncome) {
            currentDataExcel = iExcelService.generateExcel(incomeDtoList, "income");
        } else {
            currentDataExcel = iExcelService.generateExcel(expenseDtoList, "expense");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        String typeSubject = isIncome ? "Income_Report" : "Expense_Report";
        String filename = typeSubject + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        String subject = "Your "+ typeSubject +" - " + timestamp;

        String body = """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #8b5cf6;">%s</h2>
                    <p>Hello,</p>
                    <p>Please find attached your report for <strong>%s</strong>.</p>
                    <p>If you have any questions or need further assistance, please don't hesitate to contact us.</p>
                    <br>
                    <p>Best regards,<br>
                    <strong>Money Manager Team</strong></p>
                    <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                    <p style="font-size: 12px; color: #888;">
                        This is an automated email. Please do not reply to this message.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(typeSubject, timestamp);

        sendEmailWithExcelAttachment(toEmail, subject, body, currentDataExcel, filename);
    }
}
